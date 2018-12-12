/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.query.impl;

import java.util.HashSet;
import java.util.Set;

public class AliasFactory {
	private final Set<String> aliases = new HashSet<>();
	private String base;
	
	void setBase(String alias) {
		this.base = alias;
	}	
	
	
	String getAlias() {
		return getAlias(base);
	}
		
	private String getAlias(String base) {
		String result = base;
		int i = 2;
		while (aliases.contains(result)) {
			result = base + i++;						
		}
		aliases.add(result);
		return result;
	}
	
}
