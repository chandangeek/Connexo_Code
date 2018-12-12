/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.fields.impl;

import com.elster.jupiter.util.sql.SqlFragment;

abstract class AliasFragment implements SqlFragment {
	
	private final String alias;
	
	AliasFragment(String alias) {
		this.alias = alias;
	}
	
	String getAlias() {
		return alias;
	}
}
