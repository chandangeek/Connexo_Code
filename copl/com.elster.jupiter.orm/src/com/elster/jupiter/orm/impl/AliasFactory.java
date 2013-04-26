package com.elster.jupiter.orm.impl;

import java.util.HashSet;
import java.util.Set;

public class AliasFactory {
	final private Set<String> aliases = new HashSet<>();
	private String base;
	
	void setBase(String alias) {
		this.base = alias;
	}	
	
	String getAlias() {
		return getAlias(false);
	}
	
	String getAlias(boolean current) {
		return current ? getAlias("c" + base) : getAlias(base);
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
