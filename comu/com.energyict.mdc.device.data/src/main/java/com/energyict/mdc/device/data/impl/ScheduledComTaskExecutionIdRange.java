package com.energyict.mdc.device.data.impl;

/**
* Models a range of ids of ComTaskExecutionss
* that are all using the same {@link com.energyict.mdc.scheduling.model.ComSchedule}.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2014-07-08 (17:28)
*/
public class ScheduledComTaskExecutionIdRange {

    public long comScheduleId;
    public long minId;
    public long maxId;

    public ScheduledComTaskExecutionIdRange(long comScheduleId, long minId, long maxId) {
        this.comScheduleId = comScheduleId;
        this.minId = minId;
        this.maxId = maxId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ScheduledComTaskExecutionIdRange idRange = (ScheduledComTaskExecutionIdRange) o;

        if (comScheduleId != idRange.comScheduleId) {
            return false;
        }
        if (maxId != idRange.maxId) {
            return false;
        }
        if (minId != idRange.minId) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (comScheduleId ^ (comScheduleId >>> 32));
        result = 31 * result + (int) (minId ^ (minId >>> 32));
        result = 31 * result + (int) (maxId ^ (maxId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ScheduledComTaskExecutions related to ComSchedule " + comScheduleId + " with id in range from " + minId + " to " + maxId;
    }

}