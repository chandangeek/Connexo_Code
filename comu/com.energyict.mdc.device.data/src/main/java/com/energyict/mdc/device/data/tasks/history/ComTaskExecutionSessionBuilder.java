package com.energyict.mdc.device.data.tasks.history;

import com.energyict.mdc.engine.config.ComServer;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/**
 * Copyrights EnergyICT
 * Date: 30/04/2014
 * Time: 10:51
 */
@ProviderType
public interface ComTaskExecutionSessionBuilder extends BuildsStatistics<ComTaskExecutionSessionBuilder> {

    ComSessionBuilder add(Instant stopDate, ComTaskExecutionSession.SuccessIndicator successIndicator);

    ComTaskExecutionSessionBuilder addComCommandJournalEntry(Instant timestamp, CompletionCode completionCode, String commandDescription);

    ComTaskExecutionSessionBuilder addComCommandJournalEntry(Instant timestamp, CompletionCode completionCode, String errorDesciption, String commandDescription);

    ComTaskExecutionSessionBuilder addComTaskExecutionMessageJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String message, String errorDesciption);

    void updateSuccessIndicator(ComTaskExecutionSession.SuccessIndicator successIndicator);

}