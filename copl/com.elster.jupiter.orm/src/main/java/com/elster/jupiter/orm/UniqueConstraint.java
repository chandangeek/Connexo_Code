/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

/*
 * Models a unique constraint.
 */
@ProviderType
public interface UniqueConstraint extends TableConstraint {
	@ProviderType
	interface Builder {
		Builder on(Column ... columns);
		UniqueConstraint add();

		Builder since(Version version);

		Builder upTo(Version version);

		Builder during(Range... ranges);

		Builder previously(UniqueConstraint uniqueConstraint);
	}
}
