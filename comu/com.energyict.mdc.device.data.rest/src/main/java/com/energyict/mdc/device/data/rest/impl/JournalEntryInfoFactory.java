package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionJournalEntry;

/**
 * Created by bvn on 10/14/14.
 */
public class JournalEntryInfoFactory {
    public JournalEntryInfo asInfo(ComTaskExecutionJournalEntry comTaskExecutionJournalEntry) {
        JournalEntryInfo info = new JournalEntryInfo();
        info.details=comTaskExecutionJournalEntry.getErrorDescription();
        info.logLevel=comTaskExecutionJournalEntry.getLogLevel();
        info.timestamp=comTaskExecutionJournalEntry.getTimestamp();
        return info;
    }

    public JournalEntryInfo asInfo(ComSessionJournalEntry comSessionJournalEntry) {
        JournalEntryInfo info = new JournalEntryInfo();
        info.timestamp=comSessionJournalEntry.getTimestamp();
        info.logLevel=comSessionJournalEntry.getLogLevel();
        info.details=comSessionJournalEntry.getMessage();
        return info;
    }

    public JournalEntryInfo asInfo(ComSession.CombinedLogEntry combinedLogEntry) {
        JournalEntryInfo info = new JournalEntryInfo();
        info.timestamp=combinedLogEntry.getTimestamp();
        info.logLevel=combinedLogEntry.getLogLevel();
        info.details=combinedLogEntry.getDetail();
        return info;
    }
}
