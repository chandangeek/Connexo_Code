/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.common.tasks.TaskStatus;

import java.util.stream.Stream;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-04 (10:06)
 */
public enum TaskStatusTranslationKeys implements TranslationKey {

    PENDING(TaskStatus.Pending, "Pending"),
    PENDING_PRIO(TaskStatus.PendingWithPriority, "Pending with Priority"),
    FAILED(TaskStatus.Failed, "Failed"),
    BUSY(TaskStatus.Busy, "Busy"),
    ON_HOLD(TaskStatus.OnHold, "Active"),
    RETRYING(TaskStatus.Retrying, "Retrying"),
    RETRYING_PRIO(TaskStatus.RetryingWithPriority, "Retrying with Priority"),
    NEVER_COMPLETED(TaskStatus.NeverCompleted, "Never completed"),
    WAITING(TaskStatus.Waiting, "Waiting"),
    WAITING_PRIO(TaskStatus.WaitingWithPriority, "Waiting with Priority"),
    PROCESSING_ERROR(TaskStatus.ProcessingError, "Processing error");

    private TaskStatus taskStatus;
    private String defaultFormat;

    TaskStatusTranslationKeys(TaskStatus taskStatus, String defaultFormat) {
        this.taskStatus = taskStatus;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.taskStatus.name();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static TaskStatusTranslationKeys from(TaskStatus taskStatus) {
        return Stream
                .of(values())
                .filter(each -> each.taskStatus.equals(taskStatus))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Translation missing for task status: " + taskStatus));
    }

    public static String translationFor(TaskStatus taskStatus, Thesaurus thesaurus) {
        return thesaurus.getFormat(from(taskStatus)).format();
    }

}