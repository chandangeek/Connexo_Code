package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.*;

import static com.elster.jupiter.orm.impl.TableSpecs.*;

class OrmClientImpl implements OrmClient  {
	
	private final OrmService service;
	
	OrmClientImpl(OrmService service) {	
		this.service = service;
		service.add(createComponent());
	}
	
	private <T,S extends T> DataMapper<T> getDataMapper(Class<T> api, Class<S> implementation , String tableName) {
		return service.getDataMapper(api,implementation,Bus.COMPONENTNAME,tableName);
	}
	
	@Override
	public DataMapper<Component> getComponentFactory() {
		return getDataMapper(Component.class,ComponentImpl.class,ORM_COMPONENT.name());
	}
	@Override
	public DataMapper<Table> getTableFactory() {
		return getDataMapper(Table.class,TableImpl.class,ORM_TABLE.name());
	}
	
	@Override
	public DataMapper<Column> getColumnFactory() {
		return getDataMapper(Column.class,ColumnImpl.class,ORM_COLUMN.name());
	}
	@Override
	public DataMapper<TableConstraint> getTableConstraintFactory() {
		return getDataMapper(TableConstraint.class,TableConstraintImpl.class,ORM_TABLECONSTRAINT.name());
	}
	
	@Override
	public DataMapper<ColumnInConstraintImpl> getColumnInConstraintFactory() {
		return getDataMapper(ColumnInConstraintImpl.class,ColumnInConstraintImpl.class,ORM_COLUMNINCONSTRAINT.name());
	}
	
	@Override
	public Table getTable(String componentName, String tableName) {
		return service.getTable(componentName, tableName);
	}
	
	private Component createComponent() {
		Component result = service.newComponent(Bus.COMPONENTNAME,"Object Relational Mapper");
		for (TableSpecs spec : TableSpecs.values()) {
			spec.addTo(result);			
		}
		return result;
	}		
}