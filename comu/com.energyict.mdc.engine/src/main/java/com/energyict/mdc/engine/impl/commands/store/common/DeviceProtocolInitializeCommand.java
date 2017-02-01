/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

public class DeviceProtocolInitializeCommand extends SimpleComCommand {

    private final OfflineDevice device;
    private final ComChannelPlaceHolder comChannelPlaceHolder;
    private ComPortRelatedComChannel comChannel;

    public DeviceProtocolInitializeCommand(GroupedDeviceCommand groupedDeviceCommand, ComChannelPlaceHolder comChannelPlaceHolder) {
        super(groupedDeviceCommand);
        this.device = groupedDeviceCommand.getOfflineDevice();
        this.comChannelPlaceHolder = comChannelPlaceHolder;
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        try {
            deviceProtocol.init(device, getComChannel());
        } catch (Throwable e) {
            if (e instanceof ConnectionCommunicationException) {
                throw e;
            } else {
                Problem problem = getCommandRoot().getServiceProvider().issueService().newProblem(deviceProtocol, MessageSeeds.DEVICEPROTOCOL_PROTOCOL_ISSUE, e.getCause(), e);
                addIssue(problem, CompletionCode.InitError);
            }
        }
    }

    private ComPortRelatedComChannel getComChannel() {
        if (this.comChannel == null) {
            this.comChannel = comChannelPlaceHolder.getComPortRelatedComChannel();
        }
        return this.comChannel;
    }

    /**
     * Allows the framework to update the ComChannel.
     * Preferably before the command is executed.
     *
     * @param comChannel the comChannel which should be used to initialize the DeviceProtocol
     */
    public void updateComChannel(ComPortRelatedComChannel comChannel) {
        this.comChannel = comChannel;
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE;
    }

    @Override
    public String getDescriptionTitle() {
        return "Initialize the device protocol";
    }

    protected LogLevel defaultJournalingLogLevel() {
        return LogLevel.DEBUG;
    }

}