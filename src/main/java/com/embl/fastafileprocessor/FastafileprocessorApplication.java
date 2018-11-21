package com.embl.fastafileprocessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.embl.fastafileprocessor.ArgumentsValidator.Args;
import lombok.RequiredArgsConstructor;

@SpringBootApplication
@RequiredArgsConstructor
public class FastafileprocessorApplication {

	private final ArgumentsValidator argumentsValidator;
	private final SequenceProcessor sequenceProcessor;
	private final Logger log = LoggerFactory.getLogger("userlog." + FastafileprocessorApplication.class.getName());

	@Value("${app.message.intro}")
	private String appMsgIntro;

	public static void main(String[] args) {
		SpringApplication.run(FastafileprocessorApplication.class, args);
	}

	@Bean
	ApplicationRunner appRunner() {
		return arguments -> {
			log.info(appMsgIntro);
			log.info("OPERATION IS STARTING...");
			Integer defaultThreadNumber = Runtime.getRuntime().availableProcessors();
			Args args = argumentsValidator.validate(arguments, defaultThreadNumber);
			Boolean status = sequenceProcessor.process(args.getFileNames(), args.getThreadNumber());
			log.info("OPERATION IS FINISHED. STATUS: {}", status);
		};
	}
}
