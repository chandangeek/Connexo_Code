/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.HasId;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface UsagePointStateChangeRequest extends HasId {
    enum Status {
        COMPLETED,
        FAILED,
        SCHEDULED,
        CANCELLED
    }

    enum Type {
        STATE_CHANGE("state"),;

        private final String key;

        Type(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    Status getStatus();

    String getStatusName();

    Type getType();

    String getTypeName();

    UsagePoint getUsagePoint();

    String getFromStateName();

    String getToStateName();

    User getOriginator();

    Instant getTransitionTime();

    Map<String, Object> getProperties();

    Instant getScheduleTime();

    String getGeneralFailReason();

    List<UsagePointStateChangeFail> getFailReasons();

    boolean userCanManageRequest(String application);

    void cancel();

}
