package com.elster.jupiter.orm;

import java.time.Instant;

public interface PartitionCreator {
	Instant create(Instant upTo);
}
