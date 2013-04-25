package com.elster.jupiter.orm.impl;

import com.elster.jupiter.sql.util.SqlFragment;

abstract class AliasFragment implements SqlFragment {
	
	final private String alias;
	
	AliasFragment(String alias) {
		this.alias = alias;
	}
	
	String getAlias() {
		return alias;
	}
}
