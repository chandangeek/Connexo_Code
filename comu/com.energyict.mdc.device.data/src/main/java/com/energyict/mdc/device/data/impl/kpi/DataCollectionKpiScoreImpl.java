package com.energyict.mdc.device.data.impl.kpi;

import com.elster.jupiter.kpi.KpiEntry;
import com.elster.jupiter.kpi.KpiMember;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiScore;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import java.math.BigDecimal;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Provides an implementation for the {@link DataCollectionKpiScore} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-07 (12:04)
 */
public class DataCollectionKpiScoreImpl implements DataCollectionKpiScore {
    private final Date timestamp;
    private final KpiMember targetMember;
    private final Map<TaskStatus, KpiEntry> entries = new EnumMap<>(TaskStatus.class);

    public DataCollectionKpiScoreImpl(Date timestamp, KpiMember targetMember, List<TaskStatus> statuses, List<KpiEntry> entries) {
        this.timestamp = timestamp;
        this.targetMember = targetMember;
        IntStream.
            range(0, statuses.size()).
            forEach(i ->
                this.entries.put(statuses.get(i), entries.get(i)));
    }

    @Override
    public Date getTimestamp() {
        return this.timestamp;
    }

    @Override
    public BigDecimal getTarget() {
        return this.targetMember.getTarget(this.timestamp);
    }

    @Override
    public boolean meetsTarget() {
        int compare = this.getTotal().compareTo(this.getTarget());
        if (this.targetMember.targetIsMinimum()) {
            return compare >= 0;
        }
        else {
            return compare <= 0;
        }
    }

    private BigDecimal getTotal () {
        return this.getValue(TaskStatus.Waiting).add(this.getValue(TaskStatus.Pending)).add(this.getValue(TaskStatus.Failed));
    }

    @Override
    public BigDecimal getValue(TaskStatus status) {
        KpiEntry kpiEntry = this.entries.get(status);
        if (kpiEntry != null) {
            return kpiEntry.getScore();
        }
        else {
            return BigDecimal.ZERO;
        }
    }

    @Override
    public int compareTo(DataCollectionKpiScore other) {
        return this.getTimestamp().compareTo(other.getTimestamp());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        DataCollectionKpiScoreImpl that = (DataCollectionKpiScoreImpl) other;
        return this.timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return this.timestamp.hashCode();
    }

    @Override
    public BigDecimal getSuccess() {
        return BigDecimal.ONE; // TODO implement me
    }

    @Override
    public BigDecimal getOngoing() {
        return BigDecimal.ONE; // TODO implement me
    }

    @Override
    public BigDecimal getFailed() {
        return BigDecimal.ONE; // TODO implement me
    }
}