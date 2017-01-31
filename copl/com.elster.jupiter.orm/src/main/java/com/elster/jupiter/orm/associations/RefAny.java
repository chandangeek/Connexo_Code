/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.associations;

/*
 * a reference to a persistent Object of unknown type
 */

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface RefAny {
	boolean isPresent();
	Object get();
	Optional<?> getOptional();
	String getComponent();
	String getTableName();
	Object[] getPrimaryKey();
}
