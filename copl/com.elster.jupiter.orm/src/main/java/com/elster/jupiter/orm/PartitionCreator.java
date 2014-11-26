package com.elster.jupiter.orm;

import java.time.Instant;

public interface PartitionCreator {
	void create(Instant upTo);
}
