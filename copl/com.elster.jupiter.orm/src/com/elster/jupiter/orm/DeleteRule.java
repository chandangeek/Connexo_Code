package com.elster.jupiter.orm;

public enum DeleteRule {
	RESTRICT  (""),
	SETNULL (" on delete set null"),
	CASCADE (" on delete cascade");
	
	final private String ddl;
	
	private DeleteRule(String ddl) {
		this.ddl = ddl;
	}
	
	public String getDdl() {
		return ddl;
	}
}
