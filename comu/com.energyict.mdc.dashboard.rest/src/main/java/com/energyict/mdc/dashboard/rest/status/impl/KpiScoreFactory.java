package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiScore;
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

    public KpiInfo getKpiAsInfo(DataCollectionKpi dataCollectionKpi) {
        KpiInfo kpiInfo = new KpiInfo();
        if (dataCollectionKpi.calculatesConnectionSetupKpi()) {
            kpiInfo.time = new ArrayList<>();
            kpiInfo.series = new ArrayList<>();
            KpiScoreInfo success = new KpiScoreInfo(MessageSeeds.SUCCESS.getKey());
            KpiScoreInfo ongoing = new KpiScoreInfo(MessageSeeds.ONGOING.getKey());
            KpiScoreInfo failed = new KpiScoreInfo(MessageSeeds.FAILED.getKey());
            KpiScoreInfo target = new KpiScoreInfo(MessageSeeds.TARGET.getKey());
            kpiInfo.series.add(success);
            kpiInfo.series.add(ongoing);
            kpiInfo.series.add(failed);
            kpiInfo.series.add(target);

            TemporalAmount frequency = dataCollectionKpi.connectionSetupKpiCalculationIntervalLength().get();
            Interval intervalByPeriod = getIntervalByPeriod(frequency);
            List<DataCollectionKpiScore> kpiScores = dataCollectionKpi.getConnectionSetupKpiScores(intervalByPeriod);
            Instant timeIndex = Instant.ofEpochMilli(intervalByPeriod.getStart().getTime());
            Instant endTimeIndex = Instant.ofEpochMilli(intervalByPeriod.getEnd().getTime());

            int kpiScoreIndex=0;
            while (timeIndex.isBefore(endTimeIndex)) {
                kpiInfo.time.add(timeIndex.getEpochSecond());
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
        }
        return kpiInfo;
    }

    private Interval getIntervalByPeriod(TemporalAmount temporalAmount) {
        LocalDate startDay=null;
        if (temporalAmount.getUnits().contains(ChronoUnit.SECONDS)) {
            if (temporalAmount.get(ChronoUnit.SECONDS) == Duration.ofMinutes(5).getSeconds()
                    || temporalAmount.get(ChronoUnit.SECONDS) == Duration.ofMinutes(15).getSeconds()
                    || temporalAmount.get(ChronoUnit.SECONDS) == Duration.ofMinutes(30).getSeconds()) {
                startDay = LocalDate.now(clock);
            } else if (temporalAmount.get(ChronoUnit.SECONDS) == Duration.ofHours(1).getSeconds()) {
                startDay = LocalDate.now(clock).with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
            } else if (temporalAmount.get(ChronoUnit.SECONDS) == Duration.ofDays(1).getSeconds()) {
                startDay = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
            }
        } else {
            if (temporalAmount.get(ChronoUnit.DAYS) == 1) {
                startDay = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
            } else if (temporalAmount.get(ChronoUnit.MONTHS) == 1) {
                startDay = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfYear());
            }
        }
        if (startDay==null) {
            throw exceptionFactory.newException(MessageSeeds.UNSUPPORTED_KPI_PERIOD);
        }

        return Interval.of(startDay.atStartOfDay().toInstant(ZoneOffset.UTC), Instant.now(clock));
    }

}
