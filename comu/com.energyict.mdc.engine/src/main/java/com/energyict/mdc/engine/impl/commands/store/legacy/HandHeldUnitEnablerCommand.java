package com.energyict.mdc.engine.impl.commands.store.legacy;

import com.energyict.mdc.engine.exceptions.ComCommandException;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
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

/**
 * Command to enable the HandHeldUnit controller for legacy protocols.
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
        if (this.comChannelPlaceHolder.getComPortRelatedComChannel().getActualComChannel() instanceof SerialComChannel) {
            SerialComChannel comChannel = (SerialComChannel) this.comChannelPlaceHolder.getComPortRelatedComChannel().getActualComChannel();
            SerialCommunicationChannelAdapter serialCommunicationChannel = new SerialCommunicationChannelAdapter(comChannel);
            try {
                if (deviceProtocol instanceof DeviceProtocolAdapter) {
                    if (executionContext.getConnectionTask().getConnectionType() instanceof OpticalDriver) {
                        ((DeviceProtocolAdapter) deviceProtocol).enableHHUSignOn(serialCommunicationChannel);
                    }
                } else {
                    throw ComCommandException.illegalCommand(this, deviceProtocol, com.energyict.mdc.engine.impl.MessageSeeds.ILLEGAL_COMMAND);
                }
            } catch (ConnectionException e) {
                throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
            }
        }
    }

    @Override
    public ComCommandTypes getCommandType () {
        return ComCommandTypes.HAND_HELD_UNIT_ENABLER;
    }

    @Override
    public String getDescriptionTitle() {
        return "Hand-held unit sign-on";
    }

    protected LogLevel defaultJournalingLogLevel () {
        return LogLevel.DEBUG;
    }

}