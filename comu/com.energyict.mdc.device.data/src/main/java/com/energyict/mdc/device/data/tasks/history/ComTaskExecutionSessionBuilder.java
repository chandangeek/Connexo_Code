package com.energyict.mdc.device.data.tasks.history;

import com.energyict.mdc.engine.model.ComServer;

import java.time.Instant;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 30/04/2014
 * Time: 10:51
 */
public interface ComTaskExecutionSessionBuilder extends BuildsStatistics<ComTaskExecutionSessionBuilder> {

    ComSessionBuilder add(Instant stopDate, ComTaskExecutionSession.SuccessIndicator successIndicator);

    ComTaskExecutionSessionBuilder addComCommandJournalEntry(Instant timestamp, CompletionCode completionCode, String commandDescription);

    ComTaskExecutionSessionBuilder addComCommandJournalEntry(Instant timestamp, CompletionCode completionCode, String errorDesciption, String commandDescription);

    ComTaskExecutionSessionBuilder addComTaskExecutionMessageJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String message, String errorDesciption);

}