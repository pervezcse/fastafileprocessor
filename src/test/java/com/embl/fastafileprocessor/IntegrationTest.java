package com.embl.fastafileprocessor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class IntegrationTest {

	private SequenceProcessor sequenceProcessor;
	private FastaFileReader fastaFileReader;
	private ReportFileWriter reportFileWriter;
	private ConcatenatedFastaFileWriter concatenatedFastaFileWriter; 
	private GZipFiles gzipFiles;
	
    @Before
    public void setUp() throws Exception {
    	gzipFiles = new GZipFiles();
    	fastaFileReader = new FastaFileReader(gzipFiles);
    	reportFileWriter = new ReportFileWriter();
    	concatenatedFastaFileWriter = new ConcatenatedFastaFileWriter();
    	sequenceProcessor = new SequenceProcessor(fastaFileReader, reportFileWriter, concatenatedFastaFileWriter);
    }
	
    @Test
    public void processFastaFile_createsReportFile() {
    	List<String> fileNames = new ArrayList<>(Arrays.asList("src/test/resources/sample.fa.gz"));
		int threadNumber = 16;
		
		Boolean success = sequenceProcessor.process(fileNames, threadNumber);
    	
		assertThat(success).isTrue();
    }
    
    @Test
    public void processMultipleFastaFiles_createsReportFile() {
    	List<String> fileNames = new ArrayList<>(Arrays.asList("src/test/resources/sample.fa.gz", 
    			"src/test/resources/sample1.fa.gz"));
		int threadNumber = 16;
		
		Boolean success = sequenceProcessor.process(fileNames, threadNumber);
    	
		assertThat(success).isTrue();
    }
}
