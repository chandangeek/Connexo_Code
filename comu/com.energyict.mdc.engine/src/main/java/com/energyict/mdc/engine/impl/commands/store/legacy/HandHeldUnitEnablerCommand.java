package com.energyict.mdc.engine.impl.commands.store.legacy;

import com.energyict.mdc.engine.exceptions.ComCommandException;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolAdapter;
import com.energyict.mdc.protocol.api.OpticalDriver;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.SmartMeterProtocolAdapter;
import com.energyict.protocols.mdc.channels.serial.SerialComChannel;

/**
 * Command to enable the HandHeldUnit controller for legacy protocols
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/08/12
 * Time: 14:19
 */
public class HandHeldUnitEnablerCommand extends SimpleComCommand {

    private ComChannelPlaceHolder comChannelPlaceHolder;

    public HandHeldUnitEnablerCommand (CommandRoot commandRoot, ComChannelPlaceHolder comChannelPlaceHolder) {
        super(commandRoot);
        this.comChannelPlaceHolder = comChannelPlaceHolder;
    }

    @Override
    public void doExecute (DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        if (this.comChannelPlaceHolder.getComPortRelatedComChannel().getDelegatingComChannel() instanceof SerialComChannel) {
            SerialComChannel comChannel = (SerialComChannel) this.comChannelPlaceHolder.getComPortRelatedComChannel().getDelegatingComChannel();
            SerialCommunicationChannelAdapter serialCommunicationChannel = new SerialCommunicationChannelAdapter(comChannel);
            try {
                if (deviceProtocol instanceof MeterProtocolAdapter || deviceProtocol instanceof SmartMeterProtocolAdapter) {
                    if (executionContext.getConnectionTask().getConnectionType() instanceof OpticalDriver) {
                        ((DeviceProtocolAdapter) deviceProtocol).enableHHUSignOn(serialCommunicationChannel);
                    }
                } else {
                    throw ComCommandException.illegalCommand(this, deviceProtocol);
                }
            } catch (ConnectionException e) {
                throw new CommunicationException(e);
            }
        }
    }

    @Override
    public ComCommandTypes getCommandType () {
        return ComCommandTypes.HAND_HELD_UNIT_ENABLER;
    }

    protected LogLevel defaultJournalingLogLevel () {
        return LogLevel.DEBUG;
    }

}