package com.elster.jupiter.orm.impl;

enum TableConstraintType {

	PRIMARYKEY ("primary key") { boolean isPrimaryKey() {return true;}},
	UNIQUE ("unique") { boolean isUnique() {return true;}},
	FOREIGNKEY ("foreign key") { boolean isForeignKey() {return true;}};
	
	final private String ddl;
	
	private TableConstraintType( String ddl) {
		this.ddl = ddl;
	}
	
	boolean isPrimaryKey() {
		return false;
	}

	boolean isUnique() {
		return false;
	}

	boolean isForeignKey() {
		return false;
	}
	
	String getDdl() {
		return ddl;
	}

}

