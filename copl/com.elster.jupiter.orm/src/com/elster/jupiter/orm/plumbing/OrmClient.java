package com.elster.jupiter.orm.plumbing;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Component;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.impl.ColumnInConstraintImpl;

public interface OrmClient {
	DataMapper<Component> getComponentFactory();
	DataMapper<Table> getTableFactory();
	DataMapper<Column> getColumnFactory();
	DataMapper<TableConstraint> getTableConstraintFactory();
	DataMapper<ColumnInConstraintImpl> getColumnInConstraintFactory();
	Table getTable(String componentName, String tableName);
}