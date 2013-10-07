package com.elster.jupiter.orm;

import java.util.Date;

import com.elster.jupiter.util.time.UtcInstant;

public final class JournalEntry<T> implements Comparable<JournalEntry<T>> {

	final private UtcInstant journalTime;
	final private T value;
	
	public JournalEntry(UtcInstant journalTime,T value) {
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
