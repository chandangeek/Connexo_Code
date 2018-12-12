/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.LogBooksTask;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to delete a {@link LogBookType} while it is still being used by
 * at least one {@link LogBooksTask}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-21 (10:56)
 */
public class VetoDeleteLogBookTypeException extends LocalizedException {

    public VetoDeleteLogBookTypeException(Thesaurus thesaurus, LogBookType logBookType, List<LogBooksTask> clients) {
        super(thesaurus, MessageSeeds.VETO_LOG_BOOK_TYPE_DELETION, logBookType.getName(), asString(clients));
    }

    private static String asString(List<LogBooksTask> clients) {
        return clients.stream()
                .map(LogBooksTask::getComTask)
                .map(ComTask::getName)
                .collect(Collectors.joining(", "));
    }

}