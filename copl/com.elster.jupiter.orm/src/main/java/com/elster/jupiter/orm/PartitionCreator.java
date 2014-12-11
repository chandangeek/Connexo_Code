package com.elster.jupiter.orm;

import java.time.Instant;

/*
 * creates partitions on a table
 */
public interface PartitionCreator {
	Instant create(Instant upTo);
}
