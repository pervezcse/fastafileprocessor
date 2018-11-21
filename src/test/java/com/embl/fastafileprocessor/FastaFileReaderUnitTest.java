package com.embl.fastafileprocessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;
import com.embl.fastafileprocessor.FastaFileReader.OutputFastaFile;
import com.embl.fastafileprocessor.exception.InvalidFastaFormatException;

@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles("test")
public class FastaFileReaderUnitTest {

	@Mock
	private GZipFiles gZipFiles;
	
	FastaFileReader fastaFileReader;
	
    @Before
    public void setUp() throws Exception {
    	fastaFileReader = new FastaFileReader(gZipFiles);
    }
	
	@Test(expected=InvalidFastaFormatException.class)
    public void givenValidValidReportData_write_createsReportFile() {
		String[] data = {"derice", "ACTGACTAGCTAGCTAACTG", ">sanka", "GCATCGTAGCTAGCTACGAT"};
		given(gZipFiles.decompress(any(Path.class))).willReturn(Arrays.asList(data).stream());
		
		OutputFastaFile output = fastaFileReader.read(Paths.get("sample.fa.gz"), 1L, t -> {});
		
		assertThat(output.getFileId()).isEqualTo(1L);
		assertThat(output.getOutputFileName()).startsWith("1");
    }
}
