package com.embl.fastafileprocessor.exception;

public class InvalidFastaFormatException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidFastaFormatException(String message, Throwable ex) {
		super(message, ex);
	}

	public InvalidFastaFormatException(String message) {
		super(message);
	}
}
