/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.schema.oracle.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.schema.ExistingTable;
import com.elster.jupiter.schema.oracle.OracleSchemaService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import java.util.Optional;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

@Component(name = "com.elster.jupiter.schema.oracle", service = OracleSchemaService.class,
        property = {"osgi.command.scope=playground", "osgi.command.function=showora"})
public class OracleSchemaServiceImpl implements OracleSchemaService {

    public OracleSchemaServiceImpl() {
    }

    private volatile DataModel dataModel;

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.getDataModel(OrmService.EXISTING_TABLES_DATA_MODEL).get();
    }

    @Override
    public List<ExistingTable> getTableNames() {
        return dataModel.mapper(ExistingTable.class).select(Condition.TRUE, Order.ascending("name"));
    }


    @Override
    public Optional<ExistingTable> getTable(String tableName) {
        return dataModel.mapper(ExistingTable.class).getEager(tableName);
    }

    public void showora(String tableName) {
        try {
            Optional<ExistingTable> table = getTable(tableName);
            if (table.isPresent()) {
                for (String each : new TableModelGenerator(table.get()).generate()) {
                    System.out.println(each);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
