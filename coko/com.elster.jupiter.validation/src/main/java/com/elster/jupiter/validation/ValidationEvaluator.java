package com.elster.jupiter.validation;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by tgr on 5/09/2014.
 */
@ProviderType
public interface ValidationEvaluator {

	@Deprecated
	/*
	 * use DataValidationStatis.getValidationResult()
	 */
    default ValidationResult getValidationResult(Collection<? extends ReadingQuality> qualities) {
		return ValidationResult.getValidationResult(qualities);
	}

    boolean isAllDataValidated(MeterActivation meterActivation);
    boolean isAllDataValid(MeterActivation meterActivation);

    default List<DataValidationStatus> getValidationStatus(Channel channel, List<? extends BaseReading> readings) {
        List<DataValidationStatus> dataValidationStatuses;
        if (channel.getBulkQuantityReadingType().isPresent()) {
            dataValidationStatuses = getValidationStatus(channel.getCimChannel(channel.getMainReadingType()).get(),
                    channel.getCimChannel(channel.getBulkQuantityReadingType().get()).get(), readings);
        } else {
            dataValidationStatuses = getValidationStatus(channel.getCimChannel(channel.getMainReadingType()).get(), readings);
        }
        return dataValidationStatuses;
    }

    default List<DataValidationStatus> getValidationStatus(Channel channel, List<? extends BaseReading> readings, Range<Instant> interval) {
        List<DataValidationStatus> dataValidationStatuses;
        if (channel.getBulkQuantityReadingType().isPresent()) {
            dataValidationStatuses = getValidationStatus(channel.getCimChannel(channel.getMainReadingType()).get(),
                    channel.getCimChannel(channel.getBulkQuantityReadingType().get()).get(), readings, interval);
        } else {
            dataValidationStatuses = getValidationStatus(channel.getCimChannel(channel.getMainReadingType()).get(), readings, interval);
        }
        return dataValidationStatuses;
    }

    List<DataValidationStatus> getValidationStatus(CimChannel channel, List<? extends BaseReading> readings);

    List<DataValidationStatus> getValidationStatus(CimChannel channel, List<? extends BaseReading> readings, Range<Instant> interval);

    List<DataValidationStatus> getValidationStatus(CimChannel mainChannel, CimChannel bulkChannel, List<? extends BaseReading> readings);

    List<DataValidationStatus> getValidationStatus(CimChannel mainChannel, CimChannel bulkChannel, List<? extends BaseReading> readings, Range<Instant> interval);

    boolean isValidationEnabled(Meter meter);

    boolean isValidationOnStorageEnabled(Meter meter);

    boolean isValidationEnabled(Channel channel);

    Optional<Instant> getLastChecked(Meter meter, ReadingType readingType);
}
