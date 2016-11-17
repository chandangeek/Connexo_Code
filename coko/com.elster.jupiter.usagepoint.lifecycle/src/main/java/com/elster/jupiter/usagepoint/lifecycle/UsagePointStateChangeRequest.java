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

    Status getStatus();

    UsagePoint getUsagePoint();

    String getUsagePointTransition();

    User getOriginator();

    Instant getTransitionTime();

    Map<String, Object> getProperties();

    Instant getScheduleTime();

    String getGeneralFailReason();

    List<UsagePointStateChangeFail> getFailReasons();

    void cancel();

}
