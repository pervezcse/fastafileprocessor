package com.embl.fastafileprocessor;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.context.ActiveProfiles;
import com.embl.fastafileprocessor.ArgumentsValidator.Args;

@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles("test")
public class ArgumentsValidatorUnitTest {
	
	private static final String THREAD_NUMBER_16 = "--thread-number=16";
	private static final String SAMPLE_FILE = "src/test/resources/sample.fa.gz";
	private static final String SAMPLE_FILE_1 = "src/test/resources/sample1.fa.gz";
	private final ArgumentsValidator argumentsValidator = new ArgumentsValidator();
	
    @Test
    public void givenThreadNumberAndSingleFileNamePassed_validateArguments_returnsArgs() throws Exception {        
    	String args[] = {THREAD_NUMBER_16, SAMPLE_FILE};
    	Integer defaultThreadNumber = Runtime.getRuntime().availableProcessors();
    	
    	Args appArgs = argumentsValidator.validate(new DefaultApplicationArguments(args), defaultThreadNumber);
    	
    	assertThat(appArgs.getFileNames().size()).isEqualTo(1);
    	assertThat(appArgs.getFileNames().get(0)).isEqualTo(SAMPLE_FILE);
    	assertThat(appArgs.getThreadNumber()).isEqualTo(16);
    }
	
    @Test
    public void givenThreadNumberAndMultipleFileNamesPassed_validateArguments_returnsArgs() throws Exception {        
    	String args[] = {THREAD_NUMBER_16, SAMPLE_FILE, SAMPLE_FILE_1};
    	Integer defaultThreadNumber = Runtime.getRuntime().availableProcessors();
    	
    	Args appArgs = argumentsValidator.validate(new DefaultApplicationArguments(args), defaultThreadNumber);
    	
    	assertThat(appArgs.getFileNames().size()).isEqualTo(2);
    	assertThat(appArgs.getFileNames().get(0)).isEqualTo(SAMPLE_FILE);
    	assertThat(appArgs.getFileNames().get(1)).isEqualTo(SAMPLE_FILE_1);
    	assertThat(appArgs.getThreadNumber()).isEqualTo(16);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void givenThreadNumberAndMissingFilePassed_validateArguments_throwsIllegalArgumentException() throws Exception {        
    	String args[] = {THREAD_NUMBER_16, "src/test/resources/MissingSample.fa.gz"};
    	Integer defaultThreadNumber = Runtime.getRuntime().availableProcessors();
    	
    	argumentsValidator.validate(new DefaultApplicationArguments(args), defaultThreadNumber);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void givenThreadNumberAndDuplicateFileNamesPassed_validateArguments_throwsIllegalArgumentException() throws Exception {        
    	String args[] = {THREAD_NUMBER_16, SAMPLE_FILE, SAMPLE_FILE};
    	Integer defaultThreadNumber = Runtime.getRuntime().availableProcessors();
    	
    	argumentsValidator.validate(new DefaultApplicationArguments(args), defaultThreadNumber);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void givenFileNameNotPassed_validateArguments_throwsIllegalArgumentException() throws Exception {        
    	String args[] = {"--k", "--i"};
    	Integer defaultThreadNumber = Runtime.getRuntime().availableProcessors();
    	
    	argumentsValidator.validate(new DefaultApplicationArguments(args), defaultThreadNumber);
    }

    @Test(expected=IllegalArgumentException.class)
    public void givenThreadNumberValueNotPassed_validateArguments_throwsIllegalArgumentException() throws Exception {        
    	String args[] = {"--thread-number", SAMPLE_FILE};
    	Integer defaultThreadNumber = Runtime.getRuntime().availableProcessors();
    	
    	argumentsValidator.validate(new DefaultApplicationArguments(args), defaultThreadNumber);
    }
}
