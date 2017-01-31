/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

/*
 * enumerates the three delete rules for a foreign key constraint
 */

public enum DeleteRule {
	RESTRICT  (""),
	SETNULL (" on delete set null"),
	CASCADE (" on delete cascade");
	
	private final String ddl;
	
	private DeleteRule(String ddl) {
		this.ddl = ddl;
	}
	
	public String getDdl() {
		return ddl;
	}
}
