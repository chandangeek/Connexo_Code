package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.validation.DataValidationAssociationProvider;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Collectors;

@Component(name="com.energyict.mdc.device.data.impl.DataValidationMdcAssociationProvider",
        service = { DataValidationAssociationProvider.class },
        immediate = true)
public class DataValidationMdcAssociationProvider implements DataValidationAssociationProvider {

    private volatile DeviceService deviceService;
    private volatile Clock clock;

    public DataValidationMdcAssociationProvider() {

    }

    @Inject
    public DataValidationMdcAssociationProvider(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public BigDecimal getRegisterSuspects(String mRID, Range<Instant> range) {
        Optional<Device> device = deviceService.findByUniqueMrid(mRID);
        if (device.isPresent()) {
            long registerSuspects = device.get()
                    .getRegisters()
                    .stream()
                    .map(reg -> (device.get()
                            .forValidation()
                            .getValidationStatus(reg, Collections.emptyList(), range)
                            .stream())
                            .filter(s -> (s.getReadingQualities()
                                    .stream()
                                    .anyMatch(q -> q.getType()
                                            .qualityIndex()
                                            .orElse(QualityCodeIndex.DATAVALID)
                                            .equals(QualityCodeIndex.SUSPECT))))
                            .count())
                    .filter(m -> m > 0L)
                    .mapToLong(Long::longValue)
                    .sum();

            List<DataValidationStatus> yy = device.get()
                    .getRegisters()
                    .stream()
                    .map(reg -> (device.get()
                                    .forValidation()
                                    .getValidationStatus(reg, Collections.emptyList(), range)
                                    .stream())
                                    .filter(s -> (s.getReadingQualities()
                                            .stream()
                                            .anyMatch(q -> q.getType()
                                                    .qualityIndex()
                                                    .orElse(QualityCodeIndex.DATAVALID)
                                                    .equals(QualityCodeIndex.SUSPECT))))
                                    .collect(Collectors.toList())
//                            .map(DataValidationStatus::getReadingTimestamp)
//                            .mapToLong(Instant::getEpochSecond)
//                            .max()
//                            .getAsLong()
                    ).flatMap(Collection::stream).collect(Collectors.toList());

            OptionalLong zz = yy.stream().mapToLong(v -> v.getReadingTimestamp().getEpochSecond()).max();
            return new BigDecimal(registerSuspects);
        }
        return new BigDecimal(0);
    }

    @Override
    public BigDecimal getChannelsSuspects(String mRID, Range<Instant> range) {
        Optional<Device> device = deviceService.findByUniqueMrid(mRID);
        if (device.isPresent()) {
            long channelsSuspects = device.get()
                    .getLoadProfiles()
                    .stream()
                    .map(lp ->
                            lp.getChannels().stream()
                                    .flatMap(c -> c.getDevice()
                                            .forValidation()
                                            .getValidationStatus(c, Collections.emptyList(), range)
                                            .stream())
                                    .filter(s -> (s.getReadingQualities()
                                            .stream()
                                            .anyMatch(q -> q.getType()
                                                    .qualityIndex()
                                                    .orElse(QualityCodeIndex.DATAVALID)
                                                    .equals(QualityCodeIndex.SUSPECT))))
                                    .count())
                    .filter(m -> m > 0L)
                    .mapToLong(Long::longValue)
                    .sum();

            return new BigDecimal(channelsSuspects);
        }
        return new BigDecimal(0);
    }


    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }
}
