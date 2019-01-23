/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit.deviceAttributes;

import com.elster.jupiter.audit.AbstractAuditDecoder;
import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditLogChangeBuilder;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Operator;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys;

import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.energyict.mdc.device.data.impl.SyncDeviceWithKoreMeter.MULTIPLIER_ONE;

public class AuditTrailDeviceAtributesDecoder extends AbstractAuditDecoder {

    private volatile OrmService ormService;
    private volatile ServerDeviceService serverDeviceService;
    private volatile MeteringService meteringService;

    private Long deviceId;
    private Optional<Device> device;
    private Optional<EndDevice> endDevice;

    private static final String LOCATION_PROPERTY_TYPE = "LOCATION";

    AuditTrailDeviceAtributesDecoder(OrmService ormService, Thesaurus thesaurus, MeteringService meteringService, ServerDeviceService serverDeviceService) {
        this.ormService = ormService;
        this.meteringService = meteringService;
        this.serverDeviceService = serverDeviceService;
        this.setThesaurus(thesaurus);
    }

    @Override
    public String getName() {
        return device
                .map(Device::getName)
                .orElseThrow(() -> new IllegalArgumentException("Device cannot be found"));
    }

    @Override
    public UnexpectedNumberOfUpdatesException.Operation getOperation(UnexpectedNumberOfUpdatesException.Operation operation, AuditDomainContextType context) {
        return UnexpectedNumberOfUpdatesException.Operation.UPDATE;
    }

    @Override
    protected void decodeReference() {
        meteringService.findEndDeviceById(getAuditTrailReference().getPkcolumn())
                .ifPresent(ed -> {
                    endDevice = Optional.of(ed);
                    deviceId = Long.parseLong(ed.getAmrId());
                    device = serverDeviceService.findDeviceById(deviceId);
                });
    }

    @Override
    public List<AuditLogChange> getAuditLogChanges() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();

            auditLogChanges.addAll(getAuditLogChangesFromDevice());
            auditLogChanges.addAll(getAuditLogChangesFromEndDevice());
            return auditLogChanges;

        } catch (Exception e) {
        }
        return Collections.emptyList();
    }

    private List<AuditLogChange> getAuditLogChangesFromDevice() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();
            DataMapper<Device> dataMapper = ormService.getDataModel(DeviceDataServices.COMPONENT_NAME).get().mapper(Device.class);

            getHistoryEntries(dataMapper, getHistoryByJournalClauses())
                    .stream()
                    .forEach(from -> {
                        getToDeviceEntry(from, dataMapper)
                                .ifPresent(to -> {
                                    getAuditLogChangeForBatch(from, to).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForInteger(from.getYearOfCertification(), to.getYearOfCertification(), PropertyTranslationKeys.DEVICE_CERT_YEAR).ifPresent(auditLogChanges::add);
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

            getHistoryEntries(dataMapper, getHistoryByJournalClauses())
                    .stream()
                    .forEach(from -> {
                        getToEndDeviceEntry(from, dataMapper)
                                .ifPresent(to -> {
                                    getAuditLogChangeForString(from.getName(), to.getName(), PropertyTranslationKeys.DEVICE_NAME).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForString(from.getSerialNumber(), to.getSerialNumber(), PropertyTranslationKeys.DEVICE_SERIAL_NUMBER).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForString(from.getManufacturer(), to.getManufacturer(), PropertyTranslationKeys.DEVICE_MANUFACTURER).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForString(from.getModelNumber(), to.getModelNumber(), PropertyTranslationKeys.DEVICE_MODEL_NBR).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForString(from.getModelVersion(), to.getModelVersion(), PropertyTranslationKeys.DEVICE_MODEL_VERSION).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForOptional(from.getLifecycleDates().getInstalledDate(), from.getLifecycleDates()
                                            .getInstalledDate(), PropertyTranslationKeys.TRANSITION_INSTALLATION).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForLocation(from, to, PropertyTranslationKeys.DEVICE_LOCATION).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForCoordinates(from, to, PropertyTranslationKeys.DEVICE_COORDINATES).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForMultiplier(from, to, PropertyTranslationKeys.MULTIPLIER).ifPresent(auditLogChanges::add);

                                });
                    });
            return auditLogChanges
                    .stream()
                    .distinct()
                    .collect(Collectors.toList());

        } catch (Exception e) {
        }
        return Collections.emptyList();
    }

    private Optional<Device> getToDeviceEntry(Device from, DataMapper<Device> dataMapper) {
        if (from.getVersion() + 1 == device.get().getVersion()) {
            return device;
        }
        return getJournalEntry(dataMapper, ImmutableMap.of("ID", from.getId(),
                "VERSIONCOUNT", from.getVersion() + 1));
    }

    private Optional<EndDevice> getToEndDeviceEntry(EndDevice from, DataMapper<EndDevice> dataMapper) {
        if (from.getVersion() + 1 == endDevice.get().getVersion()) {
            return endDevice;
        }
        return getJournalEntry(dataMapper, ImmutableMap.of("ID", from.getId(),
                "VERSIONCOUNT", from.getVersion() + 1));
    }

    private Map<Operator, Pair<String, Object>> getHistoryByJournalClauses() {
        return ImmutableMap.of(Operator.EQUAL, Pair.of("ID", deviceId),
                Operator.GREATERTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeEnd()));
    }

    private Optional<AuditLogChange> getAuditLogChangeForBatch(Device from, Device to) {
        if (to.getBatch() != from.getBatch()) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(PropertyTranslationKeys.BATCH));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            to.getBatch().ifPresent(batch -> auditLogChange.setValue(batch.getName()));
            from.getBatch().ifPresent(batch -> auditLogChange.setValue(batch.getName()));
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    private Optional<AuditLogChange> getAuditLogChangeForLocation(EndDevice from, EndDevice to, TranslationKey translationKey) {
        if (to.getLocation().equals(from.getLocation()) == false) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(translationKey));
            auditLogChange.setType(LOCATION_PROPERTY_TYPE);
            to.getLocation().ifPresent(location -> auditLogChange.setValue(formatLocation(location)));
            from.getLocation().ifPresent(location -> auditLogChange.setPreviousValue(formatLocation(location)));
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    private Optional<AuditLogChange> getAuditLogChangeForCoordinates(EndDevice from, EndDevice to, TranslationKey translationKey) {
        if (to.getSpatialCoordinates().equals(from.getSpatialCoordinates()) == false) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(translationKey));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            to.getSpatialCoordinates().ifPresent(coordinates -> auditLogChange.setValue(coordinates.toString()));
            from.getSpatialCoordinates().ifPresent(coordinates -> auditLogChange.setPreviousValue(coordinates.toString()));
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    private Optional<AuditLogChange> getAuditLogChangeForMultiplier(EndDevice from, EndDevice to, TranslationKey translationKey) {
        if (from != null && Meter.class.isInstance(from)) {
            Meter meter = Meter.class.cast(from);
            MultiplierType multiplierType = serverDeviceService.findDefaultMultiplierType();

            Optional<? extends MeterActivation> toMeterActivation = meter.getMeterActivation(to.getModTime());
            Optional<? extends MeterActivation> fromMeterActivation = meter.getMeterActivation(toMeterActivation.get().getStart().minusMillis(1));

            Optional<BigDecimal> toMultiplier = toMeterActivation.map(activaton ->
                    activaton.getMultiplier(multiplierType).map(Optional::of)
                            .orElseGet(() -> Optional.of(MULTIPLIER_ONE)))
                    .orElseGet(() -> Optional.of(MULTIPLIER_ONE));
            Optional<BigDecimal> fromMultiplier = fromMeterActivation.map(activaton ->
                    activaton.getMultiplier(multiplierType).map(Optional::of)
                            .orElseGet(() -> Optional.of(MULTIPLIER_ONE)))
                    .orElseGet(() -> Optional.of(MULTIPLIER_ONE));

            if (fromMultiplier.equals(toMultiplier) == false) {
                AuditLogChange auditLogChange = new AuditLogChangeBuilder();
                auditLogChange.setName(getDisplayName(translationKey));
                auditLogChange.setType(SimplePropertyType.NUMBER.name());
                toMultiplier.ifPresent(multiplier -> auditLogChange.setValue(multiplier));
                fromMultiplier.ifPresent(multiplier -> auditLogChange.setPreviousValue(multiplier));
                return Optional.of(auditLogChange);
            }
        }
        return Optional.empty();
    }

    private String formatLocation(Location location) {
        List<List<String>> formattedLocationMembers = location.format();
        formattedLocationMembers.stream().skip(1).forEach(list ->
                list.stream().filter(Objects::nonNull).findFirst().ifPresent(member -> list.set(list.indexOf(member), "\\r\\n" + member)));
        return formattedLocationMembers.stream()
                .flatMap(List::stream).filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }
}