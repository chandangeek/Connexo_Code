package com.elster.jupiter.demo.impl.commands.upload;

import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class AddRegisterReadingsCommand extends ReadDataFromFileCommand {

    private Map<String, IntervalBlockImpl> blocks;
    private MeterReadingImpl meterReading;

    @Inject
    public AddRegisterReadingsCommand(MeteringService meteringService) {
        super(meteringService);
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
        Instant timeForReading = getTimeForReading(readingType, getStart(), controlValue);
        meterReading.addReading(ReadingImpl.of(readingType.getMRID(), BigDecimal.valueOf(value), timeForReading));

        //System.out.println("\t" + timeForReading + " - (" + readingType.getMRID() + ") -\tvalue = " + value);
    }

    @Override
    protected void afterParse() {
        super.afterParse();
        for (IntervalBlockImpl block : blocks.values()) {
            meterReading.addIntervalBlock(block);
        }
        getMeter().store(meterReading);
    }

    protected Instant getTimeForReading(ReadingType readingType, Instant startDate, String controlValue) {
        if (controlValue.length() != 9){
            throw new UnableToCreate("Incorrect control value for importing data. Should be 000-00:00");
        }
        startDate = startDate.plus(Integer.valueOf(controlValue.substring(0, 3)), ChronoUnit.DAYS);
        startDate = startDate.plus(Integer.valueOf(controlValue.substring(4, 6)), ChronoUnit.HOURS);
        return startDate.plus(Integer.valueOf(controlValue.substring(7, 9)), ChronoUnit.MINUTES);
    }
}
