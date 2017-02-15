/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.tasks.history.ComCommandJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionMessageJournalEntry;

/**
 * Created by bvn on 10/14/14.
 */
public class JournalEntryInfoFactory {
    public JournalEntryInfo asInfo(ComTaskExecutionJournalEntry comTaskExecutionJournalEntry) {
        JournalEntryInfo info = new JournalEntryInfo();
        info.timestamp=comTaskExecutionJournalEntry.getTimestamp();
        info.logLevel=comTaskExecutionJournalEntry.getLogLevel();
        if (comTaskExecutionJournalEntry instanceof ComTaskExecutionMessageJournalEntry) {
            info.details=((ComTaskExecutionMessageJournalEntry)comTaskExecutionJournalEntry).getMessage();
        } else if (comTaskExecutionJournalEntry instanceof ComCommandJournalEntry) {
            info.details=((ComCommandJournalEntry)comTaskExecutionJournalEntry).getCommandDescription();
        }

        info.errorDetails=comTaskExecutionJournalEntry.getErrorDescription();
        return info;
    }

    public JournalEntryInfo asInfo(ComSessionJournalEntry comSessionJournalEntry) {
        JournalEntryInfo info = new JournalEntryInfo();
        info.timestamp=comSessionJournalEntry.getTimestamp();
        info.logLevel=comSessionJournalEntry.getLogLevel();
        info.details=comSessionJournalEntry.getMessage();
        info.errorDetails=comSessionJournalEntry.getStackTrace();
        return info;
    }

    public JournalEntryInfo asInfo(ComSession.CombinedLogEntry combinedLogEntry) {
        JournalEntryInfo info = new JournalEntryInfo();
        info.timestamp=combinedLogEntry.getTimestamp();
        info.logLevel=combinedLogEntry.getLogLevel();
        info.details=combinedLogEntry.getDetail();
        info.errorDetails=combinedLogEntry.getErrorDetail();
        return info;
    }
}
