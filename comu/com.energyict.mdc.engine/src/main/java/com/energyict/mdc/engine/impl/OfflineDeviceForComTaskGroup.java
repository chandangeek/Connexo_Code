package com.energyict.mdc.engine.impl;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;
import com.energyict.mdc.upl.offline.OfflineDeviceContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides the OfflineContext for a group of ComTasks
 *
 * Copyrights EnergyICT
 * Date: 11/06/13
 * Time: 10:04
 */
public class OfflineDeviceForComTaskGroup implements OfflineDeviceContext {

    private final List<ComTaskExecution> comTaskGroup = new ArrayList<>();
    private final DeviceOfflineFlags flags;

    public OfflineDeviceForComTaskGroup(List<ComTaskExecution> comTaskGroup) {
        this.comTaskGroup.addAll(comTaskGroup);
        DeviceOfflineFlags flags = new DeviceOfflineFlags();
        for (ComTaskExecution comTaskExecution : comTaskGroup) {
            for (ProtocolTask protocolTask : comTaskExecution.getProtocolTasks()) {
                flags = flags.or((OfflineDeviceContext) protocolTask);
            }
        }
        this.flags = flags;
    }

    @Override
    public boolean needsSlaveDevices() {
        return flags.needsSlaveDevices();
    }

    @Override
    public boolean needsSentMessages() {
        return flags.needsSentMessages();
    }

    @Override
    public boolean needsFirmwareVersions() {
        return flags.needsFirmwareVersions();
    }

    @Override
    public boolean needsTouCalendar() {
        return flags.needsTouCalendar();
    }

    @Override
    public boolean needsRegisters() {
        return flags.needsRegisters();
    }

    @Override
    public boolean needsPendingMessages() {
        return flags.needsPendingMessages();
    }

    @Override
    public boolean needsMasterLoadProfiles() {
        return flags.needsMasterLoadProfiles();
    }

    @Override
    public boolean needsLogBooks() {
        return flags.needsLogBooks();
    }

    @Override
    public boolean needsAllLoadProfiles() {
        return flags.needsAllLoadProfiles();
    }
}
