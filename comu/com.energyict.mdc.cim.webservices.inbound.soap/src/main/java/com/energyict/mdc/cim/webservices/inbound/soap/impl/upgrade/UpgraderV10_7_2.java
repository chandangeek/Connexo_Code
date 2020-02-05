package com.energyict.mdc.cim.webservices.inbound.soap.impl.upgrade;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import com.google.inject.Inject;

import java.sql.Statement;

public class UpgraderV10_7_2 implements Upgrader {

    private final DataModel dataModel;

    private final static String TABLE_NAME =  "MCM_SCS_CNT";
    private final static String TABLE_NAME_JRNL =  "MCM_SCS_CNTJRNL";


    private final static String FIELD_METER =  "METER";
    private final static String FIELD_ERRORMESSAGE =  "ERRORMESSAGE";

    @Inject
    UpgraderV10_7_2(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        migrateVarcharFieldToClobSQL(TABLE_NAME, FIELD_METER);
        migrateVarcharFieldToClobSQL(TABLE_NAME, FIELD_ERRORMESSAGE);

        migrateVarcharFieldToClobSQL(TABLE_NAME_JRNL, FIELD_METER);
        migrateVarcharFieldToClobSQL(TABLE_NAME_JRNL, FIELD_ERRORMESSAGE);
    }

    private void migrateVarcharFieldToClobSQL(String tableName, String fieldName) {
        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                execute(statement, "alter table " + tableName + " add (" + fieldName + "_2 clob)");
                execute(statement, "update " + tableName + " set " + fieldName + "_2 = " + fieldName);
                execute(statement, "alter table "  + tableName + " drop column " + fieldName);
                execute(statement, "alter table " + tableName + " rename column " + fieldName + "_2 to " + fieldName);
            }
        });
    }
}
