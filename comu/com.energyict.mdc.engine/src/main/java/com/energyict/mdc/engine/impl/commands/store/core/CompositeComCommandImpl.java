/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.CompositeComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.issues.Warning;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A CompositeComCommand can contain several {@link ComCommand ComCommands} which are executed in the order the
 * {@link ComCommand} itself desires. We are responsible for creating/fetching our own {@link ComCommand ComCommands}.
 * We are also responsible for making sure that all {@link ComCommand ComCommands} in the CommandRoot
 * are unique by ComCommandType, if not a
 * ComCommandException#uniqueCommandViolation must be thrown.<br/>
 * The {@link SimpleComCommand#doExecute(DeviceProtocol, com.energyict.mdc.engine.impl.core.ExecutionContext)} will call the {@link ComCommand#execute(DeviceProtocol, com.energyict.mdc.engine.impl.core.ExecutionContext)} of all the
 * {@link ComCommand commands} in the {@link #comCommands commandList} <b>in chronological order.</b>
 *
 * @author gna
 * @since 10/05/12 - 8:33
 */
public abstract class CompositeComCommandImpl extends SimpleComCommand implements CompositeComCommand {

    /**
     * Contains all necessary commands for this {@link ComCommand}.
     * <b>It is necessary to use a LinkedHashMap because we need the commands in chronological order</b>
     */
    protected Map<ComCommandType, ComCommand> comCommands = new LinkedHashMap<>();

    protected CompositeComCommandImpl(final GroupedDeviceCommand groupedDeviceCommand) {
        super(groupedDeviceCommand);
    }

    @Override
    public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        for (ComCommand comCommand : comCommands.values()) {
            if (areWeAllowedToPerformTheCommand(comCommand)) {
                comCommand.execute(deviceProtocol, executionContext);
            } else {
                comCommand.setCompletionCode(CompletionCode.NotExecuted);
            }
        }
    }

    private boolean areWeAllowedToPerformTheCommand(ComCommand comCommand) {
        return getGroupedDeviceCommand().areWeAllowedToPerformTheCommand(comCommand);
    }

    @Override
    public void addCommand(final ComCommand command, ComTaskExecution comTaskExecution) {
        this.comCommands.put(command.getCommandType(), command);
    }

    /**
     * Getter for all the issue which occurred during the execution of this {@link CompositeComCommand};<br></br>
     * The list will include:
     * <ul>
     * <li>issues present in the {@link CollectedData} of this command</li>
     * <li>issues taken from all child commands</li>
     * </ul>
     *
     * @return a list of Issues
     */
    public List<Issue> getIssues() {
        List<Issue> issues = super.getIssues();
        for (ComCommand child : this) {
            issues.addAll(child.getIssues());
        }
        return issues;
    }

    @Override
    public List<Warning> getWarnings() {
        List<Issue> issues = this.getIssues();
        List<Warning> warnings = new ArrayList<>(issues.size());    // At most all issues are warnings
        warnings.addAll(issues.stream().filter(Issue::isWarning).map(issue -> (Warning) issue).collect(Collectors.toList()));
        return warnings;
    }

    @Override
    public List<Problem> getProblems() {
        List<Issue> issues = this.getIssues();
        List<Problem> problems = new ArrayList<>(issues.size());    // At most all issues are problems
        problems.addAll(issues.stream().filter(Issue::isProblem).map(issue -> (Problem) issue).collect(Collectors.toList()));
        return problems;
    }

    /**
     * Getter for the issues, which are present in the {@link CollectedData} of this {@link CompositeComCommand};<br></br>
     * <b>Warning: </b>Issues of child commands will not be taken into account.
     *
     * @return a list of Issues
     */
    protected List<Issue> getOwnIssuesIgnoringChildren() {
        return super.getIssues();
    }

    @Override
    public CompletionCode getCompletionCode() {
        CompletionCode completionCode = super.getCompletionCode();

        // Also look at the CompletionCode of all child ComCommands
        for (Map.Entry<ComCommandType, ComCommand> comCommandEntry : comCommands.entrySet()) {
            final ComCommand comCommand = comCommandEntry.getValue();
            completionCode = completionCode.upgradeTo(comCommand.getCompletionCode());
        }

        return completionCode;
    }

    /**
     * Get the List of ComCommands
     *
     * @return the requested list of ComCommands
     */
    public Map<ComCommandType, ComCommand> getCommands() {
        return this.comCommands;
    }

    @Override
    public Iterator<ComCommand> iterator() {
        return this.comCommands.values().iterator();
    }
}