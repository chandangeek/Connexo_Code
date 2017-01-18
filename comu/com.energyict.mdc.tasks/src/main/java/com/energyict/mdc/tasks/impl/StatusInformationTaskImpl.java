package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;

import javax.inject.Inject;

import static com.energyict.mdc.upl.offline.DeviceOfflineFlags.FIRMWARE_VERSIONS_FLAG;
import static com.energyict.mdc.upl.offline.DeviceOfflineFlags.REGISTERS_FLAG;
import static com.energyict.mdc.upl.offline.DeviceOfflineFlags.SLAVE_DEVICES_FLAG;
import static com.energyict.mdc.upl.offline.DeviceOfflineFlags.TOU_CALENDAR_FLAG;

/**
 * Implementation for a {@link com.energyict.mdc.tasks.StatusInformationTask}.
 *
 * @author gna
 * @since 2/05/12 - 13:10
 */
class StatusInformationTaskImpl extends ProtocolTaskImpl implements StatusInformationTask {

    private static final DeviceOfflineFlags FLAGS = new DeviceOfflineFlags(SLAVE_DEVICES_FLAG, REGISTERS_FLAG, TOU_CALENDAR_FLAG, FIRMWARE_VERSIONS_FLAG);

    @Inject
    StatusInformationTaskImpl(DataModel dataModel) {
        super(dataModel);
        setFlags(FLAGS);
    }

    @Override
    void deleteDependents() {
        // currently no dependents to delete
    }

}