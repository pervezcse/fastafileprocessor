package com.embl.fastafileprocessor;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.embl.fastafileprocessor.FastaFileReader.OutputFastaFile;
import com.embl.fastafileprocessor.SequenceProcessor.SequenceInfo;

@Component
public class ConcatenatedFastaFileWriter {
	
	@Value("${concat.filename}")
	private String fileName = "SEQUENCE.FASTA.GZ";
	
	public void write(Set<OutputFastaFile> fastaFiles, Map<Long, Map<Long, SequenceInfo>> seqNumberFileIdSeqInfoMap) {
		List<FileInputStream> inputStreams = new ArrayList<>();
		OutputStream outputStream = null;
		WritableByteChannel outChannel = null;
		try {			
			for (OutputFastaFile fastaFile: fastaFiles) {
				inputStreams.add(new FileInputStream(fastaFile.getOutputFileName()));
			}
			outputStream = new GZIPOutputStream(new FileOutputStream(fileName));
			outChannel = Channels.newChannel(outputStream);
			for (int i = 0; i < seqNumberFileIdSeqInfoMap.size(); i++) {
				long seqNum = i + 1L;
				String seqId = String.format(">%d%n", seqNum);
				outputStream.write(seqId.getBytes());
				for (int cCount = 0; cCount < inputStreams.size(); cCount++) {
					long fileId = cCount + 1L;
					Map<Long, SequenceInfo> fileIdSeqInfoMap = seqNumberFileIdSeqInfoMap.get(seqNum);
					if(fileIdSeqInfoMap.containsKey(fileId)) {
						SequenceInfo sequenceInfo = fileIdSeqInfoMap.get(fileId);
						FileChannel inputChannel = inputStreams.get(cCount).getChannel();
						inputChannel.transferTo(sequenceInfo.getSeqOffsetInFile(), sequenceInfo.getSeqLength(), outChannel);
					}
				}
				if(i < seqNumberFileIdSeqInfoMap.size()-1) {
					outputStream.write("\n".getBytes());
				}
			}
		} catch (IOException ex) {
			throw new UncheckedIOException("Error in writing concatenated FASTA file.", ex);
		} finally {
			closeSafely(outChannel);
			closeSafely(outputStream);
			for(FileInputStream inputStream: inputStreams) {
				closeSafely(inputStream);
			}
		}
	}
	
	private void closeSafely(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				throw new UncheckedIOException("Error in closing file.", e);
			}
		}
	}
}
