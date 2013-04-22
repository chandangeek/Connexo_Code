package com.elster.jupiter.orm.impl;

enum TableConstraintType {

	PRIMARYKEY ("primary key") {
		@Override
		boolean isPrimaryKey() {
			return true;
		}
	},
	UNIQUE ("unique") {
		@Override
		boolean isUnique() {
			return true;
		}
	},
	FOREIGNKEY ("foreign key") {
		@Override
		boolean isForeignKey() {
			return true;
		}
		
		@Override
		boolean hasAutoIndex() {
			return false;
		}
	};
	
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
	
	boolean hasAutoIndex() {
		return true;
	}

}

