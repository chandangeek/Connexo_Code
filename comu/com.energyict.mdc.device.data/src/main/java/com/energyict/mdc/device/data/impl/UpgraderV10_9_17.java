package com.energyict.mdc.device.data.impl;

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
        execute(dataModel, "DELETE FROM " + TableSpecs.DDC_DEVICEPROTOCOLPROPERTY.name() + " WHERE PROPERTYSPEC = 'UseCachedFrameCounter' and DEVICEID in " +
                "(select dd.ID from DDC_DEVICE dd join DTC_DEVICETYPE dt on dd.DEVICETYPE = dt.ID JOIN CPC_PLUGGABLECLASS p on dt.DEVICEPROTOCOLPLUGGABLEID = p.id " +
                "where p.JAVACLASSNAME = 'com.energyict.protocolimplv2.dlms.ei7.EI7' or p.JAVACLASSNAME = 'com.energyict.protocolimplv2.dlms.ei6v2021.EI6v2021')");

        execute(dataModel, "UPDATE " + TableSpecs.DDC_DEVICEPROTOCOLPROPERTY.name() + " SET INFOVALUE = 'NBIOT' " +
                "WHERE PROPERTYSPEC = 'CommunicationType' AND INFOVALUE = 'NB-IoT' and DEVICEID in " +
                "(select dd.ID from DDC_DEVICE dd join DTC_DEVICETYPE dt on dd.DEVICETYPE = dt.ID JOIN CPC_PLUGGABLECLASS p on dt.DEVICEPROTOCOLPLUGGABLEID = p.id " +
                "where p.JAVACLASSNAME = 'com.energyict.protocolimplv2.dlms.ei6v2021.EI6v2021')");

        execute(dataModel, "DELETE FROM " + TableSpecs.DDC_DEVICEPROTOCOLPROPERTY.name() + " where PROPERTYSPEC = 'CommunicationType' AND DEVICEID IN " +
                "(SELECT dd.ID FROM " + TableSpecs.DDC_DEVICE.name() + " dd JOIN DTC_DEVICETYPE dt ON dd.DEVICETYPE = dt.ID " +
                "JOIN CPC_PLUGGABLECLASS p ON dt.DEVICEPROTOCOLPLUGGABLEID = p.id " +
                "WHERE p.JAVACLASSNAME = 'com.energyict.protocolimplv2.dlms.ei7.EI7')");

    }
}
