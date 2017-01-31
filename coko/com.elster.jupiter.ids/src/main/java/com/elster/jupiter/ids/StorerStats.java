/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids;

public interface StorerStats {
	int getEntryCount();
	int getInsertCount();
	int getUpdateCount();
	long getExecuteTime();
}
