package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiScore;
import java.time.DayOfWeek;
import java.time.Duration;
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

    @Inject
    public KpiScoreFactory(ExceptionFactory exceptionFactory) {
        this.exceptionFactory = exceptionFactory;
    }

    public List<KpiScoreInfo> getKpiAsInfo(DataCollectionKpi dataCollectionKpi) {
        List<KpiScoreInfo> kpi = null;
        if (dataCollectionKpi.calculatesConnectionSetupKpi()) {
            kpi = new ArrayList<>();
            KpiScoreInfo success = new KpiScoreInfo(MessageSeeds.SUCCESS.getKey());
            KpiScoreInfo ongoing = new KpiScoreInfo(MessageSeeds.ONGOING.getKey());
            KpiScoreInfo failed = new KpiScoreInfo(MessageSeeds.FAILED.getKey());
            KpiScoreInfo target = new KpiScoreInfo(MessageSeeds.TARGET.getKey());
            kpi.add(success);
            kpi.add(ongoing);
            kpi.add(failed);
            kpi.add(target);

            List<DataCollectionKpiScore> kpiScores = dataCollectionKpi.getConnectionSetupKpiScores(getIntervalByPeriod(dataCollectionKpi.connectionSetupKpiCalculationIntervalLength().get()));
            for (DataCollectionKpiScore kpiScore : kpiScores) {
                success.addKpi(kpiScore.getTimestamp(), kpiScore.getSuccess());
                ongoing.addKpi(kpiScore.getTimestamp(), kpiScore.getOngoing());
                failed.addKpi(kpiScore.getTimestamp(), kpiScore.getFailed());
                target.addKpi(kpiScore.getTimestamp(), kpiScore.getTarget());
            }
        }
        return kpi;
    }

    private Interval getIntervalByPeriod(TemporalAmount temporalAmount) {
        LocalDate startDay=null;
        if (temporalAmount.getUnits().contains(ChronoUnit.SECONDS)) {
            if (temporalAmount.get(ChronoUnit.SECONDS) == Duration.ofMinutes(5).getSeconds()
                    || temporalAmount.get(ChronoUnit.SECONDS) == Duration.ofMinutes(15).getSeconds()
                    || temporalAmount.get(ChronoUnit.SECONDS) == Duration.ofMinutes(30).getSeconds()) {
                startDay = LocalDate.now();
            } else if (temporalAmount.get(ChronoUnit.SECONDS) == Duration.ofHours(1).getSeconds()) {
                startDay = LocalDate.now().with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
            } else if (temporalAmount.get(ChronoUnit.SECONDS) == Duration.ofDays(1).getSeconds()) {
                startDay = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
            }
        } else {
            if (temporalAmount.get(ChronoUnit.DAYS) == 1) {
                startDay = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
            } else if (temporalAmount.get(ChronoUnit.MONTHS) == 1) {
                startDay = LocalDate.now().with(TemporalAdjusters.firstDayOfYear());
            }
        }
        if (startDay==null) {
            throw exceptionFactory.newException(MessageSeeds.UNSUPPORTED_KPI_PERIOD);
        }

        return Interval.startAt(Date.from(startDay.atStartOfDay().toInstant(ZoneOffset.UTC)));
    }

}
