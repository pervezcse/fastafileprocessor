package com.embl.fastafileprocessor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
public class ReportFileWriter {

	@Value("${report.filename}")
	private String fileName = "REPORT.TXT";

	public void write(Integer fileNumber, long totalSequenceNumber, long totalSequenceBaseNumber,
			Map<String, Long> sequesnceBaseNumberMap) {
		String content = buildContent(fileNumber, totalSequenceNumber, totalSequenceBaseNumber, sequesnceBaseNumberMap);
		Path path = Paths.get(fileName);
		byte[] contentBytes = content.getBytes();
		try {
			Files.write(path, contentBytes);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private String buildContent(Integer fileNumber, long totalSequenceNumber, long totalSequenceBaseNumber,
			Map<String, Long> sequesnceBaseNumberMap) {
		List<String> lines = new ArrayList<>();
		lines.add(String.format("FILE_CNT\t%d", fileNumber));
		lines.add(String.format("SEQUENCE_CNT\t%d", totalSequenceNumber));
		lines.add(String.format("BASE_CNT\t%d", totalSequenceBaseNumber));
		sequesnceBaseNumberMap.entrySet().forEach(e -> lines.add(String.format("%s\t%d", e.getKey(), e.getValue())));
		return StringUtils.join(lines, "\n");
	}
}
