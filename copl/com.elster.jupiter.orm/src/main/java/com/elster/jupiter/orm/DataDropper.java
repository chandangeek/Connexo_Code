package com.elster.jupiter.orm;

import java.time.Instant;

/*
 * Data LifeCycle interface 
 */

public interface DataDropper {
	void drop(Instant upTo);
}
