package com.energyict.mdc.engine.impl.commands.store.access;

import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.issues.impl.ProblemImpl;
import com.energyict.mdc.protocol.api.DeviceProtocol;

import java.time.Instant;

/**
 * A Daisy chained SignOn command
 * <p>
 * Copyrights EnergyICT
 * Date: 9/10/12
 * Time: 16:23
 */
public class DaisyChainedLogOnCommand extends SimpleComCommand {

    public DaisyChainedLogOnCommand(final CommandRoot commandRoot) {
        super(commandRoot);
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        try {
            deviceProtocol.daisyChainedLogOn();
        } catch (Throwable e) {
            if (e instanceof ConnectionCommunicationException) {
                throw e;
            } else {
                addIssue(new ProblemImpl(getThesaurus(), Instant.now(), deviceProtocol, MessageSeeds.DEVICEPROTOCOL_PROTOCOL_ISSUE.getKey(), e.getLocalizedMessage()), CompletionCode.InitError);
            }
        }
    }

    @Override
    public String getDescriptionTitle() {
        return "Daisy chained log on";
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.DAISY_CHAINED_LOGON;
    }

}