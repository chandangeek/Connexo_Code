package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.validation.DataValidationAssociationProvider;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name="com.energyict.mdc.device.data.impl.DataValidationMdcAssociationProvider",
        service = { DataValidationAssociationProvider.class },
        immediate = true)
@SuppressWarnings("unused")
public class DataValidationMdcAssociationProvider implements DataValidationAssociationProvider {

    private volatile DeviceService deviceService;
    private volatile Clock clock;

    public DataValidationMdcAssociationProvider() {
        // nothing to do
    }

    @Inject
    public DataValidationMdcAssociationProvider(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public List<DataValidationStatus> getRegisterSuspects(EndDevice endDevice, Range<Instant> range) {
        return findDeviceByEndDevice(endDevice)
                .map(device -> getDataValidationStatusForRegister(device, range))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<DataValidationStatus> getChannelsSuspects(EndDevice endDevice, Range<Instant> range) {
        return findDeviceByEndDevice(endDevice)
                .map(device -> getDataValidationStatusForChannels(device, range))
                .orElse(Collections.emptyList());
    }

    @Override
    public boolean isAllDataValidated(EndDevice endDevice, Range<Instant> range) {
        return findDeviceByEndDevice(endDevice)
                .map(device -> isAllDataValidated(device, range))
                .orElse(false);
    }

    private Optional<Device> findDeviceByEndDevice(EndDevice device) {
        if (device.getAmrSystem().getId() == KnownAmrSystem.MDC.getId()) {
            return deviceService.findDeviceById(Long.valueOf(device.getAmrId()));
        }
        return Optional.empty();
    }

    private List<DataValidationStatus> getDataValidationStatusForRegister(Device device, Range<Instant> range){
        return device.getRegisters()
                .stream()
                .map(register -> (device.forValidation()
                        .getValidationStatus(register, Collections.emptyList(), range)
                        .stream())
                        .filter(dataValidationStatus -> (dataValidationStatus.getReadingQualities()
                                .stream()
                                .anyMatch(readingQuality -> readingQuality.getType()
                                        .qualityIndex()
                                        .orElse(QualityCodeIndex.DATAVALID)
                                        .equals(QualityCodeIndex.SUSPECT))))
                        .collect(Collectors.toList())
                ).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private List<DataValidationStatus> getDataValidationStatusForChannels(Device device, Range<Instant> range){
        return device.getLoadProfiles()
                .stream()
                .map(loadProfile ->
                        loadProfile.getChannels().stream()
                                .flatMap(channel -> channel.getDevice()
                                        .forValidation()
                                        .getValidationStatus(channel, Collections.emptyList(), range)
                                        .stream())
                                .filter(dataValidationStatus -> (dataValidationStatus.getReadingQualities()
                                        .stream()
                                        .anyMatch(readingQuality -> readingQuality.getType()
                                                .qualityIndex()
                                                .orElse(QualityCodeIndex.DATAVALID)
                                                .equals(QualityCodeIndex.SUSPECT))))
                                .collect(Collectors.toList()))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    private boolean isAllDataValidated(Device device, Range<Instant> range){
        boolean isValidated;
        List<DataValidationStatus> lpStatuses = device.getLoadProfiles().stream()
                .flatMap(l -> l.getChannels().stream())
                .flatMap(c -> c.getDevice().forValidation().getValidationStatus(c, Collections.emptyList(), range).stream())
                .collect(Collectors.toList());

        isValidated = device.getLoadProfiles().stream()
                .flatMap(l -> l.getChannels().stream())
                .allMatch(r -> r.getDevice().forValidation().allDataValidated(r, clock.instant()));

        isValidated &= lpStatuses.stream()
                .allMatch(DataValidationStatus::completelyValidated);

        List<DataValidationStatus> rgStatuses = device.getRegisters().stream()
                .flatMap(r -> device.forValidation().getValidationStatus(r, Collections.emptyList(), range).stream())
                .collect(Collectors.toList());
        isValidated &= device.getRegisters().stream()
                .allMatch(r -> r.getDevice().forValidation().allDataValidated(r, clock.instant()));
        isValidated &= rgStatuses.stream()
                .allMatch(DataValidationStatus::completelyValidated);

        return isValidated;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
