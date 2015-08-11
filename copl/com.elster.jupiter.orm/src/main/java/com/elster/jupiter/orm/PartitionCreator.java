package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/*
 * Creates partitions on a table.
 */
@ProviderType
public interface PartitionCreator {
	Instant create(Instant upTo);
}
