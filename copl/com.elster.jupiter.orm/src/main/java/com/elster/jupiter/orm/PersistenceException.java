package com.elster.jupiter.orm;

public class PersistenceException extends RuntimeException {
	
	private static final long serialVersionUID = 1;
	
	public PersistenceException() {		
	}
	
	public PersistenceException(Exception ex) {
		super(ex);
	}
	
	public PersistenceException(String reason) {
		super(reason);
	}
	
}
