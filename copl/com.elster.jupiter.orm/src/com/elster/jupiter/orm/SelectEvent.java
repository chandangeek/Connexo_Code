package com.elster.jupiter.orm;

public interface SelectEvent {
	long getElapsed();
	long getCpu();
	String getText();
	int getRowCount();
}
