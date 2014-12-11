package com.elster.jupiter.orm;

public enum SqlDialect {
	/*
	 * H2 Database
	 */
    H2 {
    	@Override
    	public String rowId() {
    		return "_ROWID_";
    	}
    	
    	@Override
    	public boolean hasPartitioning() {
    		return false;
    	}

		@Override
		public boolean hasIndexOrganizedTables() {
			return false;
		}

		@Override
		public boolean hasIndexCompression() {
			return false;
		}
    }, 
    /*
     * Oracle Enterprise Edition with partitioning option
     */
    ORACLE_EE {
    	@Override
    	public String rowId() {
    		return "ROWID";
    	}
    	
    	@Override
    	public boolean hasPartitioning() {
    		return true;
    	}

		@Override
		public boolean hasIndexOrganizedTables() {
			return true;
		}

		@Override
		public boolean hasIndexCompression() {
			return true;
		}
    },
    /*
     * Oracle Standard Edition
     */
    ORACLE_SE {
    	@Override
    	public String rowId() {
    		return "ROWID";
    	}
    	
    	@Override
    	public boolean hasPartitioning() {
    		return false;
    	}

		@Override
		public boolean hasIndexOrganizedTables() {
			return true;
		}

		@Override
		public boolean hasIndexCompression() {
			return true;
		}
    };
    
    abstract public String rowId();
    
    abstract public boolean hasPartitioning();
    
    abstract public boolean hasIndexOrganizedTables();
    
    abstract public boolean hasIndexCompression();
}
