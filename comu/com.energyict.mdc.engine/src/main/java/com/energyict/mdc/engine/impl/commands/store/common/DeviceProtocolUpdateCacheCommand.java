/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.common;


import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.UpdatedDeviceCache;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

public class DeviceProtocolUpdateCacheCommand extends SimpleComCommand {

    private final OfflineDevice device;

    private DeviceProtocolCache deviceCache;

    public DeviceProtocolUpdateCacheCommand(final GroupedDeviceCommand groupedDeviceCommand) {
        super(groupedDeviceCommand);
        this.device = groupedDeviceCommand.getOfflineDevice();
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        UpdatedDeviceCache updatedDeviceCache = new UpdatedDeviceCache(device.getDeviceIdentifier());
        updatedDeviceCache.setCollectedDeviceCache(deviceProtocol.getDeviceCache());
        this.deviceCache = updatedDeviceCache.getCollectedDeviceCache();
        addCollectedDataItem(updatedDeviceCache);
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (this.isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
            if (this.deviceCache != null && this.deviceCache.isDirty()) {
                builder.addLabel("Content has changed, update is required");
            } else {
                builder.addLabel("No update needed");
            }
        }
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND;
    }

    @Override
    public String getDescriptionTitle() {
        return "Update the stored device cache";
    }

    protected LogLevel defaultJournalingLogLevel() {
        return LogLevel.DEBUG;
    }

}