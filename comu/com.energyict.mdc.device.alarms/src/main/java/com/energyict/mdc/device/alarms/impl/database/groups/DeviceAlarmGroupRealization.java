/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.database.groups;


import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import java.util.Optional;

public enum DeviceAlarmGroupRealization {
    REASON {
        @Override
        public String getKey() {
            return "reasonsPerDay";
        }

        @Override
        DeviceAlarmGroupOperation getOperation(DataModel dataModel, Thesaurus thesaurus) {
            return new GroupByReasonImpl(dataModel, thesaurus);
        }
    },
    OPENVSCLOSE {
        @Override
        public String getKey() {
            return "openVsClose";
        }

        @Override
        DeviceAlarmGroupOperation getOperation(DataModel dataModel, Thesaurus thesaurus) {
            return new OpenVsCloseImpl(dataModel, thesaurus);
        }
    };

    abstract String getKey();

    abstract DeviceAlarmGroupOperation getOperation(DataModel dataModel, Thesaurus thesaurus);

    public static Optional<DeviceAlarmGroupRealization> of(String text) {
        if (text != null) {
            for (DeviceAlarmGroupRealization groupByRealization : DeviceAlarmGroupRealization.values()) {
                if (groupByRealization.getKey().equalsIgnoreCase(text)) {
                    return Optional.of(groupByRealization);
                }
            }
        }
        return Optional.empty();
    }
}
