package com.elster.jupiter.orm;

/**
 * Thrown when an update using optimistic locking fails, because another process has done an update between this process' read and write.
 */
public class OptimisticLockException extends PersistenceException {
	
	private static final long serialVersionUID = 1;
	
	public OptimisticLockException() {
		super(ExceptionTypes.OPTIMISTIC_LOCK, "Optimistic lock failed.");
	}
	
}
