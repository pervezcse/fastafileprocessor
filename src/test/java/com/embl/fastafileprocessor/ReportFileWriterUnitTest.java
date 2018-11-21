package com.embl.fastafileprocessor;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;

@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles("test")
public class ReportFileWriterUnitTest {

	ReportFileWriter reportFileWriter;
	
    @Before
    public void setUp() throws Exception {
    	reportFileWriter = new ReportFileWriter();
    }
	
	@Test
    public void givenValidValidReportData_write_createsReportFile() {
		Integer fileNumber = 2;
		long totalSequenceNumber = 10;
		long totalSequenceBaseNumber = 300;
		Map<String, Long> sequesnceBaseNumberMap = new ConcurrentHashMap<>();
		sequesnceBaseNumberMap.put("A", 5L);
		sequesnceBaseNumberMap.put("M", 15L);
		sequesnceBaseNumberMap.put("G", 16L);
		
		reportFileWriter.write(fileNumber, totalSequenceNumber, totalSequenceBaseNumber, sequesnceBaseNumberMap);
		
		assertThat(Files.exists(Paths.get(reportFileWriter.getFileName()))).isTrue();
    }
	
}
