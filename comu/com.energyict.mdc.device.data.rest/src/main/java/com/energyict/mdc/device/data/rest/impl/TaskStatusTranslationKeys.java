package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.tasks.TaskStatus;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.util.stream.Stream;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-04 (10:06)
 */
public enum TaskStatusTranslationKeys implements TranslationKey {

    PENDING(TaskStatus.Pending, "Pending"),
    FAILED(TaskStatus.Failed, "Failed"),
    BUSY(TaskStatus.Busy, "Busy"),
    ON_HOLD(TaskStatus.OnHold, "Inactive"),
    RETRYING(TaskStatus.Retrying, "Retrying"),
    NEVER_COMPLETED(TaskStatus.NeverCompleted, "Never completed"),
    WAITING(TaskStatus.Waiting, "Waiting");

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