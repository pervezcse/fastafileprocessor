package com.embl.fastafileprocessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;
import com.embl.fastafileprocessor.FastaFileReader.OutputFastaFile;

@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles("test")
public class SequenceProcessorUnitTest {
	
	@Mock
	private FastaFileReader fastaFileReader;
	@Mock
	private ReportFileWriter reportFileWriter;
	@Mock
	private ConcatenatedFastaFileWriter concatenatedFastaFileWriter;
	
	SequenceProcessor sequenceProcessor;
	
    @Before
    public void setUp() throws Exception {
    	sequenceProcessor = new SequenceProcessor(fastaFileReader, reportFileWriter, concatenatedFastaFileWriter);
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void givenValidFastaFilesAndSuccessfulReportWrite_process_returnsTrue() {
    	List<String> fileNames = new ArrayList<>(Arrays.asList("src/test/resources/sample.fa.gz", 
    			"src/test/resources/sample1.fa.gz"));
		int threadNumber = 16;
		given(fastaFileReader.read(any(Path.class), anyLong(), any(Consumer.class)))
				.willReturn(new OutputFastaFile(1L, "sample.fa"));
		    	
    	Boolean status = sequenceProcessor.process(fileNames, threadNumber);
    	assertThat(status).isTrue();
    }
}
