package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiScore;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by bvn on 10/14/14.
 */
public class KpiScoreFactory {

    private final ExceptionFactory exceptionFactory;
    private final Clock clock;

    @Inject
    public KpiScoreFactory(ExceptionFactory exceptionFactory, Clock clock) {
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
    }

    public KpiInfo getKpiAsInfo(TemporalAmount frequency, List<DataCollectionKpiScore> kpiScores, Interval intervalByPeriod) {
        KpiInfo kpiInfo = new KpiInfo();
        kpiInfo.time = new ArrayList<>();
        kpiInfo.series = new ArrayList<>();
        KpiScoreInfo success = new KpiScoreInfo(KpiId.Success.name());
        KpiScoreInfo ongoing = new KpiScoreInfo(KpiId.Ongoing.name());
        KpiScoreInfo failed = new KpiScoreInfo(KpiId.Failed.name());
        KpiScoreInfo target = new KpiScoreInfo(KpiId.Target.name());
        kpiInfo.series.add(success);
        kpiInfo.series.add(ongoing);
        kpiInfo.series.add(failed);
        kpiInfo.series.add(target);

        Instant timeIndex = intervalByPeriod.getStart();
        Instant endTimeIndex = intervalByPeriod.getEnd();

        int kpiScoreIndex=0;
        kpiScores.add(new SentinelKpiScore());
        while (timeIndex.isBefore(endTimeIndex)) {
            kpiInfo.time.add(Date.from(timeIndex).getTime());
            DataCollectionKpiScore kpiScore = kpiScores.get(kpiScoreIndex);
            if (kpiScore.getTimestamp().toInstant().equals(timeIndex)) {
                success.data.add(kpiScore.getSuccess());
                ongoing.data.add(kpiScore.getOngoing());
                failed.data.add(kpiScore.getFailed());
                target.data.add(kpiScore.getTarget());
                kpiScoreIndex++;
            } else {
                success.data.add(null);
                ongoing.data.add(null);
                failed.data.add(null);
                target.data.add(null);
            }
            timeIndex = timeIndex.plus(frequency);
        }
        return kpiInfo;
    }

    public Interval getIntervalByPeriod(TemporalAmount temporalAmount) {
        LocalDate startDay=null;
        LocalDate endDay=null;
        if (temporalAmount.getUnits().contains(ChronoUnit.SECONDS)) {
            if (temporalAmount.get(ChronoUnit.SECONDS) == Duration.ofMinutes(5).getSeconds()
                    || temporalAmount.get(ChronoUnit.SECONDS) == Duration.ofMinutes(15).getSeconds()
                    || temporalAmount.get(ChronoUnit.SECONDS) == Duration.ofMinutes(30).getSeconds()) {
                startDay = LocalDate.now(clock);
                endDay = LocalDate.now(clock);
            } else if (temporalAmount.get(ChronoUnit.SECONDS) == Duration.ofHours(1).getSeconds()) {
                startDay = LocalDate.now(clock).with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
                endDay = LocalDate.now(clock).with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
            } else if (temporalAmount.get(ChronoUnit.SECONDS) == Duration.ofDays(1).getSeconds()) {
                startDay = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
                endDay = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
            }
        } else {
            if (temporalAmount.get(ChronoUnit.DAYS) == 1) {
                startDay = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
                endDay = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth());
            } else if (temporalAmount.get(ChronoUnit.MONTHS) == 1) {
                startDay = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfYear());
                endDay = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfYear());
            }
        }
        if (startDay==null) {
            throw exceptionFactory.newException(MessageSeeds.UNSUPPORTED_KPI_PERIOD);
        }

        return Interval.of(startDay.atStartOfDay().toInstant(ZoneOffset.UTC), endDay.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
    }

    /**
     * Adding this sentinel at the end of array makes sure we never proceed down the kpiScore list (null object)
     */
    private class SentinelKpiScore implements DataCollectionKpiScore {

        @Override
        public Date getTimestamp() {
            return Date.from(Instant.EPOCH);
        }

        @Override
        public BigDecimal getTarget() {
            return null;
        }

        @Override
        public boolean meetsTarget() {
            return false;
        }

        @Override
        public BigDecimal getSuccess() {
            return null;
        }

        @Override
        public BigDecimal getOngoing() {
            return null;
        }

        @Override
        public BigDecimal getFailed() {
            return null;
        }

        @Override
        public int compareTo(DataCollectionKpiScore o) {
            return 0;
        }
    }
}
