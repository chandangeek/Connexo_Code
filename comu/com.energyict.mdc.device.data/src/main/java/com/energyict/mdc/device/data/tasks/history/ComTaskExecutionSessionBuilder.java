/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks.history;

import com.energyict.mdc.engine.config.ComServer;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface ComTaskExecutionSessionBuilder extends BuildsStatistics<ComTaskExecutionSessionBuilder> {

    ComSessionBuilder add(Instant stopDate, ComTaskExecutionSession.SuccessIndicator successIndicator);

    ComTaskExecutionSessionBuilder addComCommandJournalEntry(Instant timestamp, CompletionCode completionCode, String commandDescription);

    ComTaskExecutionSessionBuilder addComCommandJournalEntry(Instant timestamp, CompletionCode completionCode, String errorDesciption, String commandDescription);

    ComTaskExecutionSessionBuilder addComTaskExecutionMessageJournalEntry(Instant timestamp, ComServer.LogLevel logLevel, String message, String errorDesciption);

    void updateSuccessIndicator(ComTaskExecutionSession.SuccessIndicator successIndicator);

}