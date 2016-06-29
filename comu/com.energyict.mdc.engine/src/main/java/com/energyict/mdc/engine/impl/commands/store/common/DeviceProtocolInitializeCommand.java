package com.energyict.mdc.engine.impl.commands.store.common;

import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.inbound.ComChannelPlaceHolder;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.issues.impl.ProblemImpl;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

import java.time.Instant;

/**
 * Command to initialize a {@link DeviceProtocol}
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/07/12
 * Time: 14:11
 */
public class DeviceProtocolInitializeCommand extends SimpleComCommand {

    private final OfflineDevice device;
    private final ComChannelPlaceHolder comChannelPlaceHolder;

    public DeviceProtocolInitializeCommand(CommandRoot commandRoot, OfflineDevice device, ComChannelPlaceHolder comChannelPlaceHolder) {
        super(commandRoot);
        this.device = device;
        this.comChannelPlaceHolder = comChannelPlaceHolder;
    }

    @Override
    public void doExecute (DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        try {
            deviceProtocol.init(device, comChannelPlaceHolder.getComPortRelatedComChannel());
        } catch (Throwable e) {
            if (e instanceof ConnectionCommunicationException) {
                throw e;
            } else {
                addIssue(new ProblemImpl(getThesaurus(), Instant.now(), deviceProtocol, MessageSeeds.DEVICEPROTOCOL_PROTOCOL_ISSUE.getKey(), e.getLocalizedMessage()), CompletionCode.InitError);
            }
        }
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE;
    }

    @Override
    public String getDescriptionTitle() {
        return "Initialize the device protocol";
    }

    protected LogLevel defaultJournalingLogLevel () {
        return LogLevel.DEBUG;
    }

}