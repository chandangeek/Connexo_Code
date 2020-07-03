/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.journal.ProtocolJournal;
import com.energyict.mdc.upl.issue.Problem;

import com.energyict.protocol.exceptions.ConnectionCommunicationException;

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

            try {
                deviceProtocol.setProtocolJournaling(getProtocolJournal(executionContext));
            } catch (Throwable ex){
                //swallow, seems we don't not supports this
                System.out.print(ex.getMessage());
            }

            deviceProtocol.init(device, getComChannel());
        } catch (ConnectionCommunicationException e) {
            throw e;
        } catch (Throwable e) {
            Problem problem = getCommandRoot().getServiceProvider().issueService().newProblem(deviceProtocol, MessageSeeds.DEVICEPROTOCOL_PROTOCOL_ISSUE, e.getMessage(), e);
            addIssue(problem, CompletionCode.InitError);
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


    protected ProtocolJournal getProtocolJournal(ExecutionContext executionContext) {
        ProtocolJournal protocolJournal = a -> {
        };
        if (isJournalingLevelEnabled(getCommunicationLogLevel(executionContext), defaultJournalingLogLevel())) {
            // create a DEBUG-level journal link for the protocols and inject it to protocols
            protocolJournal = a -> executionContext.createJournalEntry(ComServer.LogLevel.DEBUG, a);
        }
        return protocolJournal;
    }
}