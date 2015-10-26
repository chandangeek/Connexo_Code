package com.elster.jupiter.export.security;

import com.elster.jupiter.nls.TranslationKey;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {

    ADMINISTRATE_DATA_EXPORT_TASK(Constants.ADMINISTRATE_DATA_EXPORT_TASK, "Administrate"),
    VIEW_DATA_EXPORT_TASK(Constants.VIEW_DATA_EXPORT_TASK, "View"),
    UPDATE_DATA_EXPORT_TASK(Constants.UPDATE_DATA_EXPORT_TASK, "Update"),
    UPDATE_SCHEDULE_DATA_EXPORT_TASK(Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, "Update schedule"),
    RUN_DATA_EXPORT_TASK(Constants.RUN_DATA_EXPORT_TASK, "Run");

    private final String key;
    private final String description;

    Privileges(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return getDescription();
    }

    public String getDescription() {
        return description;
    }

    public static String[] keys() {
        return Arrays.stream(Privileges.values())
                .map(Privileges::getKey)
                .collect(Collectors.toList())
                .toArray(new String[Privileges.values().length]);
    }

    public interface Constants {
        String ADMINISTRATE_DATA_EXPORT_TASK = "privilege.administrate.dataExportTask";
        String VIEW_DATA_EXPORT_TASK = "privilege.view.dataExportTask";
        String UPDATE_DATA_EXPORT_TASK = "privilege.update.dataExportTask";
        String UPDATE_SCHEDULE_DATA_EXPORT_TASK = "privilege.update.schedule.dataExportTask";
        String RUN_DATA_EXPORT_TASK = "privilege.run.dataExportTask";
    }


}