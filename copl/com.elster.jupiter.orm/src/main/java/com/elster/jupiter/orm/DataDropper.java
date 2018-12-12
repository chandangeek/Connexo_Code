/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/*
 * Data LifeCycle interface.
 */
@ProviderType
public interface DataDropper {
	void drop(Instant upTo);
}
