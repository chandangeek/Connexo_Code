/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.legacy;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.exceptions.ComCommandException;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolAdapter;
import com.energyict.mdc.protocol.api.OpticalDriver;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexEnabler;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.SmartMeterProtocolAdapter;

public class HandHeldUnitEnablerCommand extends SimpleComCommand {

    private ComChannelPlaceHolder comChannelPlaceHolder;
    private boolean hhuSignOn = false;

    public HandHeldUnitEnablerCommand(GroupedDeviceCommand groupedDeviceCommand, ComChannelPlaceHolder comChannelPlaceHolder) {
        super(groupedDeviceCommand);
        this.comChannelPlaceHolder = comChannelPlaceHolder;
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        if (isSerialComChannel()) {
            SerialComChannel comChannel = this.comChannelPlaceHolder.getComPortRelatedComChannel();
            SerialCommunicationChannelAdapter serialCommunicationChannel = new SerialCommunicationChannelAdapter(comChannel);
            try {
                if (deviceProtocol instanceof MeterProtocolAdapter || deviceProtocol instanceof SmartMeterProtocolAdapter) {
                    if (executionContext.getConnectionTask().getConnectionType() instanceof OpticalDriver) {
                        ((DeviceProtocolAdapter) deviceProtocol).enableHHUSignOn(serialCommunicationChannel);
                        hhuSignOn = true;
                    }

                    //If the old protocol implements HalfDuplexEnabler, provide the HalfDuplexController
                    if ((deviceProtocol instanceof MeterProtocolAdapter) && (((MeterProtocolAdapter) deviceProtocol).getMeterProtocol() instanceof HalfDuplexEnabler)) {
                        ((HalfDuplexEnabler) ((MeterProtocolAdapter) deviceProtocol).getMeterProtocol()).setHalfDuplexController(serialCommunicationChannel);
                    } else if ((deviceProtocol instanceof SmartMeterProtocolAdapter) && (((SmartMeterProtocolAdapter) deviceProtocol).getSmartMeterProtocol() instanceof HalfDuplexEnabler)) {
                        ((HalfDuplexEnabler) ((SmartMeterProtocolAdapter) deviceProtocol).getSmartMeterProtocol()).setHalfDuplexController(serialCommunicationChannel);
                    }

                } else {
                    throw ComCommandException.illegalCommand(this, deviceProtocol, com.energyict.mdc.engine.impl.MessageSeeds.ILLEGAL_COMMAND);
                }
            } catch (ConnectionException e) {
                throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
            }
        }
    }

    private boolean isSerialComChannel() {
        return this.comChannelPlaceHolder.getComPortRelatedComChannel().getSerialPort() != null;
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.HAND_HELD_UNIT_ENABLER;
    }

    @Override
    public String getDescriptionTitle() {
        return "Hand-held unit sign-on";
    }

    protected LogLevel defaultJournalingLogLevel() {
        return LogLevel.DEBUG;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        builder.addLabel(hhuSignOn ? "Enabling sign-on" : "Not needed");
    }

}