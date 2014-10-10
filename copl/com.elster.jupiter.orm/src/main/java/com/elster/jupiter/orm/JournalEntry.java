package com.elster.jupiter.orm;

import java.time.Instant;

public final class JournalEntry<T> implements Comparable<JournalEntry<T>> {

	private final Instant journalTime;
	private final T value;
	
	public JournalEntry(Instant journalTime, T value) {
		this.journalTime = journalTime;
		this.value = value;
	}
	
	public Instant getJournalTime() {
		return journalTime;
	}
	
	public T get() {
		return value;
	}

	@Override
	public int compareTo(JournalEntry<T> o) {	
		return journalTime.compareTo(o.journalTime);
	}
}
