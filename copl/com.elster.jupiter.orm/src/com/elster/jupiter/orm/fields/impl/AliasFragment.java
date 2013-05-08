package com.elster.jupiter.orm.fields.impl;

import com.elster.jupiter.util.sql.SqlFragment;

abstract class AliasFragment implements SqlFragment {
	
	final private String alias;
	
	AliasFragment(String alias) {
		this.alias = alias;
	}
	
	String getAlias() {
		return alias;
	}
}
