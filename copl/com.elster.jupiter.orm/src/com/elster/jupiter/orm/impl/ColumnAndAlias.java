package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;

class ColumnAndAlias {
		private final Column column;
		private final String alias;
				
		ColumnAndAlias(Column column, String alias) {
			this.column = column;
			this.alias = alias;
		}
		
		@Override
		public String toString() {
			return column.getName(alias);
		}

		public Column getColumn() {
			return column;
		}

		public String getAlias() {
			return alias;
		}
}
