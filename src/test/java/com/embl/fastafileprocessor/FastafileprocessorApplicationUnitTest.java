package com.embl.fastafileprocessor;

import static org.mockito.BDDMockito.given;
import static com.embl.fastafileprocessor.ArgumentsValidator.Args;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.context.ActiveProfiles;

@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles("test")
public class FastafileprocessorApplicationUnitTest {

	@Mock
	private ArgumentsValidator argumentsValidator;
	@Mock
	private SequenceProcessor sequenceProcessor;
	
	FastafileprocessorApplication application;
	
    @Before
    public void setUp() throws Exception {
    	application = new FastafileprocessorApplication(argumentsValidator, sequenceProcessor);
    }
	
    @Test
    public void givenArgumentValidatorAndFileProcessor_appRunnerRuns() throws Exception {
    	String args[] = {"--thread-number=16", "sample.fa.gz"};
    	ApplicationArguments arguments = new DefaultApplicationArguments(args);
    	List<String> fileNames = new ArrayList<>(Arrays.asList("sample.fa.gz"));
    	Integer defaultThreadNumber = Runtime.getRuntime().availableProcessors();
    	given(argumentsValidator.validate(arguments, defaultThreadNumber)).willReturn(new Args(fileNames, 16));
    	given(sequenceProcessor.process(fileNames, 16)).willReturn(true);
    	
    	application.appRunner().run(arguments);
    }
}
