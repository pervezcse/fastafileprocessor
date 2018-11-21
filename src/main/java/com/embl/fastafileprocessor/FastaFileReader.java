package com.embl.fastafileprocessor;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.embl.fastafileprocessor.exception.InternalException;
import com.embl.fastafileprocessor.exception.InvalidFastaFormatException;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FastaFileReader {

	private final GZipFiles gzipFiles;
	private ExecutorService taskExecutors;
	private final List<Future<Void>> taskFutures = new ArrayList<>();

	@Value("${app.message.internalerror}")
	private String internalErrorMessage;
	@Value("${tmp.fasta.filextension}")
	private String tmpFastaFileExtension = ".fasta.tmp";

	@Data
	@Builder
	public static class TaskInfo {
		private String seqIdentifier;
		private String seqBases;
		private Long fileId;
		private Long seqNumber;
		private Long seqOffsetInFile;
		private Integer seqLength;
	}
	
	@EqualsAndHashCode
	@RequiredArgsConstructor
	@Getter
	public static class OutputFastaFile implements Comparable<OutputFastaFile> {
		private final Long fileId;
		@EqualsAndHashCode.Exclude private final String outputFileName;
		@Override
		public int compareTo(OutputFastaFile other) {
			return this.getFileId().compareTo(other.getFileId());
		}
	}

	// This method could not be broken into smaller tasks for concurrency as it is a
	// sequential process. There is no way to know beforehand from which line a new sequence will start.
	// So one FASTA file need to be read by one thread which is I/O bound process.
	public OutputFastaFile read(final Path path, final Long fileId, final Consumer<TaskInfo> task) {
		final StringBuilder seqIdentifier = new StringBuilder();
		final StringBuilder seqBases = new StringBuilder(100_000);
		final AtomicLong currentFileOffset = new AtomicLong(0L);
		final AtomicLong seqOffsetInFile = new AtomicLong(0L);
		final AtomicLong seqNumber = new AtomicLong(0L);
		
		String outPutFileName = fileId + tmpFastaFileExtension;
		try(FileWriter tmpWriter = new FileWriter(outPutFileName)) {
			Stream<String> lines = gzipFiles.decompress(path);
			lines.forEach(line -> {
				currentFileOffset.addAndGet(line.getBytes().length + 1L);
				if (line.startsWith(">")) {
					if (seqBases.length() > 0) {
						TaskInfo taskInfo = TaskInfo.builder().seqIdentifier(seqIdentifier.toString())
								.seqBases(seqBases.toString()).fileId(fileId)
								.seqNumber(seqNumber.incrementAndGet()).seqOffsetInFile(seqOffsetInFile.get())
								.seqLength(seqBases.toString().getBytes().length).build();
						initDelegatedTask(task, taskInfo);
					}
					seqIdentifier.setLength(0);
					seqIdentifier.append(line.substring(1));
					seqBases.setLength(0);
					seqOffsetInFile.set(currentFileOffset.get());
				} else {
					if (seqIdentifier.length() == 0) {
						throw new InvalidFastaFormatException("Invalid FASTA formatted file: " + path.getFileName());
					}
					seqBases.append(line);
				}
				try {
					tmpWriter.write(line + "\n");
				} catch (IOException e) {
					throw new UncheckedIOException("Failed to write in tmp file.", e);
				}
			});
			lines.close();
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to write in tmp file.", e);
		}
		if (seqBases.length() > 0) {
			TaskInfo taskInfo = TaskInfo.builder().seqIdentifier(seqIdentifier.toString()).seqBases(seqBases.toString())
					.fileId(fileId).seqNumber(seqNumber.incrementAndGet())
					.seqOffsetInFile(seqOffsetInFile.get()).seqLength(seqBases.length()).build();
			initDelegatedTask(task, taskInfo);
		}
		checkValidFastaFormat(path);
		waitToFinish();
		return new OutputFastaFile(fileId, outPutFileName);
	}

	private void checkValidFastaFormat(final Path path) {
		if (taskFutures.isEmpty()) {
			throw new InvalidFastaFormatException("Invalid FASTA formatted file: " + path.getFileName());
		}
	}

	private void waitToFinish() {
		for (int i = 0; i < taskFutures.size(); i++) {
			try {
				taskFutures.get(i).get();
			} catch (InterruptedException | ExecutionException e) {
				Thread.currentThread().interrupt();
				throw new InternalException(internalErrorMessage, e);
			}
		}
	}

	private void initDelegatedTask(final Consumer<TaskInfo> task, final TaskInfo taskInfo) {
		Future<Void> taskFuture = CompletableFuture.runAsync(() -> task.accept(taskInfo), taskExecutors);
		taskFutures.add(taskFuture);
	}

	public void init(Integer threadNumber) {
		shutdownTaskExecutors();
		taskFutures.clear();
		// Number of worker threads set as per user input. 
		// But if user does not provide it then number of threads is equal to 
		// number of logical cores of the Host machine.
		this.taskExecutors = Executors.newFixedThreadPool(threadNumber); 
	}

	public void shutdownTaskExecutors() {
		if (this.taskExecutors != null) {
			taskExecutors.shutdownNow();
			try {
				taskExecutors.awaitTermination(30, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new InternalException(internalErrorMessage, e);
			}
			if (!taskExecutors.isTerminated()) {
				throw new InternalException(internalErrorMessage);
			}
		}
	}
}
