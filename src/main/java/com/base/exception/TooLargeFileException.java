package com.base.exception;

public class TooLargeFileException extends RuntimeException{

	public TooLargeFileException(String msg) {
		super(msg);
	}
}
