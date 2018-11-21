package com.embl.fastafileprocessor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.embl.fastafileprocessor.FastaFileReader.OutputFastaFile;
import com.embl.fastafileprocessor.FastaFileReader.TaskInfo;
import com.embl.fastafileprocessor.exception.InternalException;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SequenceProcessor {

	@Value("${app.message.internalerror}")
	private String internalErrorMessage;

	private final FastaFileReader fastaFileReader;
	private final ReportFileWriter reportFileWriter;
	private final ConcatenatedFastaFileWriter concatenatedFastaFileWriter;
	private final List<Future<Void>> fileFutures = new ArrayList<>();
	private ExecutorService fileExecutors;

	private Integer fileNumber = 0;
	private final AtomicLong totalSequenceNumber = new AtomicLong(0);
	private final AtomicLong totalSequenceBaseNumber = new AtomicLong(0);
	private final Map<String, Long> sequesnceBaseNumberMap = new ConcurrentHashMap<>();
	// The following data structure is used to store all sequences' positions/offsets in files along with their lengths.
	// These info will be used later to concatenate sequences read directly from disk instead of storing in ram 
	// which is required for processing large files.
	private final Map<Long, Map<Long, SequenceInfo>> seqNumberFileIdSeqInfoMap = new ConcurrentHashMap<>();

	@Data
	@RequiredArgsConstructor
	public class SequenceInfo {
		private final Long seqOffsetInFile;
		private final Integer seqLength;
	}

	public Boolean process(List<String> fileNames, int threadNumber) {
		init(threadNumber);
		fileNumber = fileNames.size();
		Set<OutputFastaFile> fastaFiles = new ConcurrentSkipListSet<>();
		for (int i = 0; i < fileNames.size(); i++) {
			Path filePath = Paths.get(fileNames.get(i));
			final long fileId = i + 1L;
			Future<Void> fileProcessor = CompletableFuture
					.supplyAsync(() -> fastaFileReader.read(filePath, fileId, reportTask()), fileExecutors)
					.thenAccept(fastaFiles::add);
			fileFutures.add(fileProcessor);
		}
		waitToFinish();
		generateResult(fastaFiles);
		cleanUp(fastaFiles);
		return true;
	}

	// This method is a CPU bound task.
	private Consumer<TaskInfo> reportTask() {
		return (taskInfo) -> {
			totalSequenceNumber.incrementAndGet();
			totalSequenceBaseNumber.addAndGet(taskInfo.getSeqLength());
			for (int i = 0; i < taskInfo.getSeqLength(); i++) {
				String key = String.valueOf(taskInfo.getSeqBases().charAt(i));
				sequesnceBaseNumberMap.merge(key, 1L, (oldValue, newValue) -> oldValue + newValue);
			}
			Long seqNum = taskInfo.getSeqNumber();
			Long fileId = taskInfo.getFileId();
			SequenceInfo seqInfo = new SequenceInfo(taskInfo.getSeqOffsetInFile(), taskInfo.getSeqLength());
			seqNumberFileIdSeqInfoMap.compute(seqNum, (k, v) -> {
				if (v == null) {
					v = new ConcurrentHashMap<>();
				}
				v.put(fileId, seqInfo);
				return v;
			});
		};
	}
	
	private void generateResult(Set<OutputFastaFile> fastaFiles) {
		reportFileWriter.write(fileNumber, totalSequenceNumber.get(), totalSequenceBaseNumber.get(),
				sequesnceBaseNumberMap);
		concatenatedFastaFileWriter.write(fastaFiles, seqNumberFileIdSeqInfoMap);
	}

	private void waitToFinish() {
		for (int i = 0; i < fileFutures.size(); i++) {
			try {
				fileFutures.get(i).get();
			} catch (InterruptedException | ExecutionException e) {
				Thread.currentThread().interrupt();
				throw new InternalException("Internal error. Please restart the program.", e);
			}
		}
	}
	
	private void cleanUp(Set<OutputFastaFile> fastaFiles) {
		for(OutputFastaFile fastaFile: fastaFiles) {
			try {
				if (Files.exists(Paths.get(fastaFile.getOutputFileName()))) {
					Files.delete(Paths.get(fastaFile.getOutputFileName()));
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		shutdownFileExecutors();
		fastaFileReader.shutdownTaskExecutors();
	}

	private void shutdownFileExecutors() {
		if (this.fileExecutors != null) {
			fileExecutors.shutdownNow();
			try {
				fileExecutors.awaitTermination(30, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new InternalException(internalErrorMessage, e);
			}
			if (!fileExecutors.isTerminated()) {
				throw new InternalException(internalErrorMessage);
			}
		}
	}

	private void init(int threadNumber) {
		shutdownFileExecutors();
		fileFutures.clear();
		fastaFileReader.init(threadNumber);
		fileNumber = 0;
		totalSequenceNumber.getAndSet(0);
		totalSequenceBaseNumber.getAndSet(0);
		sequesnceBaseNumberMap.clear();
		seqNumberFileIdSeqInfoMap.clear();
		fileExecutors = Executors.newFixedThreadPool(2); // Number of file reader threads set to 2.
	}
}
