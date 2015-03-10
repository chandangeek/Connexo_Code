package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;
import java.util.Optional;

public interface EstimationTask extends HasName, HasAuditInfo {

    long getId();

    void activate(); // resume

    void deactivate(); // suspend

//    RelativePeriod getExportPeriod();

    Optional<Instant> getLastRun();

    EndDeviceGroup getEndDeviceGroup();

    Instant getNextExecution();

    void save();

    void delete();

    boolean isActive();

    ScheduleExpression getScheduleExpression();

//    Optional<? extends DataExportOccurrence> getLastOccurrence();
//
//    Optional<? extends DataExportOccurrence> getOccurrence(Long id);

    void setNextExecution(Instant instant);

    void setScheduleExpression(ScheduleExpression scheduleExpression);

    void setName(String name);

//    void setExportPeriod(RelativePeriod relativePeriod);

    void setEndDeviceGroup(EndDeviceGroup endDeviceGroup);

    void triggerNow();

    void updateLastRun(Instant triggerTime);

}
