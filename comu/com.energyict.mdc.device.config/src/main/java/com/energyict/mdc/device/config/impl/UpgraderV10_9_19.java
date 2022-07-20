package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

public class UpgraderV10_9_19 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    UpgraderV10_9_19(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        execute(dataModel, "UPDATE " + TableSpecs.DTC_MESSAGEENABLEMENT.name() + " SET DEVICEMESSAGEID = 10007 WHERE DEVICEMESSAGEID = 19 AND DEVICECONFIG IN " +
                "(SELECT dc.ID from " + TableSpecs.DTC_DEVICECONFIG.name() + " dc JOIN " + TableSpecs.DTC_DEVICETYPE.name() + " dt on dc.DEVICETYPEID = dt.ID JOIN CPC_PLUGGABLECLASS p on dt.DEVICEPROTOCOLPLUGGABLEID = p.id " +
                "where p.JAVACLASSNAME = 'com.energyict.protocolimpl.dlms.actarisace6000.ACE6000')");

        execute(dataModel, "UPDATE " + TableSpecs.DTC_MESSAGEENABLEMENT.name() + " SET DEVICEMESSAGEID = 10008 WHERE DEVICEMESSAGEID = 7 AND DEVICECONFIG IN " +
                "(SELECT dc.ID from " + TableSpecs.DTC_DEVICECONFIG.name() + " dc JOIN " + TableSpecs.DTC_DEVICETYPE.name() + " dt on dc.DEVICETYPEID = dt.ID JOIN CPC_PLUGGABLECLASS p on dt.DEVICEPROTOCOLPLUGGABLEID = p.id " +
                "where p.JAVACLASSNAME = 'com.energyict.protocolimpl.dlms.actarisace6000.ACE6000')");
    }
}
