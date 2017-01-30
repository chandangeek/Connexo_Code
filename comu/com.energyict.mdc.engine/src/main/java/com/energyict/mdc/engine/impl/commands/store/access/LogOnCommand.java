package com.energyict.mdc.engine.impl.commands.store.access;

import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.exceptions.ConnectionSetupException;
import com.energyict.mdc.upl.io.ConnectionCommunicationException;
import com.energyict.mdc.upl.io.SerialComponentService;
import com.energyict.mdc.upl.issue.Problem;

import com.energyict.protocol.exceptions.ConnectionException;

import static com.energyict.mdc.engine.impl.commands.MessageSeeds.DEVICEPROTOCOL_PROTOCOL_ISSUE;
import static com.energyict.mdc.engine.impl.commands.MessageSeeds.SOMETHING_UNEXPECTED_HAPPENED;

/**
 * A LogOn command performs a logical signOn with the device.
 * <p>
 * Copyrights EnergyICT
 * Date: 9/08/12
 * Time: 11:12
 */
public class LogOnCommand extends SimpleComCommand {

    public LogOnCommand(final GroupedDeviceCommand groupedDeviceCommand) {
        super(groupedDeviceCommand);
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        try {
            deviceProtocol.logOn();
        } catch (IllegalArgumentException e) {
            throw new ConnectionSetupException(MessageSeeds.LOG_ON_FAILED, new ConnectionException(SerialComponentService.COMPONENT_NAME, SOMETHING_UNEXPECTED_HAPPENED, e));
        } catch (ConnectionCommunicationException e) {
            throw e;
        } catch (Throwable e) {
            Problem problem = getCommandRoot().getServiceProvider().issueService().newProblem(deviceProtocol, DEVICEPROTOCOL_PROTOCOL_ISSUE, e.getLocalizedMessage(), e);
            addIssue(problem, CompletionCode.InitError);
        }
    }

    @Override
    public String getDescriptionTitle() {
        return "Log on to the device";
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.LOGON;
    }

}