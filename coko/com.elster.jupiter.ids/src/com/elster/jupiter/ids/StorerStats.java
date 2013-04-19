package com.elster.jupiter.ids;

public interface StorerStats {
	int getEntryCount();
	int getInsertCount();
	int getUpdateCount();
	long getExecuteTime();
}
