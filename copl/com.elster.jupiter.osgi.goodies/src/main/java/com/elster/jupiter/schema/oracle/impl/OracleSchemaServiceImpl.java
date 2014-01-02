package com.elster.jupiter.schema.oracle.impl;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.schema.oracle.OracleSchemaService;
import com.elster.jupiter.schema.oracle.UserTable;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;

@Component(
	name="com.elster.jupiter.schema.oracle", 
	property = {"osgi.command.scope=playground", "osgi.command.function=showora" }) 
public class OracleSchemaServiceImpl implements OracleSchemaService {
	
	public OracleSchemaServiceImpl() {
	}

	private volatile DataModel dataModel;
	
	@Reference 
	public void setOrmService(OrmService ormService) {
		dataModel = ormService.newDataModel("ORA", "Oracle schema");
		for (TableSpecs each : TableSpecs.values()) {
			each.addTo(dataModel);
		}
		dataModel.register();
	}
	
	@Override
	public List<UserTable> getTableNames() {
		return dataModel.mapper(UserTable.class).select(Condition.TRUE, "name");
	}
	
	public void showora(String tableName) {
		try {
				Optional<UserTable> table = dataModel.mapper(UserTable.class).getOptional(tableName);
				if (table.isPresent()) {
					for (String each : table.get().generate()) {
						System.out.println(each);
					}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		
	}

	@Override
	public Optional<UserTable> getTable(String tableName) {
		return dataModel.mapper(UserTable.class).getEager(tableName);
	}
}
