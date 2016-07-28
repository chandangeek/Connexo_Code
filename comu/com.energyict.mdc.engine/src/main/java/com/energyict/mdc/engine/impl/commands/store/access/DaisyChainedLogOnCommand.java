package com.energyict.mdc.engine.impl.commands.store.access;

import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.protocol.api.DeviceProtocol;

/**
 * A Daisy chained SignOn command
 * <p>
 * Copyrights EnergyICT
 * Date: 9/10/12
 * Time: 16:23
 */
public class DaisyChainedLogOnCommand extends SimpleComCommand {

    public DaisyChainedLogOnCommand(final GroupedDeviceCommand groupedDeviceCommand) {
        super(groupedDeviceCommand);
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        try {
            deviceProtocol.daisyChainedLogOn();
        } catch (Throwable e) {
            if (e instanceof ConnectionCommunicationException) {
                throw e;
            } else {
                Problem problem = getCommandRoot().getServiceProvider().issueService().newProblem(deviceProtocol, getThesaurus(), MessageSeeds.DEVICEPROTOCOL_PROTOCOL_ISSUE, e.getLocalizedMessage());
                addIssue(problem, CompletionCode.InitError);
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