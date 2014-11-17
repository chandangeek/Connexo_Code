package com.elster.jupiter.orm;

public enum SqlDialect {
    H2 {
    	@Override
    	public String rowId() {
    		return "_ROWID_";
    	}
    }, 
    ORACLE {
    	@Override
    	public String rowId() {
    		return "ROWID";
    	}
    };
    
    abstract public String rowId();
}
