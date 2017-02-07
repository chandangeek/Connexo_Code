/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.issues.Warning;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

import java.util.List;

/**
 * Provides general functionality for a ComCommand.
 *
 * @author gna
 * @since 8/05/12 - 14:31
 */
public interface ComCommand {

    /**
     * @return the {@link ComCommandType type} of this command.
     */
    ComCommandType getCommandType();

    /**
     * Get the root of this command which serves as a root for unique {@link ComCommand ComCommands}.
     *
     * @return the {@link CommandRoot}
     */
    CommandRoot getCommandRoot();

    /**
     * Get the grouped device command which serves as a parent for unique ComCommands
     *
     * @return the {@link GroupedDeviceCommand}
     */
    GroupedDeviceCommand getGroupedDeviceCommand();

    /**
     * @return the OfflineDevice for which this command needs to be performed
     */
    OfflineDevice getOfflineDevice();

    /**
     * Get all the issue which occurred during the execution of this {@link ComCommand}.
     *
     * @return a List of occurred {@link Issue issues}
     */
    List<Issue> getIssues();

    /**
     * Get all the problems which occurred during the execution of this {@link ComCommand}.
     * This is a convenience method for getIssues that filters the warnings
     * to return only the problems.
     *
     * @return a List of occurred {@link Problem}s
     * @see #getIssues()
     */
    List<Problem> getProblems();

    /**
     * Get all the warnings which occurred during the execution of this {@link ComCommand}.
     * This is a convenience method for getIssues that filters the problems
     * to return only the warnings.
     *
     * @return a List of occurred {@link Warning}s
     * @see #getIssues()
     */
    List<Warning> getWarnings();

    /**
     * Get all the {@link CollectedData} which is collected during this {@link ComCommand}.
     *
     * @return the {@link CollectedData}
     */
    List<CollectedData> getCollectedData();

    /**
     * Perform the actions which are owned by this {@link ComCommand}.
     *
     * @param deviceProtocol   the {@link DeviceProtocol} which will perform the actions
     * @param executionContext The ExecutionContext
     */
    void execute(DeviceProtocol deviceProtocol, ExecutionContext executionContext);

    /**
     * Add the given {@link CollectedData} to the collectedDataList.
     *
     * @param collectedData the {@link CollectedData} to add
     */
    void addCollectedDataItem(CollectedData collectedData);

    /**
     * All all the {@link CollectedData} items in the given list to the collectedDataList.
     *
     * @param collectedDataList all the {@link CollectedData} to add
     */
    void addListOfCollectedDataItems(List<? extends CollectedData> collectedDataList);

    /**
     * Gets the {@link CompletionCode} of this Command.
     *
     * @return The CompletionCode
     */
    CompletionCode getCompletionCode();

    void setCompletionCode(CompletionCode completionCode);

    /**
     * Gets the minimum LogLevel that needs to be activated
     * before this ComCommand must be logged.
     * As an example when LogLevel {@link com.energyict.mdc.engine.config.ComServer.LogLevel#INFO} is returned
     * then the ComServer's log level must be at least INFO or higher
     * before this ComCommand will actually be logged as a
     * ComCommandJournalEntryShadow.
     *
     * @return The minimum ComServer.LogLevel
     */
    LogLevel getJournalingLogLevel();

    /**
     * Converts this ComCommand to a String that will be used
     * as the human readable description for the
     * ComCommandJournalEntryShadow.
     *
     * @param serverLogLevel The LogLevel set on the ComServer
     * @return The human readable description of this ComCommand
     */
    String toJournalMessageDescription(LogLevel serverLogLevel);

    /**
     * Creates a human readable description of all issues;
     * If no issues occurred, then an empty string will be returned.
     *
     * @return The human readable description of the issues
     */
    String issuesToJournalMessageDescription();

}