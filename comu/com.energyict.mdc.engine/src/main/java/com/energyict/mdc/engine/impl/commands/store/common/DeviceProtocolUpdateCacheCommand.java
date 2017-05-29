/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.common;


import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.UpdatedDeviceCache;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;

import com.energyict.protocol.FrameCounterCache;

public class DeviceProtocolUpdateCacheCommand extends SimpleComCommand {

    private final OfflineDevice device;

    private DeviceProtocolCache deviceCache;

    public DeviceProtocolUpdateCacheCommand(final GroupedDeviceCommand groupedDeviceCommand) {
        super(groupedDeviceCommand);
        this.device = groupedDeviceCommand.getOfflineDevice();
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        DeviceProtocolCache deviceCache = deviceProtocol.getDeviceCache();
        if (shouldStoreCache(deviceCache)) {
            UpdatedDeviceCache updatedDeviceCache = new UpdatedDeviceCache(device.getDeviceIdentifier());
            updatedDeviceCache.setCollectedDeviceCache(deviceCache);
            this.deviceCache = updatedDeviceCache.getCollectedDeviceCache();
            addCollectedDataItem(updatedDeviceCache);
        }
    }

    /**
     * - Always store the received cache (provided by the protocol) if it's an instance of FrameCounterCache.
     * - If this is not the case, it is not allowed to store the new cache if there's a serial number mismatch, and there was no old cache yet in EIServer.
     */
    private boolean shouldStoreCache(DeviceProtocolCache deviceCache) {
        boolean containsFrameCounter = (deviceCache != null) && (deviceCache instanceof FrameCounterCache);

        boolean alreadyHasCache = (getOfflineDevice().getDeviceProtocolCache() != null);
        ComCommand basicCheckCommand = getCommandRoot().getCommands().get(ComCommandTypes.BASIC_CHECK_COMMAND);
        boolean serialNumberOK = basicCheckCommand == null || basicCheckCommand.getCompletionCode() != CompletionCode.ConfigurationError;     //Completion code 'ConfigurationError' on the basic check task means serial number mismatch

        return containsFrameCounter || alreadyHasCache || serialNumberOK;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (this.isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
            if (this.deviceCache != null && this.deviceCache.contentChanged()) {
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