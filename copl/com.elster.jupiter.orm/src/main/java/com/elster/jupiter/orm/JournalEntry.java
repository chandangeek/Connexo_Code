package com.elster.jupiter.orm;

import com.elster.jupiter.util.time.UtcInstant;

import java.util.Date;

public final class JournalEntry<T> implements Comparable<JournalEntry<T>> {

	private final UtcInstant journalTime;
	private final T value;
	
	public JournalEntry(UtcInstant journalTime, T value) {
		this.journalTime = journalTime;
		this.value = value;
	}
	
	public Date getJournalTime() {
		return journalTime.toDate();
	}
	
	public T get() {
		return value;
	}

	@Override
	public int compareTo(JournalEntry<T> o) {	
		return journalTime.compareTo(o.journalTime);
	}
}
