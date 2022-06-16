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
        execute(dataModel, "UPDATE " + TableSpecs.DDC_DEVICEPROTOCOLPROPERTY.name() + " SET INFOVALUE = 'NBIOT' WHERE PROPERTYSPEC = 'CommunicationType' AND INFOVALUE = 'NB-IoT'");
        execute(dataModel, "DELETE FROM " + TableSpecs.DDC_DEVICEPROTOCOLPROPERTY.name() + " WHERE PROPERTYSPEC = 'UseCachedFrameCounter'");
        execute(dataModel, "DELETE FROM " + TableSpecs.DDC_DEVICEPROTOCOLPROPERTY.name() + " where PROPERTYSPEC = 'CommunicationType' AND DEVICEID IN " +
                "(SELECT dd.ID FROM " + TableSpecs.DDC_DEVICE.name() + " dd JOIN DTC_DEVICETYPE dt ON dd.DEVICETYPE = dt.ID JOIN CPC_PLUGGABLECLASS p ON dt.DEVICEPROTOCOLPLUGGABLEID = p.id " +
                "WHERE p.JAVACLASSNAME = 'com.energyict.protocolimplv2.dlms.ei7.EI7')");

    }
}
