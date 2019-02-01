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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

    //private Long deviceId;
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
                    device = serverDeviceService.findDeviceById(Long.parseLong(ed.getAmrId()))
                            .map(Optional::of)
                            .orElseGet(() -> {
                                isRemoved = true;
                                return getDeviceFromHistory(Long.parseLong(ed.getAmrId()));
                            });
                });
    }

    @Override
    public List<AuditLogChange> getAuditLogChanges() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();

            auditLogChanges.addAll(getAuditLogChangesFromDevice());
            auditLogChanges.addAll(getAuditLogChangesFromEndDevice());
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
                    .stream()
                    .forEach(from -> {
                        historyEntries.stream()
                                .filter(ed -> ed.getVersion() == from.getVersion() + 1)
                                .findFirst()
                                .map(Optional::of)
                                .orElseGet(() -> getToDeviceEntry(from, from.getVersion() + 1, dataMapper))
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

            List<EndDevice> historyEntries = getHistoryEntries(dataMapper, getHistoryByJournalClauses(Long.parseLong(endDevice.get().getAmrId())));
            historyEntries
                    .stream()
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
                                    getAuditLogChangeForLocation(from, to, PropertyTranslationKeys.DEVICE_LOCATION).ifPresent(auditLogChanges::add);
                                    getAuditLogChangeForCoordinates(from, to, PropertyTranslationKeys.DEVICE_COORDINATES).ifPresent(auditLogChanges::add);
                                    //getAuditLogChangeForMultiplier(from, to, PropertyTranslationKeys.MULTIPLIER).ifPresent(auditLogChanges::add);

                                });
                    });
            return auditLogChanges;

        } catch (Exception e) {
        }
        return Collections.emptyList();
    }

    private Optional<Device> getToDeviceEntry(Device from, long version, DataMapper<Device> dataMapper) {
        if (version >= device.get().getVersion()) {
            return device;
        }
        return getJournalEntry(dataMapper, ImmutableMap.of("ID", from.getId(),
                "VERSIONCOUNT", version))
                .map(Optional::of)
                .orElseGet(() -> getToDeviceEntry(from, version + 1, dataMapper));
    }

    private Optional<EndDevice> getToEndDeviceEntry(EndDevice from, long version, DataMapper<EndDevice> dataMapper) {
        if (version >= endDevice.get().getVersion()) {
            return endDevice;
        }
        return getJournalEntry(dataMapper, ImmutableMap.of("ID", from.getId(),
                "VERSIONCOUNT", version))
                .map(Optional::of)
                .orElseGet(() -> getToEndDeviceEntry(from, version + 1, dataMapper));
    }

    private Map<Operator, Pair<String, Object>> getHistoryByJournalClauses(Long deviceId) {
        return ImmutableMap.of(Operator.EQUAL, Pair.of("ID", deviceId),
                Operator.GREATERTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeEnd()));
    }

    private Map<Operator, Pair<String, Object>> getHistoryByModTimeClauses(Long deviceId) {
        return ImmutableMap.of(Operator.EQUAL, Pair.of("ID", deviceId),
                Operator.GREATERTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeEnd()));
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
        try {
            if (from != null && Meter.class.isInstance(from)) {
                Meter meter = Meter.class.cast(from);
                MultiplierType multiplierType = serverDeviceService.findDefaultMultiplierType();
                List<? extends MeterActivation> meterActivations = meter.getMeterActivations();

                Optional<BigDecimal> toMultiplier = Optional.empty();
                Optional<BigDecimal> fromMultiplier = Optional.empty();

                Map<Instant, BigDecimal> timeSlices = new HashMap<>();


                meterActivations.stream()
                        .forEach(meterActivation -> {
                            Map<Instant, BigDecimal> toJournalMultipliers = meterActivation.getJournalMultipliers();
                            if (timeSlices.size() == 0) {
                                timeSlices.put(((MeterActivation) meterActivation).getCreateDate(), MULTIPLIER_ONE);
                            }
                            if (toJournalMultipliers.size() > 0) {
                                toJournalMultipliers.entrySet().stream()
                                        .forEach(history -> {
                                            timeSlices.put(history.getKey(), history.getValue());
                                        });
                            }
                            if ((toJournalMultipliers.size() == 0) || (((MeterActivation) meterActivation).getEnd() == null)) {
                                timeSlices.put(((MeterActivation) meterActivation).getModificationDate(), ((MeterActivation) meterActivation).getMultiplier(multiplierType)
                                        .orElseGet(() -> MULTIPLIER_ONE));
                            }
                        });

                fromMultiplier = timeSlices.entrySet().stream()
                        .filter(timeSlice -> timeSlice.getKey().isAfter(getAuditTrailReference().getModTimeStart()))
                        .sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey()))
                        .map(Map.Entry::getValue)
                        .findFirst();
                toMultiplier = timeSlices.entrySet().stream()
                        .filter(timeSlice -> timeSlice.getKey().isAfter(getAuditTrailReference().getModTimeStart()))
                        .sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey()))
                        .skip(1)
                        .map(Map.Entry::getValue)
                        .findFirst();
               /*
                Optional<? extends MeterActivation> toMeterActivation = meterActivations.stream()
                        .filter(activation -> ((MeterActivation) activation).getStart().compareTo(getAuditTrailReference().getModTimeStart().truncatedTo(ChronoUnit.MINUTES))==0)
                        .findFirst();

                if (toMeterActivation.isPresent()==false){
                    return Optional.empty();
                }

                Map<Instant, BigDecimal> toJournalMultipliers = toMeterActivation.get().getJournalMultipliers();
                if (toJournalMultipliers.size()>0){
                    Set<Map.Entry<Instant, BigDecimal>> f1 = toJournalMultipliers.entrySet().stream()
                            .filter(multiplierValueJournalEntry -> multiplierValueJournalEntry.getKey().isAfter(getAuditTrailReference().getModTimeStart()) &&
                                    multiplierValueJournalEntry.getKey().isBefore(getAuditTrailReference().getModTimeEnd()))
                            .collect(Collectors.toSet());
                    Set<Map.Entry<Instant, BigDecimal>> f2 = f1.stream().sorted(Comparator.comparing(Map.Entry::getKey)).collect(Collectors.toSet());

                    fromMultiplier = f2.stream().map(journalEntry -> journalEntry.getValue()).findFirst();
                    if (fromMultiplier.isPresent() == false)
                    {
                        Optional<? extends MeterActivation> fromMeterActivation = meterActivations.stream()
                                .filter(activation -> ((MeterActivation) activation).getEnd().compareTo(getAuditTrailReference().getModTimeStart().truncatedTo(ChronoUnit.MINUTES))==0)
                                .findFirst();

                        fromMultiplier = fromMeterActivation.get().getJournalMultipliers().entrySet().stream()
                                .filter(multiplierValueJournalEntry -> multiplierValueJournalEntry.getKey().isAfter(getAuditTrailReference().getModTimeStart()))
                                .sorted(Comparator.comparing(Map.Entry::getKey))
                                .findFirst()
                                .map(journalEntry -> journalEntry.getValue())
                                .map(Optional::of)
                                .orElseGet(() -> fromMeterActivation.get().getMultiplier(multiplierType))
                                .map(Optional::of)
                                .orElseGet(() -> Optional.of(MULTIPLIER_ONE));
                    }

                    Set<Map.Entry<Instant, BigDecimal>> t1 = toJournalMultipliers.entrySet().stream()
                            .filter(multiplierValueJournalEntry -> multiplierValueJournalEntry.getKey().isAfter(getAuditTrailReference().getModTimeEnd()))
                            .collect(Collectors.toSet());
                    Set<Map.Entry<Instant, BigDecimal>> t2 = t1.stream().sorted(Comparator.comparing(Map.Entry::getKey)).collect(Collectors.toSet());

                    toMultiplier = toJournalMultipliers.entrySet().stream()
                            .filter(multiplierValueJournalEntry -> multiplierValueJournalEntry.getKey().isAfter(getAuditTrailReference().getModTimeEnd()))
                            .sorted(Comparator.comparing(Map.Entry::getKey))
                            .findFirst()
                            .map(journalEntry -> journalEntry.getValue())
                            .map(Optional::of)
                            .orElseGet(() -> toMeterActivation.get().getMultiplier(multiplierType))
                            .map(Optional::of)
                            .orElseGet(() -> Optional.of(MULTIPLIER_ONE));
                }
                else {
                    toMultiplier = toMeterActivation.get().getMultiplier(multiplierType)
                            .map(Optional::of)
                            .orElseGet(() -> Optional.of(MULTIPLIER_ONE));

                    Optional<? extends MeterActivation> fromMeterActivation = meterActivations.stream()
                            .filter(activation -> ((MeterActivation) activation).getEnd().compareTo(getAuditTrailReference().getModTimeStart().truncatedTo(ChronoUnit.MINUTES))==0)
                            .findFirst();

                    fromMultiplier = fromMeterActivation.get().getJournalMultipliers().entrySet().stream()
                            .filter(multiplierValueJournalEntry -> multiplierValueJournalEntry.getKey().isAfter(getAuditTrailReference().getModTimeStart()))
                            .sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey()))
                            .findFirst()
                            .map(journalEntry -> journalEntry.getValue())
                            .map(Optional::of)
                            .orElseGet(() -> fromMeterActivation.get().getMultiplier(multiplierType))
                            .map(Optional::of)
                            .orElseGet(() -> Optional.of(MULTIPLIER_ONE));
                }*/

                if (fromMultiplier.equals(toMultiplier) == false) {
                    AuditLogChange auditLogChange = new AuditLogChangeBuilder();
                    auditLogChange.setName(getDisplayName(translationKey));
                    auditLogChange.setType(SimplePropertyType.NUMBER.name());
                    toMultiplier.ifPresent(multiplier -> auditLogChange.setValue(multiplier));
                    fromMultiplier.ifPresent(multiplier -> auditLogChange.setPreviousValue(multiplier));
                    return Optional.of(auditLogChange);
                }

            }
        } catch (Exception e) {
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

    private Optional<Device> getDeviceFromHistory(long id) {
        DataMapper<Device> dataMapper = ormService.getDataModel(DeviceDataServices.COMPONENT_NAME).get().mapper(Device.class);
        Map<Operator, Pair<String, Object>> historyClause = ImmutableMap.of(Operator.EQUAL, Pair.of("ID", id),
                Operator.GREATERTHANOREQUAL, Pair.of("journaltime", getAuditTrailReference().getModTimeStart()));

        return getHistoryEntries(dataMapper, historyClause)
                .stream()
                .findFirst();
    }
}