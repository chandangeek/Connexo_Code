package com.energyict.mdc.issue.datacollection.event;

import com.elster.jupiter.issue.share.cep.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.issue.datacollection.impl.TrendPeriodUnit;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import org.joda.time.DateTimeConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MeterReadingEvent implements IssueEvent {
    private static final Logger LOG = Logger.getLogger(MeterReadingEvent.class.getName());

    private final Meter meter;
    private final ReadingType readingType;
    private final Clock clock;

    public MeterReadingEvent(Meter meter, ReadingType readingType, Clock clock) {
        this.meter = meter;
        this.readingType = readingType;
        this.clock = clock;
    }

    @Override
    public String getEventType() {
        return "MeterReadingIssueEvent";
    }

    @Override
    public EndDevice getEndDevice() {
        return meter;
    }

    @Override
    public Optional<? extends Issue> findExistingIssue() {
        return Optional.empty();
    }

    @Override
    public void apply(Issue issue) {
    }

    public ReadingType getReadingType() {
        return readingType;
    }

    public double computeMaxSlope(int trendPeriod, int trendPeriodUnitId) {
        TrendPeriodUnit unit = TrendPeriodUnit.getById(trendPeriodUnitId);
        if (unit == null) {
            LOG.warning("Unknown thrend period unit");
            return 0d;
        }
        long trendPeriodInMillis = unit.getTrendPeriodInMillis(trendPeriod);
        List<? extends BaseReadingRecord> readings =
                meter.getReadings(
                        Range.range(
                                Instant.ofEpochMilli(unit.getStartMillisForTrendPeriod(trendPeriod)), BoundType.CLOSED,
                                this.clock.instant(), BoundType.CLOSED),
                        readingType);
        if (!isValidReadings(readings, trendPeriodInMillis)) {
            //Nothing to do because at least two measurement points needed
            LOG.log(Level.INFO, "Device '" + getEndDevice().getMRID() + "' doesn't have enough readings (only " + readings.size() + ")");
            return 0d;
        }

        double s0d = 0;
        BigDecimal s1 = new BigDecimal(0),
                s2 = new BigDecimal(0),
                t0 = new BigDecimal(0),
                t1 = new BigDecimal(0);
        StringBuilder sb = new StringBuilder();
        for (BaseReadingRecord reading : readings) {
            s0d++;
            BigDecimal time = new BigDecimal(reading.getTimeStamp().toEpochMilli()).divide(new BigDecimal(DateTimeConstants.MILLIS_PER_HOUR), 10, RoundingMode.HALF_UP);
            sb.append("\n\treading with time = ").append(time.doubleValue()).append(" and value = ").append(reading.getValue());
            s1 = s1.add(time);
            s2 = s2.add(time.multiply(time));
            t0 = t0.add(reading.getValue());
            t1 = t1.add(time.multiply(reading.getValue()));
        }
        LOG.log(Level.INFO, "Processed readings:" + sb.toString());
        BigDecimal s0 = new BigDecimal(s0d);
        double result = Math.abs(s0.multiply(t1).subtract(s1.multiply(t0)).divide(s0.multiply(s2).subtract(s1.multiply(s1)), RoundingMode.HALF_UP).doubleValue());
        LOG.log(Level.INFO, "Slope for device '" + getEndDevice().getMRID() + "' with " + readings.size() + " readings is: " + result);
        return result;
    }

    private boolean isValidReadings(List<? extends BaseReadingRecord> readings, long trendPeriodInMillis) {
        if (readings != null) {
            Iterator<? extends BaseReadingRecord> itr = readings.iterator();
            while (itr.hasNext()) {
                BaseReadingRecord readingRecord = itr.next();
                if (readingRecord == null || readingRecord.getValue() == null) {
                    itr.remove();
                }
            }
            long readingInterval = ((long) readingType.getMeasuringPeriod().getMinutes()) * DateTimeConstants.MILLIS_PER_MINUTE;
            long expectedReadingsCount = (trendPeriodInMillis / readingInterval) / 4; // At least quarter of all expected readings
            LOG.log(Level.INFO, "Expected readings count: " + expectedReadingsCount);
            return readings.size() >= 2 && readings.size() >= expectedReadingsCount;
        }
        return false;
    }
}