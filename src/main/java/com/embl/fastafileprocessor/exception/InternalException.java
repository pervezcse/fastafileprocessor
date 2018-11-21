package com.embl.fastafileprocessor.exception;

public class InternalException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InternalException(String message, Throwable ex) {
		super(message, ex);
	}
	
	public InternalException(String message) {
		super(message);
	}
}
