package com.energyict.mdc.device.data.tasks.history;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 30/04/2014
 * Time: 10:51
 */
public interface ComTaskExecutionSessionBuilder extends BuildsStatistics<ComTaskExecutionSessionBuilder> {

    ComSessionBuilder add(Date stopDate, ComTaskExecutionSession.SuccessIndicator successIndicator);

    ComTaskExecutionSessionBuilder addComCommandJournalEntry(Date timestamp, CompletionCode completionCode, String errorDesciption, String commandDescription);

    ComTaskExecutionSessionBuilder addComTaskExecutionMessageJournalEntry(Date timestamp, String errorDesciption, String message);
}
