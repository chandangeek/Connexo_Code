/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import java.time.Instant;

/**
 * represents a version of an object instance that may be no longer current
 */
public final class JournalEntry<T> implements Comparable<JournalEntry<T>> {

	private final Instant journalTime;
	private final T value;
	
	public JournalEntry(Instant journalTime, T value) {
		this.journalTime = journalTime;
		this.value = value;
	}
	
	/**
	 * @since 1.1
	 */
	public JournalEntry(T value) {
		this.journalTime = Instant.MAX;
		this.value = value;
	}
	
	public Instant getJournalTime() {
		if (isCurrent()) {
			throw new IllegalStateException();
		}
		return journalTime;
	}
	
	public T get() {
		return value;
	}

	@Override
	public int compareTo(JournalEntry<T> o) {
		return journalTime.compareTo(o.journalTime);
	}
	
	/**
	 * @since 1.1
	 */
	public boolean isCurrent() {
		return journalTime.equals(Instant.MAX);
	}
}
