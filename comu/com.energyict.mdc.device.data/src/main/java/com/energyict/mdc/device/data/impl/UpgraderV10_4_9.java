package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;

import javax.inject.Inject;

public class UpgraderV10_4_9 implements Upgrader {

    private final DataModel dataModel;

    @Inject
    public UpgraderV10_4_9(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4, 9));
        updateCRLTable();
    }

    private void updateCRLTable() {
        int definedCRLTasks = dataModel.mapper(CrlRequestTaskProperty.class).find().size();
        if (definedCRLTasks != 0) {
            throw new RuntimeException("Cannot upgrade while CRL tasks defined and cannot be automatically upgraded. Please delete your CRL tasks and retry");
        }
        execute(dataModel, "ALTER TABLE " + TableSpecs.DDC_CRL_REQUEST_TASK_PROPS.name() + " DROP COLUMN SECURITY_ACCESSOR");
    }
}
