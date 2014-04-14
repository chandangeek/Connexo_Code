package com.elster.jupiter.schema.oracle.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.schema.oracle.OracleSchemaService;
import com.elster.jupiter.schema.oracle.UpgradeService;
import com.elster.jupiter.schema.oracle.UserTable;
import com.google.common.base.Optional;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Copyrights EnergyICT
 * Date: 10/04/2014
 * Time: 14:57
 */
@Component(name = "com.elster.jupiter.schema.upgrade", service = UpgradeService.class,
        immediate = true, property = {"osgi.command.scope=jupiter", "osgi.command.function=upgrade"})
public class UpgradeServiceImpl implements UpgradeService {

    private volatile OracleSchemaService oracleSchemaService;
    private volatile OrmService ormService;

    @Reference
    public void setOracleSchemaService(OracleSchemaService schemaService) {
        this.oracleSchemaService = schemaService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Override
    public void update(DataModel model) {
        DataModel existingDataModel = ormService.newDataModel("UPG", "Upgrade  of " + model.getName());
        for (Table<?> table : model.getTables()) {
            Optional<UserTable> existingTable = oracleSchemaService.getTable(table.getName());
            if (existingTable.isPresent()) {
                UserTable userTable = existingTable.get();

                Optional<UserTable> existingJournalTable = Optional.absent();
                if (table.hasJournal()) {
                    existingJournalTable = oracleSchemaService.getTable(table.getJournalTableName());
                }
                userTable.addTo(existingDataModel, Optional.fromNullable(existingJournalTable.isPresent() ? existingJournalTable.get().getName() : null));
            }
        }
        existingDataModel.upgradeTo(model);

    }


    public void upgrade(String[] componentNames) throws InvalidSyntaxException {
        try {
            for (String componentName : componentNames) {
                System.out.println("About to install " + componentName);
                upgrade(componentName);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    private void upgrade(String componentName) {
        Optional<? extends DataModel> dataModel = ormService.getDataModel(componentName);
        if (dataModel.isPresent()) {
            System.out.println("upgrading dataModel = " + dataModel);
            update(dataModel.get());
        } else {
            System.out.println("Noting to update");
        }
    }


}
