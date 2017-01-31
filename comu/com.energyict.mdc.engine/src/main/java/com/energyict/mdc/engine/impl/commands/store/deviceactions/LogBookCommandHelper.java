/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LogBooksCommand;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBook;
import com.energyict.mdc.tasks.LogBooksTask;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class containing common methods that can be re-used for LogBookCommand and
 * LegacyLoadProfileLogBooksCommand
 *
 * @author sva
 * @since 10/06/2015 - 17:14
 */
public class LogBookCommandHelper {

    /**
     * Create {@link LogBookReader}q for this {@link LogBooksCommand}, based on the {@link LogBookType}s specified in the logBooksTask.
     * If no types are specified, then a {@link LogBookReader} for all
     * of the {@link LogBook}s of the device will be created.
     */
    public static List<LogBookReader> createLogBookReaders(CommandRoot.ServiceProvider serviceProvider, LogBooksTask logBooksTask, OfflineDevice device, ComTaskExecution comTaskExecution) {
        List<LogBookReader> logBookReaders = new ArrayList<>(device.getAllOfflineLogBooks().size());
        List<OfflineLogBook> listOfAllLogBooks = device.getAllOfflineLogBooks();
        if (logBooksTask.getLogBookTypes().isEmpty()) {
            for (OfflineLogBook logBook : listOfAllLogBooks) {
                if (comTaskExecution.getDevice().getId() == logBook.getDeviceId()) {
                    logBookReaders.add(createLogBookReader(serviceProvider.clock(), logBook));
                }
            }
        } else {
            for (LogBookType logBookType : logBooksTask.getLogBookTypes()) {
                for (OfflineLogBook logBook : listOfAllLogBooks) {
                    if (logBookType.getId() == logBook.getLogBookTypeId()) {
                        if (comTaskExecution.getDevice().getId() == logBook.getDeviceId()) {
                            logBookReaders.add(createLogBookReader(serviceProvider.clock(), logBook));
                        }
                    }
                }
            }
        }
        return logBookReaders;
    }

    /**
     * Create a proper {@link LogBookReader} for the given {@link OfflineLogBook}
     */
    private static LogBookReader createLogBookReader(final Clock clock, final OfflineLogBook logBook) {
        return new LogBookReader(
                clock,
                logBook.getObisCode(),
                logBook.getLastLogBook(),
                logBook.getLogBookIdentifier(),
                logBook.getDeviceIdentifier(),
                logBook.getMasterSerialNumber());
    }

    /**
     * Check whether the given list of logBookReaders already contain this LogBookReader or not
     *
     * @param newReader the LogBookReader to check
     * @return true if it does not exist yet, false otherwise
     */
    public static boolean canWeAddIt(final List<LogBookReader> logBookReaders, final LogBookReader newReader) {
        for (LogBookReader existingReader : logBookReaders) {
            if (sameLogBookObisCode(newReader, existingReader) || sameLogBook(newReader, existingReader)) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameLogBookObisCode(LogBookReader newReader, LogBookReader existingReader) {
        return existingReader.getLogBookObisCode().equals(newReader.getLogBookObisCode());
    }

    private static boolean sameLogBook(LogBookReader newReader, LogBookReader existingReader) {
        return existingReader.getLogBookIdentifier().equals(newReader.getLogBookIdentifier());
    }
}