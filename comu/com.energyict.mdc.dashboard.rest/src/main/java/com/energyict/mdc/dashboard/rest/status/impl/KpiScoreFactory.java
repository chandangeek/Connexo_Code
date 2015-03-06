package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.util.Ranges;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiScore;
import com.google.common.collect.Range;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
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

    public KpiInfo getKpiAsInfo(TemporalAmount frequency, List<DataCollectionKpiScore> kpiScores, Range<Instant> intervalByPeriod) {
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

        Instant timeIndex = intervalByPeriod.lowerEndpoint();
        Instant endTimeIndex = intervalByPeriod.upperEndpoint();

        int kpiScoreIndex = 0;
        kpiScores.add(new SentinelKpiScore());
        while (timeIndex.isBefore(endTimeIndex)) {
            kpiInfo.time.add(timeIndex.toEpochMilli());
            DataCollectionKpiScore kpiScore = kpiScores.get(kpiScoreIndex);
            if (kpiScore.getTimestamp().equals(timeIndex)) {
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

    public Range<Instant> getRangeByDisplayRange(TemporalAmount displayRange) {
        LocalDate today = LocalDate.now(clock);
        LocalDate startDay=null;
        LocalDate endDay=null;
        if (displayRange.getUnits().contains(ChronoUnit.SECONDS)) {
            if (displayRange.get(ChronoUnit.SECONDS) == Duration.ofHours(1).getSeconds()) {
                Instant startTime = LocalDateTime.now(clock).withMinute(0).withSecond(0).toInstant(ZoneOffset.UTC);
                Instant endTime = startTime.plus(1, ChronoUnit.HOURS);
                return Ranges.closed(startTime, endTime);
            } else if (displayRange.get(ChronoUnit.SECONDS) == Duration.ofDays(1).getSeconds()) {
                startDay = today;
                endDay = startDay;
            }
        } else {
            if (displayRange.get(ChronoUnit.DAYS) == 1) {
                startDay = today;
                endDay = startDay;
            } else if (displayRange.get(ChronoUnit.DAYS) == 7) {
                startDay = today.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
                endDay = today.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
            } else if (displayRange.get(ChronoUnit.DAYS) == 14) {
                startDay = today.with(TemporalAdjusters.previous(DayOfWeek.MONDAY)).with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
                endDay = today.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
            } else if (displayRange.get(ChronoUnit.MONTHS) == 1) {
                startDay = today.with(TemporalAdjusters.firstDayOfMonth());
                endDay = today.with(TemporalAdjusters.lastDayOfMonth());
            } else if (displayRange.get(ChronoUnit.YEARS) == 1) {
                startDay = today.with(TemporalAdjusters.firstDayOfYear());
                endDay = today.with(TemporalAdjusters.lastDayOfYear());
            }
        }
        if (startDay==null) {
            throw exceptionFactory.newException(MessageSeeds.UNSUPPORTED_KPI_PERIOD);
        }

        return Ranges.closed(
                startDay.atStartOfDay().toInstant(ZoneOffset.UTC),
                endDay.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
    }

    /**
     * Adding this sentinel at the end of array makes sure we never proceed down the kpiScore list (null object)
     */
    private class SentinelKpiScore implements DataCollectionKpiScore {

        @Override
        public Instant getTimestamp() {
            return Instant.EPOCH;
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