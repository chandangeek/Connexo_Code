package com.elster.jupiter.demo.impl.commands.upload;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AddChannelReadingsCommand extends ReadDataFromFileCommand {

    private final DeviceService deviceService;

    private Map<String, IntervalBlockImpl> blocks;
    private MeterReadingImpl meterReading;

    public AddChannelReadingsCommand(MeteringService meteringService, DeviceService deviceService) {
        super(meteringService);
        this.deviceService = deviceService;
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
        IntervalReadingImpl intervalReading = IntervalReadingImpl.of(timeForReading, BigDecimal.valueOf(value), Collections.<ReadingQuality>emptyList());
        blocks.get(readingType.getMRID()).addIntervalReading(intervalReading);

        //System.out.println("\t" + timeForReading + " - (" + readingType.getMRID() + ") -\tvalue = " + value);
    }
    
    @Override
    protected void afterParse() {
        super.afterParse();
        for (IntervalBlockImpl block : blocks.values()) {
            meterReading.addIntervalBlock(block);
        }
        getMeter().store(QualityCodeSystem.MDC, meterReading);
        setLastReadingTypeForLoadProfile(Long.parseLong(getMeter().getAmrId()));
    }

    protected abstract Instant getTimeForReading(ReadingType readingType, Instant startDate, String controlValue);

    private void setLastReadingTypeForLoadProfile(long deviceId) {
        deviceService.findDeviceById(deviceId).ifPresent(device -> {
            for (LoadProfile loadProfile : device.getLoadProfiles()) {
                LoadProfile.LoadProfileUpdater updater = device.getLoadProfileUpdaterFor(loadProfile);
                for (Channel channel : loadProfile.getChannels()) {
                    channel.getLastDateTime().ifPresent(updater::setLastReadingIfLater);
                }
                updater.update();
            }
        });
    }
}
