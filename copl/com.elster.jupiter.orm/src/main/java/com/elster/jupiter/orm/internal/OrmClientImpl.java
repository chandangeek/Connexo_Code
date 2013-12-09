package com.elster.jupiter.orm.internal;

import static com.elster.jupiter.orm.internal.TableSpecs.ORM_COLUMN;
import static com.elster.jupiter.orm.internal.TableSpecs.ORM_COLUMNINCONSTRAINT;
import static com.elster.jupiter.orm.internal.TableSpecs.ORM_DATAMODEL;
import static com.elster.jupiter.orm.internal.TableSpecs.ORM_TABLE;
import static com.elster.jupiter.orm.internal.TableSpecs.ORM_TABLECONSTRAINT;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.orm.impl.ColumnInConstraintImpl;

public class OrmClientImpl implements OrmClient  {
	
	private final DataModel dataModel;
	
	public OrmClientImpl(DataModel dataModel) {	
		this.dataModel = dataModel;	
	}
	
	@Override
	public DataMapper<DataModel> getDataModelFactory() {
		return dataModel.getDataMapper(DataModel.class,ORM_DATAMODEL.name());
	}
	@Override
	public DataMapper<Table> getTableFactory() {
		return dataModel.getDataMapper(Table.class,ORM_TABLE.name());
	}
	
	@Override
	public DataMapper<Column> getColumnFactory() {
		return dataModel.getDataMapper(Column.class,ORM_COLUMN.name());
	}
	@Override
	public DataMapper<TableConstraint> getTableConstraintFactory() {
		return dataModel.getDataMapper(TableConstraint.class,ORM_TABLECONSTRAINT.name());
	}
	
	@Override
	public DataMapper<ColumnInConstraintImpl> getColumnInConstraintFactory() {
		return dataModel.getDataMapper(ColumnInConstraintImpl.class,ORM_COLUMNINCONSTRAINT.name());
	}
	
	@Override
	public void install(boolean executeDdl, boolean storeMappings) {
		dataModel.install(executeDdl, storeMappings);
		
	}		
}