package com.energyict.mdc.task.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.task.StatusInformationTask;
import javax.inject.Inject;

import static com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags.REGISTERS_FLAG;
import static com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags.SLAVE_DEVICES_FLAG;

/**
 * Implementation for a {@link com.energyict.mdc.task.StatusInformationTask}.
 *
 * @author gna
 * @since 2/05/12 - 13:10
 */
class StatusInformationTaskImpl extends ProtocolTaskImpl implements StatusInformationTask {

    private static final DeviceOfflineFlags FLAGS = new DeviceOfflineFlags(SLAVE_DEVICES_FLAG, REGISTERS_FLAG);

    @Inject
    public StatusInformationTaskImpl(DataModel dataModel) {
        super(dataModel);
        setFlags(FLAGS);
    }

}