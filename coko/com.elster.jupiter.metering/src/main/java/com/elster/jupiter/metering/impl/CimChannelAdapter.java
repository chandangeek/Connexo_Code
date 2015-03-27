package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.either;

public class CimChannelAdapter extends AbstractCimChannel implements CimChannel {

    private final ChannelImpl channel;
    private final ReadingType readingType;
    private final MeteringService meteringService;

    CimChannelAdapter(ChannelImpl channel, ReadingType readingType, DataModel dataModel, MeteringService meteringService) {
        super(dataModel);
        this.channel = channel;
        this.readingType = readingType;
        this.meteringService = meteringService;
    }

    @Override
    public ChannelImpl getChannel() {
        return channel;
    }

    @Override
    public ReadingType getReadingType() {
        return readingType;
    }

    @Override
    public void editReadings(List<? extends BaseReading> readings) {
        if (readings.isEmpty()) {
            return;
        }
        ReadingStorer storer = meteringService.createOverrulingStorer();
        Range<Instant> range = readings.stream().map(BaseReading::getTimeStamp).map(Range::singleton).reduce(Range::span).get();
        List<ReadingQualityRecord> allQualityRecords = findReadingQuality(range);
        ReadingQualityType editQualityType = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.EDITGENERIC);
        ReadingQualityType addQualityType = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ADDED);
        for (BaseReading reading : readings) {
            List<ReadingQualityRecordImpl> currentQualityRecords = allQualityRecords.stream()
                    .filter(qualityRecord -> qualityRecord.getReadingTimestamp().equals(reading.getTimeStamp()))
                    .map(ReadingQualityRecordImpl.class::cast)
                    .collect(Collectors.toList());
            ProcessStatus processStatus = ProcessStatus.of(ProcessStatus.Flag.EDITED);
            Optional<BaseReadingRecord> oldReading = getReading(reading.getTimeStamp());
            boolean hasEditQuality = currentQualityRecords.stream().anyMatch(ReadingQualityRecordImpl::hasEditCategory);
            if (oldReading.isPresent()) {
                processStatus = processStatus.or(oldReading.get().getProcesStatus());
                if (!hasEditQuality) {
                    getChannel().createReadingQuality(editQualityType, getReadingType(), reading).save();
                }
            } else if (!hasEditQuality) {
                getChannel().createReadingQuality(addQualityType, getReadingType(), reading).save();
            }
            currentQualityRecords.stream()
                    .filter(ReadingQualityRecordImpl::isSuspect)
                    .forEach(ReadingQualityRecordImpl::delete);
            currentQualityRecords.stream()
                    .filter(either(ReadingQualityRecord::hasValidationCategory).or(ReadingQualityRecord::isMissing))
                    .forEach(ReadingQualityRecordImpl::makePast);
            storer.addReading(this, reading, processStatus);
        }
        storer.execute();
    }

    @Override
    public void removeReadings(List<? extends BaseReadingRecord> readings) {
        //TODO automatically generated method body, provide implementation.

    }

}
