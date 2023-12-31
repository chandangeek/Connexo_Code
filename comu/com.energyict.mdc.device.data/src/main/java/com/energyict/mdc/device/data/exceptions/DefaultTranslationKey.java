/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.TranslationKey;

public enum DefaultTranslationKey implements TranslationKey {
    COM_SCHEDULE_ADDED("AddComSchedule" , "Added ComSchedule {0} on device {1}"),
    COM_SCHEDULE_REMOVED("RemoveComSchedule" , "Removed ComSchedule {0} from device {1}"),
    COM_SCHEDULE_UNABLE_TO_ADD("UnableAddComSchedule", "Unable to add ComSchedules {0} on device {1} because there are conflicts with another schedule")
    ;

    private String key;
    private String defaultFormat;

    DefaultTranslationKey(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }
}
