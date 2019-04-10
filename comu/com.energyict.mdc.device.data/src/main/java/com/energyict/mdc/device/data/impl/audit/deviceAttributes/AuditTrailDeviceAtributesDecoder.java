/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit.deviceAttributes;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditLogChangeBuilder;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.audit.AbstractDeviceAuditDecoder;
import com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.energyict.mdc.device.data.impl.SyncDeviceWithKoreMeter.MULTIPLIER_ONE;

public class AuditTrailDeviceAtributesDecoder extends AbstractDeviceAuditDecoder {

    private static final String LOCATION_PROPERTY_TYPE = "LOCATION";
    private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    AuditTrailDeviceAtributesDecoder(OrmService ormService, Thesaurus thesaurus, MeteringService meteringService, ServerDeviceService serverDeviceService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.ormService = ormService;
        this.meteringService = meteringService;
        this.serverDeviceService = serverDeviceService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.setThesaurus(thesaurus);
    }

    @Override
    public UnexpectedNumberOfUpdatesException.Operation getOperation(UnexpectedNumberOfUpdatesException.Operation operation, AuditDomainContextType context) {
        return isDomainObsolete() ? UnexpectedNumberOfUpdatesException.Operation.DELETE : operation;
    }

    @Override
    public List<AuditLogChange> getAuditLogChanges() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();

            if (isDomainObsolete()) {
                return auditLogChanges;
            }
            if (getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.UPDATE) {
                auditLogChanges.addAll(getAuditLogChangesFromDevice());
                auditLogChanges.addAll(getAuditLogChangesFromEndDevice());
            } else if (getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.INSERT) {
                auditLogChanges.addAll(getAuditLogChangesForNewDevice());
                auditLogChanges.addAll(getAuditLogChangesForNewEndDevice());
            }
            return auditLogChanges
                    .stream()
                    .distinct()
                    .collect(Collectors.toList());

        } catch (Exception e) {
        }
        return Collections.emptyList();
    }

    private List<AuditLogChange> getAuditLogChangesFromDevice() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();
            DataMapper<Device> dataMapper = ormService.getDataModel(DeviceDataServices.COMPONENT_NAME).get().mapper(Device.class);

            List<Device> historyEntries = getHistoryEntries(dataMapper, getHistoryByJournalClauses(Long.parseLong(endDevice.get().getAmrId())));
            historyEntries
                    .forEach(from -> {
                        historyEntries.stream()
                                .filter(ed -> ed.getVersion() == from.getVersion() + 1)
                                .findFirst()
                                .map(Optional::of)
                                .orElseGet(() -> getToDeviceEntry(from, from.getVersion() + 1, dataMapper))
                                .ifPresent(to -> {
                                    getAuditLogChangeForBatch(from, to).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForInteger(from.getYearOfCertification(), to.getYearOfCertification(), PropertyTranslationKeys.DEVICE_CERT_YEAR).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForString(from.getDeviceConfiguration().getName(), to.getDeviceConfiguration()
                                            .getName(), PropertyTranslationKeys.DEVICE_CONFIGURATION).ifPresent(auditLogChanges::add);

                                });
                    });
            return auditLogChanges;

        } catch (Exception e) {
        }
        return Collections.emptyList();
    }

    private List<AuditLogChange> getAuditLogChangesFromEndDevice() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();
            DataMapper<EndDevice> dataMapper = ormService.getDataModel(MeteringService.COMPONENTNAME).get().mapper(EndDevice.class);

            List<EndDevice> historyEntries = getHistoryEntries(dataMapper, getHistoryByJournalClauses(endDevice.get().getId()));
            historyEntries.addAll(getHistoryEntries(dataMapper, getHistoryByModTimeClauses(endDevice.get().getId())));
            historyEntries
                    .forEach(from -> {
                        historyEntries.stream()
                                .filter(ed -> ed.getVersion() == from.getVersion() + 1)
                                .findFirst()
                                .map(Optional::of)
                                .orElseGet(() -> getToEndDeviceEntry(from, from.getVersion() + 1, dataMapper))
                                .ifPresent(to -> {
                                    getAuditLogChangeForString(from.getName(), to.getName(), PropertyTranslationKeys.DEVICE_NAME).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForString(from.getSerialNumber(), to.getSerialNumber(), PropertyTranslationKeys.DEVICE_SERIAL_NUMBER).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForString(from.getManufacturer(), to.getManufacturer(), PropertyTranslationKeys.DEVICE_MANUFACTURER).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForString(from.getModelNumber(), to.getModelNumber(), PropertyTranslationKeys.DEVICE_MODEL_NBR).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForString(from.getModelVersion(), to.getModelVersion(), PropertyTranslationKeys.DEVICE_MODEL_VERSION).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForOptional(from.getLifecycleDates().getRemovedDate(), to.getLifecycleDates()
                                            .getRemovedDate(), PropertyTranslationKeys.TRANSITION_DEACTIVATION, SimplePropertyType.TIMESTAMP).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForOptional(from.getLifecycleDates().getRetiredDate(), to.getLifecycleDates()
                                            .getRetiredDate(), PropertyTranslationKeys.TRANSITION_DECOMMISSIONING, SimplePropertyType.TIMESTAMP).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForOptional(from.getLifecycleDates().getReceivedDate(), to.getLifecycleDates()
                                            .getReceivedDate(), PropertyTranslationKeys.TRANSITION_SHIPMENT, SimplePropertyType.TIMESTAMP).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForOptional(from.getLifecycleDates().getInstalledDate(), to.getLifecycleDates()
                                            .getInstalledDate(), PropertyTranslationKeys.TRANSITION_INSTALLATION, SimplePropertyType.TIMESTAMP).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForLocation(from, to).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForCoordinates(from, to).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForMultiplier(from, to).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForState().ifPresent(auditLogChanges::add);
                                });
                    });
            return auditLogChanges;

        } catch (Exception e) {
        }
        return Collections.emptyList();
    }

    private List<AuditLogChange> getAuditLogChangesForNewDevice() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();
            DataMapper<Device> dataMapper = ormService.getDataModel(DeviceDataServices.COMPONENT_NAME).get().mapper(Device.class);

            Optional<Device> auditDevice = device
                    .filter(d -> d.getModificationDate().isAfter(getAuditTrailReference().getModTimeStart()) &&
                            d.getModificationDate().isBefore(getAuditTrailReference().getModTimeEnd()))
                    .map(Optional::of)
                    .orElseGet(() -> getHistoryEntries(dataMapper, getHistoryByModTimeClauses(Long.parseLong(endDevice.get().getAmrId())))
                            .stream().max(Comparator.comparing(Device::getVersion)));
            auditDevice
                    .ifPresent(from -> {
                        getAuditLogChangeForBatch(from).ifPresent(auditLogChanges::add);
                        getAuditLogChangeForInteger(from.getYearOfCertification(), PropertyTranslationKeys.DEVICE_CERT_YEAR).ifPresent(auditLogChanges::add);
                        getAuditLogChangeForString(from.getDeviceType().getName(), PropertyTranslationKeys.DEVICE_TYPE).ifPresent(auditLogChanges::add);
                        getAuditLogChangeForString(from.getDeviceConfiguration().getName(), PropertyTranslationKeys.DEVICE_CONFIGURATION).ifPresent(auditLogChanges::add);
                    });
            return auditLogChanges;

        } catch (Exception e) {
        }
        return Collections.emptyList();
    }

    private List<AuditLogChange> getAuditLogChangesForNewEndDevice() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();
            DataMapper<EndDevice> dataMapper = ormService.getDataModel(MeteringService.COMPONENTNAME).get().mapper(EndDevice.class);

            Optional<EndDevice> auditDevice = endDevice
                    .filter(d -> isBetweenPeriodMod(d.getModTime()))
                    .map(Optional::of)
                    .orElseGet(() -> getHistoryEntries(dataMapper, getHistoryByModTimeClauses(endDevice.get().getId()))
                            .stream().max(Comparator.comparing(EndDevice::getVersion)));
            auditDevice
                    .ifPresent(from -> {
                        getAuditLogChangeForString(from.getName(), PropertyTranslationKeys.DEVICE_NAME).ifPresent(auditLogChanges::add);
                        getAuditLogChangeForString(from.getSerialNumber(), PropertyTranslationKeys.DEVICE_SERIAL_NUMBER).ifPresent(auditLogChanges::add);
                        getAuditLogChangeForString(from.getManufacturer(), PropertyTranslationKeys.DEVICE_MANUFACTURER).ifPresent(auditLogChanges::add);
                        getAuditLogChangeForString(from.getModelNumber(), PropertyTranslationKeys.DEVICE_MODEL_NBR).ifPresent(auditLogChanges::add);
                        getAuditLogChangeForString(from.getModelVersion(), PropertyTranslationKeys.DEVICE_MODEL_VERSION).ifPresent(auditLogChanges::add);
                        getAuditLogChangeForOptional(from.getLifecycleDates().getReceivedDate(), PropertyTranslationKeys.TRANSITION_SHIPMENT, SimplePropertyType.TIMESTAMP).ifPresent(auditLogChanges::add);
                    });
            return auditLogChanges;

        } catch (Exception e) {
        }
        return Collections.emptyList();
    }

    private Optional<AuditLogChange> getAuditLogChangeForBatch(Device from, Device to) {
        if ((to.getBatch().isPresent() != from.getBatch().isPresent()) ||
                (to.getBatch().isPresent() && from.getBatch().isPresent() &&
                        to.getBatch().get().getName().compareToIgnoreCase(from.getBatch().get().getName()) != 0)) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(PropertyTranslationKeys.BATCH));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            to.getBatch().ifPresent(batch -> auditLogChange.setValue(batch.getName()));
            from.getBatch().ifPresent(batch -> auditLogChange.setPreviousValue(batch.getName()));
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    private Optional<AuditLogChange> getAuditLogChangeForLocation(EndDevice from, EndDevice to) {
        if (!to.getLocation().equals(from.getLocation())) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(PropertyTranslationKeys.DEVICE_LOCATION));
            auditLogChange.setType(LOCATION_PROPERTY_TYPE);
            to.getLocation().ifPresent(location -> auditLogChange.setValue(formatLocation(location)));
            from.getLocation().ifPresent(location -> auditLogChange.setPreviousValue(formatLocation(location)));
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    private Optional<AuditLogChange> getAuditLogChangeForCoordinates(EndDevice from, EndDevice to) {
        if (!to.getSpatialCoordinates().equals(from.getSpatialCoordinates())) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(PropertyTranslationKeys.DEVICE_COORDINATES));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            to.getSpatialCoordinates().ifPresent(coordinates -> auditLogChange.setValue(coordinates.toString()));
            from.getSpatialCoordinates().ifPresent(coordinates -> auditLogChange.setPreviousValue(coordinates.toString()));
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    private Optional<AuditLogChange> getAuditLogChangeForMultiplier(EndDevice from, EndDevice to) {
        try {
            if (Meter.class.isInstance(from)) {
                Map<Instant, BigDecimal> timeSlices = new HashMap<>();

                Meter meter = Meter.class.cast(from);
                MultiplierType multiplierType = serverDeviceService.findDefaultMultiplierType();
                List<? extends MeterActivation> meterActivations = meter.getMeterActivations();

                meterActivations
                        .forEach(meterActivation -> {
                            Map<Instant, BigDecimal> toJournalMultipliers = meterActivation.getJournalMultipliers();
                            if (timeSlices.size() == 0) {
                                timeSlices.put(meterActivation.getModificationDate(), MULTIPLIER_ONE);
                            }
                            if (toJournalMultipliers.size() > 0) {
                                toJournalMultipliers.forEach(timeSlices::put);
                            }
                            if ((toJournalMultipliers.size() == 0) || (meterActivation.getEnd() == null)) {
                                timeSlices.put(Instant.MAX, meterActivation.getMultiplier(multiplierType)
                                        .orElseGet(() -> MULTIPLIER_ONE));
                            }
                        });

                Optional<BigDecimal> fromMultiplier = timeSlices.entrySet().stream()
                        .filter(timeSlice -> timeSlice.getKey().isAfter(getAuditTrailReference().getModTimeStart()) || timeSlice.getKey().equals(getAuditTrailReference().getModTimeStart()))
                        .sorted(Comparator.comparing(Map.Entry::getKey))
                        .map(Map.Entry::getValue)
                        .findFirst();

                Optional<BigDecimal> toMultiplier = timeSlices.entrySet().stream()
                        .filter(timeSlice -> timeSlice.getKey().isAfter(getAuditTrailReference().getModTimeEnd()))
                        .sorted(Comparator.comparing(Map.Entry::getKey))
                        .map(Map.Entry::getValue)
                        .findFirst();

                if (!fromMultiplier.equals(toMultiplier)) {
                    AuditLogChange auditLogChange = new AuditLogChangeBuilder();
                    auditLogChange.setName(getDisplayName(PropertyTranslationKeys.MULTIPLIER));
                    auditLogChange.setType(SimplePropertyType.NUMBER.name());
                    toMultiplier.ifPresent(auditLogChange::setValue);
                    fromMultiplier.ifPresent(auditLogChange::setPreviousValue);
                    return Optional.of(auditLogChange);
                }
            }
        } catch (Exception e) {
        }
        return Optional.empty();
    }

    private Optional<AuditLogChange> getAuditLogChangeForBatch(Device to) {
        return to.getBatch().map(batch -> {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(PropertyTranslationKeys.BATCH));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            auditLogChange.setValue(batch.getName());
            return auditLogChange;
        });
    }

    private Optional<AuditLogChange> getAuditLogChangeForState() {
        return new DeviceStateDecoder(this).getAuditLog();
    }

    private String getStateName(State state) {
        return DefaultState
                .from(state)
                .map(deviceLifeCycleConfigurationService::getDisplayName)
                .orElseGet(state::getName);
    }

    private String formatLocation(Location location) {
        List<List<String>> formattedLocationMembers = location.format();
        formattedLocationMembers.stream().skip(1).forEach(list ->
                list.stream().filter(Objects::nonNull).findFirst().ifPresent(member -> list.set(list.indexOf(member), "\\r\\n" + member)));
        return formattedLocationMembers.stream()
                .flatMap(List::stream).filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

    public OrmService getOrmService(){
        return ormService;
    }

    public EndDevice getEndDevice(){
        return endDevice.get();
    }

    public Device getDevice(){
        return device.get();
    }

    public DeviceLifeCycleConfigurationService getDeviceLifeCycleConfigurationService(){
        return deviceLifeCycleConfigurationService;
    }
}