package com.embl.fastafileprocessor.exception;

public class InvalidGzipFormatException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidGzipFormatException(String message, Throwable ex) {
		super(message, ex);
	}
	
	public InvalidGzipFormatException(String message) {
		super(message);
	}
}
