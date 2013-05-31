package com.elster.jupiter.orm.plumbing;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.ColumnInConstraintImpl;
import com.elster.jupiter.orm.impl.DataModelImpl;
import com.elster.jupiter.orm.impl.TableConstraintImpl;
import com.elster.jupiter.orm.impl.TableImpl;

import static com.elster.jupiter.orm.plumbing.TableSpecs.*;

public class OrmClientImpl implements OrmClient  {
	
	private final DataModel dataModel;
	
	public OrmClientImpl() {	
		this.dataModel = createDataModel();		
	}
	
	@Override
	public DataMapper<DataModel> getDataModelFactory() {
		return dataModel.getDataMapper(DataModel.class,DataModelImpl.class,ORM_DATAMODEL.name());
	}
	@Override
	public DataMapper<Table> getTableFactory() {
		return dataModel.getDataMapper(Table.class,TableImpl.class,ORM_TABLE.name());
	}
	
	@Override
	public DataMapper<Column> getColumnFactory() {
		return dataModel.getDataMapper(Column.class,ColumnImpl.class,ORM_COLUMN.name());
	}
	@Override
	public DataMapper<TableConstraint> getTableConstraintFactory() {
		return dataModel.getDataMapper(TableConstraint.class,TableConstraintImpl.implementers,ORM_TABLECONSTRAINT.name());
	}
	
	@Override
	public DataMapper<ColumnInConstraintImpl> getColumnInConstraintFactory() {
		return dataModel.getDataMapper(ColumnInConstraintImpl.class,ColumnInConstraintImpl.class,ORM_COLUMNINCONSTRAINT.name());
	}
	
	private DataModel createDataModel() {
		DataModel result =  new DataModelImpl(Bus.COMPONENTNAME,"Object Relational Mapper");
		for (TableSpecs spec : TableSpecs.values()) {
			spec.addTo(result);			
		}
		return result;
	}

	@Override
	public void install(boolean executeDdl, boolean storeMappings) {
		dataModel.install(executeDdl, storeMappings);
		
	}		
}