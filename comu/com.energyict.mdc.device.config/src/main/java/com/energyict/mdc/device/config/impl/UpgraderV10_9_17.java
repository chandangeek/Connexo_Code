package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

public class UpgraderV10_9_17 implements Upgrader {
    private final DataModel dataModel;

    @Inject
    UpgraderV10_9_17(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        // remove use cashed frame counter property
        execute(dataModel, "DELETE FROM  " + TableSpecs.DTC_PROTOCOLCONFIGPROPSATTR.name() + "  WHERE NAME = 'UseCachedFrameCounter' and DEVICECONFIGURATION in " +
                "(select dc.ID from DTC_DEVICECONFIG dc JOIN DTC_DEVICETYPE dt on dc.DEVICETYPEID = dt.ID JOIN CPC_PLUGGABLECLASS p on dt.DEVICEPROTOCOLPLUGGABLEID = p.id " +
                "where p.JAVACLASSNAME = 'com.energyict.protocolimplv2.dlms.ei7.EI7'or p.JAVACLASSNAME = 'com.energyict.protocolimplv2.dlms.ei6v2021.EI6v2021')");

        // change communication type value
        execute(dataModel, "UPDATE " + TableSpecs.DTC_PROTOCOLCONFIGPROPSATTR.name() + " SET VALUE = 'NBIOT' WHERE NAME = 'CommunicationType' AND VALUE = 'NB-IoT' and DEVICECONFIGURATION in " +
        "(select dc.ID from DTC_DEVICECONFIG dc JOIN DTC_DEVICETYPE dt on dc.DEVICETYPEID = dt.ID JOIN CPC_PLUGGABLECLASS p on dt.DEVICEPROTOCOLPLUGGABLEID = p.id " +
        "where p.JAVACLASSNAME = 'com.energyict.protocolimplv2.dlms.ei6v2021.EI6v2021')");

        // remove communication type property from EI7
        execute(dataModel, "DELETE FROM " + TableSpecs.DTC_PROTOCOLCONFIGPROPSATTR.name() + " WHERE NAME = 'CommunicationType' AND DEVICECONFIGURATION IN " +
                "(SELECT dc.ID FROM " + TableSpecs.DTC_DEVICECONFIG.name() + " dc JOIN " + TableSpecs.DTC_DEVICETYPE.name() + " dt ON dc.DEVICETYPEID = dt.ID " +
                "JOIN CPC_PLUGGABLECLASS p ON dt.DEVICEPROTOCOLPLUGGABLEID = p.id " +
                "WHERE p.JAVACLASSNAME = 'com.energyict.protocolimplv2.dlms.ei7.EI7')");
    }
}
