package com.embl.fastafileprocessor;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.embl.fastafileprocessor.exception.InvalidGzipFormatException;

@Component
public class GZipFiles {

	private final Logger log = LoggerFactory.getLogger("userlog." + GZipFiles.class.getName());
	
	public Stream<String> decompress(Path path) {
	    InputStream fileIs = null;
	    BufferedInputStream bufferedIs = null;
	    GZIPInputStream gzipIs = null;
		try {
			fileIs = Files.newInputStream(path);
			bufferedIs = new BufferedInputStream(fileIs, 64 * 1024);
			gzipIs = new GZIPInputStream(bufferedIs);
		} catch (IOException e) {
			closeSafely(gzipIs);
			closeSafely(bufferedIs);
			closeSafely(fileIs);
			throw new InvalidGzipFormatException("Failed to decompress file:" + path.getFileName(), e);
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(gzipIs));
		// Java 8 can read lines lazily from files using the Files.lines method. Useful for large file processing.
		return reader.lines().onClose(() -> closeSafely(reader));
	}

	private void closeSafely(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
				// ignore re-throwing exception.
			}
		}
	}
}
