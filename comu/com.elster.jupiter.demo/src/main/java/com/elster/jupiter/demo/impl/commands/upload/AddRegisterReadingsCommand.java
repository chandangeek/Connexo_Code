package com.elster.jupiter.demo.impl.commands.upload;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.demo.impl.commands.upload.time.IntervalReadingTimeProvider;
import com.elster.jupiter.demo.impl.commands.upload.time.TimeProvider;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class AddRegisterReadingsCommand extends ReadDataFromFileCommand {

    private Map<String, IntervalBlockImpl> blocks;
    private MeterReadingImpl meterReading;

    private TimeProvider timeProvider;

    @Inject
    public AddRegisterReadingsCommand(MeteringService meteringService) {
        super(meteringService);
        this.timeProvider = new IntervalReadingTimeProvider();
    }

    @Override
    protected void beforeParse() {
        super.beforeParse();
        this.blocks = new HashMap<>();
        meterReading = MeterReadingImpl.newInstance();
    }

    @Override
    protected void parseHeader(String header) {
        super.parseHeader(header);
        for (ReadingType type : getReadingTypes()) {
            IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(type.getMRID());
            blocks.put(type.getMRID(), intervalBlock);
        }
    }

    @Override
    protected void saveRecord(ReadingType readingType, String controlValue, Double value) {
        Instant timeForReading = this.timeProvider.getTimeForReading(readingType, getStart(), controlValue);
        meterReading.addReading(ReadingImpl.of(readingType.getMRID(), BigDecimal.valueOf(value), timeForReading));
        //System.out.println("\t" + timeForReading + " - (" + readingType.getMRID() + ") -\tvalue = " + value);
    }

    @Override
    protected void saveRecord(ReadingType readingType, String controlValue, String value) {
        Instant timeForReading = this.timeProvider.getTimeForReading(readingType, getStart(), controlValue);
        meterReading.addReading(ReadingImpl.of(readingType.getMRID(), value, timeForReading));
    }

    @Override
    protected void saveRecord(ReadingType readingType, String controlValue, Double value, String from, String to) {
        Instant timeForReading = this.timeProvider.getTimeForReading(readingType, getStart(), controlValue);
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        Instant fromInstant = null;
        try {
            fromInstant = format.parse(from).toInstant();
            Instant toInstant = format.parse(from).toInstant();
            meterReading.addReading(ReadingImpl.of(readingType.getMRID(), BigDecimal.valueOf(value), timeForReading, fromInstant, toInstant));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void afterParse() {
        super.afterParse();
        for (IntervalBlockImpl block : blocks.values()) {
            meterReading.addIntervalBlock(block);
        }
        getMeter().store(QualityCodeSystem.MDC, meterReading);
    }
}
