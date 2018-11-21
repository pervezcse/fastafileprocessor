package com.embl.fastafileprocessor;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;

import com.embl.fastafileprocessor.exception.InvalidGzipFormatException;

@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles("test")
public class GZipFilesUnitTest {

	GZipFiles gzipFiles;
	
    @Before
    public void setUp() throws Exception {
    	gzipFiles = new GZipFiles();
    }
    
    @Test
    public void givenValidGZippedFile_decompress_returnsLinesOfString() throws Exception {        
    	Path path = Paths.get("src/test/resources/sample.fa.gz");
    	
    	Stream<String> lines = gzipFiles.decompress(path);
    	
    	assertThat(lines.count()).isPositive();    	
    }
    
    @Test(expected=InvalidGzipFormatException.class)
    public void givenInvalidGZippedFile_decompress_returnsLinesOfString() throws Exception {        
    	Path path = Paths.get("src/test/resources/sample.fa");
    	
    	Stream<String> lines = gzipFiles.decompress(path);
    	
    	assertThat(lines.count()).isPositive();    	
    }
}
