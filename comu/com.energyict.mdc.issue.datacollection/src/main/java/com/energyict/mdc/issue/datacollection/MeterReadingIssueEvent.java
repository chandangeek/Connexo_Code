package com.energyict.mdc.issue.datacollection;

import com.elster.jupiter.issue.share.cep.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.issue.datacollection.impl.TrendPeriodUnit;
import com.google.common.base.Optional;
import org.joda.time.DateTimeConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class MeterReadingIssueEvent implements IssueEvent {
    private static final Logger LOG = Logger.getLogger(MeterReadingIssueEvent.class.getName());

    private final Meter meter;
    private final ReadingType readingType;
    private final IssueService issueService;
    private IssueStatus status;

    public MeterReadingIssueEvent(Meter meter, ReadingType readingType, IssueStatus status, IssueService issueService) {
        this.meter = meter;
        this.readingType = readingType;
        this.issueService = issueService;
        this.status = status;
    }

    @Override
    public String getEventType() {
        return "MeterReadingIssueEvent";
    }

    @Override
    public IssueStatus getStatus() {
        return status;
    }

    @Override
    public EndDevice getKoreDevice() {
        return meter;
    }

    @Override
    public Optional<? extends Issue> findExistingIssue(Issue baseIssue) {
        return null;
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
            LOG.warning("Unknown thrend period unit"); // TODO may be it will be better to throw exception?
            return 0d;
        }
        long trendPeriodInMillis = unit.getTrendPeriodInMillis(trendPeriod);
        List<? extends BaseReadingRecord> readings = meter.getReadings(
                new Interval(new Date(unit.getStartMillisForTrendPeriod(trendPeriod)), new Date()), readingType);
        if (!isValidReadings(readings, trendPeriodInMillis)) {
            //Nothing to do because at least two measurement points needed
            LOG.info("Device '" + getKoreDevice().getMRID() + "' hasn't enought readings (only " + readings.size() + ")");
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
            BigDecimal time = new BigDecimal(reading.getTimeStamp().getTime()).divide(new BigDecimal(DateTimeConstants.MILLIS_PER_HOUR), 10, RoundingMode.HALF_UP);
            sb.append("\n\treading with time = ").append(time.doubleValue()).append(" and value = ").append(reading.getValue());
            s1 = s1.add(time);
            s2 = s2.add(time.multiply(time));
            t0 = t0.add(reading.getValue());
            t1 = t1.add(time.multiply(reading.getValue()));
        }
        LOG.info("Processed readings:" + sb.toString());
        BigDecimal s0 = new BigDecimal(s0d);
        double result = Math.abs(s0.multiply(t1).subtract(s1.multiply(t0)).divide(s0.multiply(s2).subtract(s1.multiply(s1)), RoundingMode.HALF_UP).doubleValue());
        LOG.info("Slope for device '" + getKoreDevice().getMRID() + "' with " + readings.size() + " readings is: " + result);
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
            LOG.info("Expected readings count: " + expectedReadingsCount);
            return readings.size() >= 2 && readings.size() >= expectedReadingsCount;
        }
        return false;
    }
}
