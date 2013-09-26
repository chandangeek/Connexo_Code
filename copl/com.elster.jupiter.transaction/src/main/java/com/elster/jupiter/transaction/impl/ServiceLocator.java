package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.transaction.SqlEvent;

public interface ServiceLocator {
	void publish(SqlEvent event);
}
