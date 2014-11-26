package com.elster.jupiter.orm;

import java.time.Instant;

public interface PartitionDropper {
	void drop(Instant upTo);
}
