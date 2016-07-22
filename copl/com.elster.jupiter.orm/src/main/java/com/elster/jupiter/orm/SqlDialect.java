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

		@Override
		public String renameColumnSyntax() {
			return "ALTER TABLE {0} ALTER COLUMN {1} RENAME TO {2}";
		}

        @Override
        public boolean allowsConstraintRename() {
            return false;
        }

		@Override
		public String leftPad(String field, int zerofillSize, String padCharacter) {
			return "lpad(" + field + "," + zerofillSize + ",'" + padCharacter + "'";
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

		@Override
		public String leftPad(String field, int zerofillSize, String padCharacter) {
			return "lpad(to_char(" + field + ")," + zerofillSize + ",'" + padCharacter + "'";
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

		@Override
		public String leftPad(String field, int zerofillSize, String padCharacter) {
			return "lpad(to_char(" + field + ")," + zerofillSize + ",'" + padCharacter + "'";
		}
	};
    
    abstract public String rowId();
    
    abstract public boolean hasPartitioning();
    
    abstract public boolean hasIndexOrganizedTables();
    
    abstract public boolean hasIndexCompression();

	public String renameColumnSyntax() {
		return "ALTER TABLE {0} RENAME COLUMN {1} TO {2}";
	}

	public String renameConstraintSyntax() {
		return "ALTER TABLE {0} RENAME CONSTRAINT {1} TO {2}";
	}

    public boolean allowsConstraintRename() {
        return true;
    }

	public abstract String leftPad(String id, int zerofillSize, String s);
}
