/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.query.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.impl.ColumnImpl;

class ColumnAndAlias {
		private final Column column;
		private final String alias;
				
		ColumnAndAlias(ColumnImpl column, String alias) {
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
