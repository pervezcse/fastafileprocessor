package com.embl.fastafileprocessor;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Component
public class ArgumentsValidator {

	@Data
	@RequiredArgsConstructor
	public static class Args {
		private final List<String> fileNames;
		private final Integer threadNumber;
	}

	public Args validate(ApplicationArguments arguments, Integer defaultThreadNumber) {
		final List<String> fileNames = arguments.getNonOptionArgs();
		checkFileNamesProvided(fileNames);
		checkMissingFiles(fileNames);
		checkDuplicateFiles(fileNames);
		int threadNumber = validateThreadNumber(arguments, defaultThreadNumber);
		return new Args(fileNames, threadNumber);
	}

	private void checkMissingFiles(List<String> fileNames) {
		List<String> missingFiles = fileNames.stream().filter(f -> !Files.exists(Paths.get(f)))
				.collect(Collectors.toList());
		if (!missingFiles.isEmpty()) {
			throw new IllegalArgumentException("Files not found: " + StringUtils.join(missingFiles, ','));
		}
	}

	private void checkFileNamesProvided(List<String> fileNames) {
		if (fileNames.isEmpty()) {
			throw new IllegalArgumentException("no gzipped FASTA file found.");
		}
	}

	private void checkDuplicateFiles(List<String> fileNames) {
		Set<String> allItems = new HashSet<>();
		Set<String> duplicateFiles = fileNames.stream().filter(n -> !allItems.add(n)).collect(Collectors.toSet());
		if (!duplicateFiles.isEmpty()) {
			throw new IllegalArgumentException("duplicate file name found: " + StringUtils.join(duplicateFiles, ','));
		}
	}

	private int validateThreadNumber(ApplicationArguments arguments, Integer defaultThreadNumer) {
		if (arguments.containsOption("thread-number")) {
			final List<String> threadNumberOptions = arguments.getOptionValues("thread-number");
			if (threadNumberOptions.isEmpty()) {
				throw new IllegalArgumentException("Option '--thread-number' must have a positive number as value.");
			}
			String threadNumberStr = threadNumberOptions.get(0);
			if (threadNumberOptions.size() > 1) {
				throw new IllegalArgumentException("Duplicate --thread-number options found.");
			} else if (!StringUtils.isNumeric(threadNumberStr) && Integer.parseInt(threadNumberStr) > 0) {
				throw new IllegalArgumentException("Option '--thread-number' must have a positive number as value.");
			}
			return Integer.parseInt(threadNumberStr);
		}
		return defaultThreadNumer;
	}
}
