package com.elster.jupiter.orm.plumbing;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.impl.ColumnInConstraintImpl;

public interface OrmClient {
	DataMapper<DataModel> getDataModelFactory();
	DataMapper<Table> getTableFactory();
	DataMapper<Column> getColumnFactory();
	DataMapper<TableConstraint> getTableConstraintFactory();
	DataMapper<ColumnInConstraintImpl> getColumnInConstraintFactory();
	void install(boolean executeDdl, boolean storeMappings);
}