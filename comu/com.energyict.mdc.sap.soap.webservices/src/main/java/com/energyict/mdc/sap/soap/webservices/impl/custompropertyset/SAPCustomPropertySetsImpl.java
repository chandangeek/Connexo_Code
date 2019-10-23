/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.custompropertyset;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.HardCodedFieldNames;
import com.elster.jupiter.cps.OverlapCalculatorBuilder;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ValuesRangeConflict;
import com.elster.jupiter.cps.ValuesRangeConflictType;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.RangeSets;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.device.config.ChannelSpec;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.RegisterSpec;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.LoadProfile;
import com.energyict.mdc.common.device.data.Register;
import com.elster.jupiter.metering.DefaultState;
import com.energyict.mdc.common.masterdata.RegisterType;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.obis.ObisCode;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;

@Component(name = "com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.SAPCustomPropertySets",
        service = {SAPCustomPropertySets.class, TranslationKeyProvider.class},
        property = "name=" + SAPCustomPropertySetsImpl.COMPONENT_NAME, immediate = true)
public class SAPCustomPropertySetsImpl implements TranslationKeyProvider, SAPCustomPropertySets {
    static final String COMPONENT_NAME = "SCA"; // only for translations
    private static final TemporalAmount LESS_THAN_TIME_STEP = Duration.ofNanos(1);

    private volatile DeviceService deviceService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile PropertySpecService propertySpecService;
    private volatile OrmService ormService;
    private volatile Thesaurus thesaurus;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile MasterDataService masterDataService;

    private CustomPropertySet<Device, DeviceSAPInfoDomainExtension> deviceInfo;
    private CustomPropertySet<ChannelSpec, DeviceChannelSAPInfoDomainExtension> channelInfo;
    private CustomPropertySet<RegisterSpec, DeviceRegisterSAPInfoDomainExtension> registerInfo;

    public SAPCustomPropertySetsImpl() {
        // for OSGi purposes
    }

    @Inject
    public SAPCustomPropertySetsImpl(DeviceService deviceService, CustomPropertySetService customPropertySetService,
                                     PropertySpecService propertySpecService, OrmService ormService,
                                     NlsService nlsService, DeviceConfigurationService deviceConfigurationService) {
        setDeviceService(deviceService);
        setCustomPropertySetService(customPropertySetService);
        setPropertySpecService(propertySpecService);
        setOrmService(ormService);
        setNlsService(nlsService);
        setDeviceConfigurationService(deviceConfigurationService);
        setMasterDataService(masterDataService);
    }

    @Activate
    public void activate() {
        customPropertySetService.addCustomPropertySet(deviceInfo = new DeviceSAPInfoCustomPropertySet(propertySpecService, thesaurus, this));
        customPropertySetService.addCustomPropertySet(channelInfo = new DeviceChannelSAPInfoCustomPropertySet(propertySpecService, thesaurus));
        customPropertySetService.addCustomPropertySet(registerInfo = new DeviceRegisterSAPInfoCustomPropertySet(propertySpecService, thesaurus));
    }

    @Deactivate
    public void deactivate() {
        customPropertySetService.removeCustomPropertySet(deviceInfo);
        customPropertySetService.removeCustomPropertySet(channelInfo);
        customPropertySetService.removeCustomPropertySet(registerInfo);
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(getComponentName(), getLayer());
    }

    @Reference
    public void setMasterDataService(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @Override
    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

    @Override
    public Optional<String> getSapDeviceId(Device device) {
        return getCPSDataModel(DeviceSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceSAPInfoDomainExtension.class)
                .filter(Where.where(DeviceSAPInfoDomainExtension.FieldNames.DOMAIN.javaName()).isEqualTo(device))
                .findAny()
                .flatMap(DeviceSAPInfoDomainExtension::getDeviceIdentifier);
    }

    @Override
    public Optional<String> getSapDeviceId(String deviceName) {
        return deviceService.findDeviceByName(deviceName)
                .flatMap(this::getSapDeviceId);
    }

    @Override
    public void setSapDeviceId(Device device, String sapDeviceId) {
        lockDeviceTypeOrThrowException(device.getDeviceType());
        Device lockedDevice = lockDeviceOrThrowException(device.getId());

        if (!getSapDeviceId(device).isPresent()) {
            setDeviceCPSProperty(lockedDevice, DeviceSAPInfoDomainExtension.FieldNames.DEVICE_IDENTIFIER.javaName(), sapDeviceId);
        } else {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.DEVICE_ALREADY_HAS_SAP_IDENTIFIER, device.getSerialNumber());
        }
    }

    @Override
    public Optional<Device> getDevice(String sapDeviceId) {
        return getCPSDataModel(DeviceSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceSAPInfoDomainExtension.class)
                .join(Device.class)
                .filter(Where.where(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_IDENTIFIER.javaName()).isEqualTo(sapDeviceId))
                .findAny()
                .map(DeviceSAPInfoDomainExtension::getDevice);
    }

    @Override
    public Map<String, RangeSet<Instant>> getLrn(Channel channel, Range<Instant> range) {
        return channel.getChannelsContainer().getMeter()
                .map(Meter::getId)
                .flatMap(deviceService::findDeviceByMeterId)
                .map(device -> getLrn(device, channel, range))
                .orElseGet(Collections::emptyMap);
    }

    @Override
    public Map<String, RangeSet<Instant>> getProfileId(Channel channel, Range<Instant> range) {
        return channel.getChannelsContainer().getMeter()
                .map(Meter::getId)
                .flatMap(deviceService::findDeviceByMeterId)
                .map(device -> getProfileId(device, channel, range))
                .orElseGet(Collections::emptyMap);
    }

    @Override
    public void setLocation(Device device, String locationId) {
        lockDeviceTypeOrThrowException(device.getDeviceType());
        Device lockedDevice = lockDeviceOrThrowException(device.getId());

        setDeviceCPSProperty(lockedDevice, DeviceSAPInfoDomainExtension.FieldNames.DEVICE_LOCATION.javaName(), locationId);
    }

    @Override
    public void setPod(Device device, String podId) {
        lockDeviceTypeOrThrowException(device.getDeviceType());
        Device lockedDevice = lockDeviceOrThrowException(device.getId());

        setDeviceCPSProperty(lockedDevice, DeviceSAPInfoDomainExtension.FieldNames.POINT_OF_DELIVERY.javaName(), podId);
    }

    @Override
    public Map<String, RangeSet<Instant>> getLrn(com.energyict.mdc.common.device.data.Channel channel, Range<Instant> range) {
        return getLrn(channel.getDevice(), channel.getChannelSpec(), range);
    }

    @Override
    public Map<String, RangeSet<Instant>> getLrn(com.energyict.mdc.common.device.data.Register register, Range<Instant> range) {
        return getLrn(register.getDevice(), register.getRegisterSpec(), range);
    }

    @Override
    public boolean isAnyLrnPresent(long deviceId) {
        return isAnyRegisterLrn(deviceId) || isAnyChannelLrn(deviceId);
    }

    @Override
    public void setLrn(Register register, String lrn, Instant startDateTime, Instant endDateTime) {
        Range<Instant> range = getTimeInterval(startDateTime, endDateTime);

        setLrn(register, lrn, range);
    }

    @Override
    public void setLrn(com.energyict.mdc.common.device.data.Channel channel, String lrn, Instant startDateTime, Instant endDateTime) {
        Range<Instant> range = getTimeInterval(startDateTime, endDateTime);

        setLrn(channel, lrn, range);
    }

    @Override
    public Optional<Channel> getChannel(String lrn, Instant when) {
        return Stream.<Supplier<Optional<Pair<Long, ReadingType>>>>of(
                () -> getChannelIdentification(lrn, when),
                () -> getRegisterIdentification(lrn, when)
        )
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .findFirst()
                .map(Optional::get)
                .flatMap(deviceIdAndReadingType -> getChannel(deviceIdAndReadingType.getFirst(), deviceIdAndReadingType.getLast(), when));
    }

    @Override
    public Optional<ChannelSpec> getChannelSpecForProfileId(ChannelSpec channelSpec, long deviceId, String profileId, Range<Instant> interval) {
        Condition whereOverlapped = getOverlappedCondition(interval);
        Condition notThisChannelSpec = Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.DOMAIN.javaName()).isNotEqual(channelSpec);
        Condition notThisDevice = Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.DEVICE_ID.javaName()).isNotEqual(deviceId);
        Condition thisDevice = notThisDevice.not();
        return (getCPSDataModel(DeviceChannelSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceChannelSAPInfoDomainExtension.class)
                .join(ChannelSpec.class)
                .join(ReadingType.class)
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.PROFILE_ID.javaName()).isEqualTo(profileId)
                        .and(notThisDevice.or(thisDevice.and(notThisChannelSpec))))
                .filter(whereOverlapped).map(s -> s.getChannelSpec()).findAny());
    }

    @Override
    public Set<Pair<Long, ChannelSpec>> getChannelInfosAfterDate(String lrn, String profileId, Instant date) {
        Condition intervalAfterDateCondition = getIntervalAfterDateCondition(date);
        return getCPSDataModel(DeviceChannelSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceChannelSAPInfoDomainExtension.class)
                .join(ChannelSpec.class)
                .join(ReadingType.class)
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.LOGICAL_REGISTER_NUMBER.javaName()).isEqualTo(lrn))
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.PROFILE_ID.javaName()).isEqualTo(profileId))
                .filter(intervalAfterDateCondition)
                .map(e -> Pair.of(e.getDeviceId(), e.getChannelSpec())).collect(Collectors.toSet());
    }

    @Override
    public Map<Pair<Long, ChannelSpec>, List<Pair<Range<Instant>, Range<Instant>>>> getChannelInfos(String lrn, Range<Instant> interval) {
        Condition whereOverlapped = getOverlappedCondition(interval);
        Stream<DeviceChannelSAPInfoDomainExtension> stream = getCPSDataModel(DeviceChannelSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceChannelSAPInfoDomainExtension.class)
                .join(ChannelSpec.class)
                .join(ReadingType.class)
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.LOGICAL_REGISTER_NUMBER.javaName()).isEqualTo(lrn))
                .filter(whereOverlapped);

        Map<Pair<Long, ChannelSpec>, List<Pair<Range<Instant>, Range<Instant>>>> map = new HashMap<>();
        stream.forEach(e -> {
            Range range = e.getRange();
            Optional<Range<Instant>> cutRange = cutRange(range);
            if (cutRange.isPresent()) {
                Optional<Device> device = deviceService.findDeviceById(e.getDeviceId());
                if (device.isPresent()) {
                    if (isDeviceActive(device.get())) {
                        Pair<Long, ChannelSpec> key = Pair.of(e.getDeviceId(), e.getChannelSpec());
                        List<Pair<Range<Instant>, Range<Instant>>> list = map.getOrDefault(key, new ArrayList<>());
                        try {
                            Range<Instant> rangeIntersection = cutRange.get().intersection(interval);
                            if (Duration.between(rangeIntersection.lowerEndpoint(), rangeIntersection.upperEndpoint()).toDays() >= 1) {
                                list.add(Pair.of(rangeIntersection, range));
                            }
                            map.put(key, list);
                        } catch (IllegalArgumentException ex) {
                            // no intersection with interval (should never occur)
                        }
                    } else {
                        throw new SAPWebServiceException(thesaurus, MessageSeeds.DEVICE_IS_NOT_ACTIVE, device.get().getName());
                    }
                }
            }
        });
        return map;
    }

    public void truncateCpsInterval(Device device, String lrn, Instant endDate) {
        Optional<Pair<Object, RegisteredCustomPropertySet>> dataSource = Stream.<Supplier<Optional<Pair<Object, RegisteredCustomPropertySet>>>>of(
                () -> getChannelCps(device.getId(), lrn, endDate),
                () -> getRegisterCps(device.getId(), lrn, endDate)
        )
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .findFirst()
                .map(Optional::get);
        if (dataSource.isPresent()) {
            CustomPropertySetValues versionToUpdate = customPropertySetService.getUniqueValuesFor(dataSource.get().getLast().getCustomPropertySet(), dataSource.get()
                    .getFirst(), endDate, device.getId());
            Range<Instant> oldRange = versionToUpdate.getEffectiveRange();
            if (oldRange.hasLowerBound() && endDate.equals(oldRange.lowerEndpoint())) {
                // end date equals lower bound of existing version
                List<CustomPropertySetValues> allVersions = customPropertySetService.getAllVersionedValuesFor(dataSource.get().getLast().getCustomPropertySet(), dataSource.get()
                        .getFirst(), device.getId());
                List<CustomPropertySetValues> toRecreateBeforeConflict = new ArrayList<>();
                List<CustomPropertySetValues> toRecreateAfterConflict = new ArrayList<>();
                for (CustomPropertySetValues version : allVersions) {
                    if (!version.getEffectiveRange().hasLowerBound() || version.getEffectiveRange().hasLowerBound() && version.getEffectiveRange().lowerEndpoint().isBefore(endDate)) {
                        toRecreateBeforeConflict.add(version);
                    }
                    if (version.getEffectiveRange().hasLowerBound() && version.getEffectiveRange().lowerEndpoint().isAfter(endDate)) {
                        toRecreateAfterConflict.add(version);
                    }
                }
                customPropertySetService.removeValuesFor(dataSource.get().getLast().getCustomPropertySet(), dataSource.get().getFirst(), device.getId());
                for (CustomPropertySetValues version : toRecreateBeforeConflict) {
                    customPropertySetService.setValuesVersionFor(dataSource.get().getLast().getCustomPropertySet(), dataSource.get()
                            .getFirst(), version, version.getEffectiveRange(), device.getId());
                }
                if (!toRecreateAfterConflict.isEmpty()) {
                    customPropertySetService.setValuesVersionFor(dataSource.get().getLast().getCustomPropertySet(), dataSource.get().getFirst(),
                            CustomPropertySetValues.emptyDuring(versionToUpdate.getEffectiveRange()), versionToUpdate.getEffectiveRange(), device.getId());
                    for (CustomPropertySetValues version : toRecreateAfterConflict) {
                        customPropertySetService.setValuesVersionFor(dataSource.get().getLast().getCustomPropertySet(), dataSource.get()
                                .getFirst(), version, version.getEffectiveRange(), device.getId());
                    }
                }
            } else {
                Range<Instant> range = getRangeToUpdate(endDate, oldRange);
                OverlapCalculatorBuilder overlapCalculatorBuilder = customPropertySetService.calculateOverlapsFor(dataSource.get().getLast().getCustomPropertySet(), dataSource.get()
                        .getFirst(), device.getId());
                Optional<ValuesRangeConflict> conflict = overlapCalculatorBuilder.whenUpdating(oldRange.hasLowerBound() ? oldRange.lowerEndpoint() : Instant.EPOCH, range).stream().filter(c -> c.getType().equals(ValuesRangeConflictType.RANGE_GAP_BEFORE) || c.getType().equals(ValuesRangeConflictType.RANGE_GAP_AFTER)).findFirst();
                if (conflict.isPresent()) {
                    List<CustomPropertySetValues> allVersions = customPropertySetService.getAllVersionedValuesFor(dataSource.get().getLast().getCustomPropertySet(), dataSource.get()
                            .getFirst(), device.getId());
                    Optional<Range<Instant>> conflictRange = Optional.ofNullable(conflict.get().getConflictingRange());
                    List<CustomPropertySetValues> toRecreateBeforeConflict = new ArrayList<>();
                    List<CustomPropertySetValues> toRecreateAfterConflict = new ArrayList<>();
                    for (CustomPropertySetValues version : allVersions) {
                        if (!version.equals(versionToUpdate) && versionToUpdate.getEffectiveRange().hasLowerBound() && version.getEffectiveRange().hasUpperBound()
                                && (version.getEffectiveRange().upperEndpoint().isBefore(versionToUpdate.getEffectiveRange().lowerEndpoint()) || version.getEffectiveRange().upperEndpoint().equals(versionToUpdate.getEffectiveRange().lowerEndpoint()))) {
                            toRecreateBeforeConflict.add(version);
                        }
                        if (conflictRange.isPresent() && !version.equals(versionToUpdate) && versionToUpdate.getEffectiveRange().hasUpperBound() && version.getEffectiveRange().hasLowerBound()
                                && (version.getEffectiveRange().lowerEndpoint().isAfter(versionToUpdate.getEffectiveRange().upperEndpoint()) || version.getEffectiveRange().lowerEndpoint().equals(versionToUpdate.getEffectiveRange().upperEndpoint()))) {
                            toRecreateAfterConflict.add(version);
                        }
                    }
                    customPropertySetService.removeValuesFor(dataSource.get().getLast().getCustomPropertySet(), dataSource.get().getFirst(), device.getId());
                    for (CustomPropertySetValues version : toRecreateBeforeConflict) {
                        customPropertySetService.setValuesVersionFor(dataSource.get().getLast().getCustomPropertySet(), dataSource.get().getFirst(), version, version.getEffectiveRange(), device.getId());
                    }
                    // set changed version
                    customPropertySetService.setValuesVersionFor(dataSource.get().getLast().getCustomPropertySet(), dataSource.get().getFirst(), versionToUpdate, range, endDate, device.getId());
                    customPropertySetService.setValuesVersionFor(dataSource.get().getLast().getCustomPropertySet(), dataSource.get().getFirst(),
                            CustomPropertySetValues.emptyDuring(conflictRange.get()), conflictRange.get(), device.getId());
                    for (CustomPropertySetValues version : toRecreateAfterConflict) {
                        customPropertySetService.setValuesVersionFor(dataSource.get().getLast().getCustomPropertySet(), dataSource.get().getFirst(), version, version.getEffectiveRange(), device.getId());
                    }
                } else {
                    // set changed version
                    customPropertySetService.setValuesVersionFor(dataSource.get().getLast().getCustomPropertySet(), dataSource.get().getFirst(), versionToUpdate, range, endDate, device.getId());
                }
            }


        } else {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.DATASOURCE_NOT_FOUND, device.getName(), lrn, endDate);
        }
    }

    private Range<Instant> getRangeToUpdate(Instant endDate, Range<Instant> oldRange) {
        if (oldRange.hasLowerBound()) {
            if (endDate.isAfter(oldRange.lowerEndpoint()) && oldRange.hasUpperBound() && endDate.isBefore(oldRange.upperEndpoint()) || !oldRange.hasUpperBound() ) {
                return Range.closedOpen(oldRange.lowerEndpoint(), endDate);
            } else {
                throw new SAPWebServiceException(thesaurus, MessageSeeds.INVALID_END_DATE, endDate, oldRange);
            }
        } else if (endDate.equals(Instant.EPOCH)) {
            return Range.all();
        } else {
            return Range.lessThan(endDate);
        }
    }

    @Override
    public Optional<Interval> getLastProfileIdDateForChannelOnDevice(long deviceId, String channelMrid) {
        return getCPSDataModel(DeviceChannelSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceChannelSAPInfoDomainExtension.class)
                .join(ChannelSpec.class)
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.PROFILE_ID.javaName()).isNotNull())
                .filter(e -> e.getDeviceId() == deviceId)
                .filter(f -> f.getChannelSpec().getReadingType().getMRID().equals(channelMrid))
                .map(c -> c.getInterval())
                .max(Comparator.comparingLong(m -> {
                    if (m.getEnd() != null) {
                        return m.getEnd().toEpochMilli();
                    } else {
                        return Long.MAX_VALUE;
                    }
                }));
    }

    private boolean isDeviceActive(Device device) {
        return device.getState().getName().equals(DefaultState.ACTIVE.getKey());
    }

    private Condition getOverlappedCondition(Range<Instant> range) {
        return Where.where(HardCodedFieldNames.INTERVAL.javaName()).isEffective(range);
    }

    private Condition getIntervalAfterDateCondition(Instant date) {
        return Where.where(HardCodedFieldNames.INTERVAL.javaName() + ".start").isGreaterThan(date.toEpochMilli());
    }

    private Optional<Range<Instant>> cutRange(Range<Instant> range) {
        Instant start = range.hasLowerBound() ? truncateToDays(range.lowerEndpoint()).equals(range.lowerEndpoint()) ?
                range.lowerEndpoint() : truncateToDays(range.lowerEndpoint()).plus(1, DAYS) : null;

        Instant end = range.hasUpperBound() ? truncateToDays(range.upperEndpoint()) : null;

        if (start != null && end != null) {
            if (start.isBefore(end)) {
                if (end.equals(range.upperEndpoint())) {
                    return Optional.of(Range.closedOpen(start, end));
                } else {
                    return Optional.of(Range.closed(start, end));
                }
            }
            return Optional.empty();
        } else {
            if (start != null) {
                return Optional.of(Range.atLeast(start));
            }

            if (end != null) {
                return Optional.of(Range.atMost(end));
            }
        }
        return Optional.of(range);
    }

    private Instant truncateToDays(Instant dateTime) {
        return ZonedDateTime.ofInstant(dateTime, ZoneId.systemDefault()).truncatedTo(DAYS).toInstant();
    }

    @Override
    public Set<ReadingType> findReadingTypesForProfileId(String profileId) {
        return getCPSDataModel(DeviceChannelSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceChannelSAPInfoDomainExtension.class)
                .join(ChannelSpec.class)
                .join(ReadingType.class)
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.PROFILE_ID.javaName()).isEqualTo(profileId))
                .map(e -> e.getChannelSpec().getReadingType())
                .collect(Collectors.toSet());
    }

    private Optional<Pair<Long, ReadingType>> getChannelIdentification(String lrn, Instant when) {
        return getCPSDataModel(DeviceChannelSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceChannelSAPInfoDomainExtension.class)
                .join(ChannelSpec.class)
                .join(ReadingType.class)
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.LOGICAL_REGISTER_NUMBER.javaName()).isEqualTo(lrn))
                .filter(Where.where(HardCodedFieldNames.INTERVAL.javaName()).isEffective(when))
                .findAny()
                .map(ext -> Pair.of(ext.getDeviceId(), ext.getChannelSpec().getReadingType()));
    }

    private Optional<Pair<Long, ReadingType>> getRegisterIdentification(String lrn, Instant when) {
        return getCPSDataModel(DeviceRegisterSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceRegisterSAPInfoDomainExtension.class)
                .join(RegisterSpec.class)
                .join(ReadingType.class)
                .filter(Where.where(DeviceRegisterSAPInfoDomainExtension.FieldNames.LOGICAL_REGISTER_NUMBER.javaName()).isEqualTo(lrn))
                .filter(Where.where(HardCodedFieldNames.INTERVAL.javaName()).isEffective(when))
                .findAny()
                .map(ext -> Pair.of(ext.getDeviceId(), ext.getRegisterSpec().getReadingType()));
    }

    private Optional<Pair<Object, RegisteredCustomPropertySet>> getChannelCps(long deviceId, String lrn, Instant when) {
        return getCPSDataModel(DeviceChannelSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceChannelSAPInfoDomainExtension.class)
                .join(ChannelSpec.class)
                .join(ReadingType.class)
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.LOGICAL_REGISTER_NUMBER.javaName()).isEqualTo(lrn))
                .filter(Where.where(HardCodedFieldNames.INTERVAL.javaName()).isEffective(when))
                .filter(e -> e.getDeviceId() == deviceId)
                .findAny()
                .map(ext -> Pair.of(ext.getChannelSpec(), ext.getRegisteredCustomPropertySet()));
    }

    private Optional<Pair<Object, RegisteredCustomPropertySet>> getRegisterCps(long deviceId, String lrn, Instant when) {
        return getCPSDataModel(DeviceRegisterSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceRegisterSAPInfoDomainExtension.class)
                .join(RegisterSpec.class)
                .join(ReadingType.class)
                .filter(Where.where(DeviceRegisterSAPInfoDomainExtension.FieldNames.LOGICAL_REGISTER_NUMBER.javaName()).isEqualTo(lrn))
                .filter(Where.where(HardCodedFieldNames.INTERVAL.javaName()).isEffective(when))
                .filter(e -> e.getDeviceId() == deviceId)
                .findAny()
                .map(ext -> Pair.of(ext.getRegisterSpec(), ext.getRegisteredCustomPropertySet()));
    }
    private Optional<Channel> getChannel(long deviceId, ReadingType readingType, Instant when) {
        return deviceService.findDeviceById(deviceId)
                .flatMap(device -> device.getMeterActivation(when))
                .map(MeterActivation::getChannelsContainer)
                .flatMap(cc -> cc.getChannel(readingType));
    }

    private Map<String, RangeSet<Instant>> getLrn(Device device, Channel channel, Range<Instant> range) {
        return anyPoint(range).flatMap(instant -> channel.isRegular() ?
                getChannelSpec(device, channel.getReadingTypes(), instant)
                        .map(spec -> getLrn(device, spec, range)) :
                getRegisterSpec(device, channel.getReadingTypes(), instant)
                        .map(spec -> getLrn(device, spec, range)))
                .orElseGet(Collections::emptyMap);
    }

    private Map<String, RangeSet<Instant>> getProfileId(Device device, Channel channel, Range<Instant> range) {
        Map<String, RangeSet<Instant>> profileIdIntervals = new HashMap<>();
        if (channel.isRegular()) {
            Optional<Instant> any = anyPoint(range);
            if (any.isPresent()) {
                Optional<ChannelSpec> spec = getChannelSpec(device, channel.getReadingTypes(), any.get());
                if (spec.isPresent()) {
                    return getProfileId(device, spec.get(), range);
                }
            }
        }
        return profileIdIntervals;
    }

    private Optional<ChannelSpec> getChannelSpec(Device device, List<? extends ReadingType> readingTypes, Instant when) {
        Device historyDevice = device.getHistory(when).orElse(device);
        return historyDevice.getDeviceConfiguration().getChannelSpecs().stream()
                .filter(spec -> readingTypes.contains(spec.getReadingType())).findAny();
    }

    private Optional<RegisterSpec> getRegisterSpec(Device device, List<? extends ReadingType> readingTypes, Instant when) {
        return device.getHistory(when)
                .map(Device::getDeviceConfiguration)
                .map(DeviceConfiguration::getRegisterSpecs)
                .flatMap(specs -> specs.stream().filter(spec -> readingTypes.contains(spec.getReadingType())).findAny());
    }

    private Map<String, RangeSet<Instant>> getProfileId(Device device, ChannelSpec channelSpec, Range<Instant> range) {
        Stream<DeviceChannelSAPInfoDomainExtension> extensions = getCPSDataModel(DeviceChannelSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceChannelSAPInfoDomainExtension.class)
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.DOMAIN.javaName()).isEqualTo(channelSpec))
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.DEVICE_ID.javaName()).isEqualTo(device.getId()))
                .filter(Where.where(HardCodedFieldNames.INTERVAL.javaName()).isEffective(range));
        Map<String, RangeSet<Instant>> map = new HashMap<>();
        extensions.forEach(ext -> {
            if (ext.getLogicalRegisterNumber().isPresent() && ext.getProfileId().isPresent()) {
                RangeSet<Instant> rangeSet = map.get(ext.getProfileId().get());
                if (rangeSet == null) {
                    rangeSet = TreeRangeSet.create();
                }
                rangeSet.add(ext.getRange().intersection(range));
                map.put(ext.getProfileId().get(), rangeSet);
            }
        });
        return map;
    }

    private Map<String, RangeSet<Instant>> getLrn(Device device, ChannelSpec channelSpec, Range<Instant> range) {
        return getCPSDataModel(DeviceChannelSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceChannelSAPInfoDomainExtension.class)
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.DOMAIN.javaName()).isEqualTo(channelSpec))
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.DEVICE_ID.javaName()).isEqualTo(device.getId()))
                .filter(Where.where(HardCodedFieldNames.INTERVAL.javaName()).isEffective(range))
                .map(ext -> ext.getLogicalRegisterNumber().map(lrn -> Pair.of(lrn, ImmutableRangeSet.of(ext.getRange().intersection(range)))))
                .flatMap(Functions.asStream())
                .collect(Collectors.toMap(Pair::getFirst, Pair::getLast, RangeSets::union));
    }

    private Map<String, RangeSet<Instant>> getLrn(Device device, RegisterSpec registerSpec, Range<Instant> range) {
        return getCPSDataModel(DeviceRegisterSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceRegisterSAPInfoDomainExtension.class)
                .filter(Where.where(DeviceRegisterSAPInfoDomainExtension.FieldNames.DOMAIN.javaName()).isEqualTo(registerSpec))
                .filter(Where.where(DeviceRegisterSAPInfoDomainExtension.FieldNames.DEVICE_ID.javaName()).isEqualTo(device.getId()))
                .filter(Where.where(HardCodedFieldNames.INTERVAL.javaName()).isEffective(range))
                .map(ext -> ext.getLogicalRegisterNumber().map(lrn -> Pair.of(lrn, ImmutableRangeSet.of(ext.getRange().intersection(range)))))
                .flatMap(Functions.asStream())
                .collect(Collectors.toMap(Pair::getFirst, Pair::getLast, RangeSets::union));
    }

    private static Optional<Instant> anyPoint(Range<Instant> range) {
        return range.isEmpty() ? Optional.empty() : Optional.of(
                range.hasLowerBound() ?
                        range.lowerBoundType() == BoundType.CLOSED ? range.lowerEndpoint() : range.lowerEndpoint().plus(LESS_THAN_TIME_STEP) :
                        range.hasUpperBound() ?
                                range.upperBoundType() == BoundType.CLOSED ? range.upperEndpoint() : range.upperEndpoint().minus(LESS_THAN_TIME_STEP) :
                                Instant.EPOCH
        );
    }

    private DataModel getCPSDataModel(String modelName) {
        return ormService.getDataModel(modelName)
                .orElseThrow(() -> new IllegalStateException(DataModel.class.getSimpleName() + ' ' + modelName + " isn't found."));
    }

    private void setLrn(Register register, String lrn, Range<Instant> range) {
        lockRegisterTypeOrThrowException(register.getRegisterSpec().getRegisterType());
        lockRegisterSpecOrThrowException(register.getRegisterSpec());

        addRegisterCustomPropertySetVersioned(register, DeviceRegisterSAPInfoDomainExtension.FieldNames.LOGICAL_REGISTER_NUMBER.javaName(), lrn, range);
    }

    private void setLrn(com.energyict.mdc.common.device.data.Channel channel, String lrn, Range<Instant> range) {
        lockLoadProfileTypeOrThrowException(channel.getLoadProfile());
        lockChannelSpecOrThrowException(channel.getChannelSpec());

        addChannelCustomPropertySetVersioned(channel, DeviceRegisterSAPInfoDomainExtension.FieldNames.LOGICAL_REGISTER_NUMBER.javaName(), lrn, range);
    }

    private boolean isAnyRegisterLrn(long deviceId) {
        return getCPSDataModel(DeviceRegisterSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceRegisterSAPInfoDomainExtension.class)
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.DEVICE_ID.javaName()).isEqualTo(deviceId))
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.LOGICAL_REGISTER_NUMBER.javaName()).isNotNull())
                .findAny()
                .isPresent();
    }


    private boolean isAnyChannelLrn(long deviceId) {
        return getCPSDataModel(DeviceChannelSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceChannelSAPInfoDomainExtension.class)
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.DEVICE_ID.javaName()).isEqualTo(deviceId))
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.LOGICAL_REGISTER_NUMBER.javaName()).isNotNull())
                .findAny()
                .isPresent();
    }

    private Range<Instant> getTimeInterval(Instant startDateTime, Instant endDateTime) {
        Range<Instant> range;
        try {
            range = Ranges.closedOpen(startDateTime, endDateTime);
        } catch (IllegalArgumentException e) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.INTERVAL_INVALID,
                    startDateTime.toString(), endDateTime.toString());
        }
        if (range.isEmpty()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.INTERVAL_INVALID,
                    startDateTime.toString(), endDateTime.toString());
        }
        return range;
    }

    private Device lockDeviceOrThrowException(long deviceId) {
        return deviceService.findAndLockDeviceById(deviceId)
                .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.NO_SUCH_DEVICE, deviceId));
    }

    private void lockDeviceTypeOrThrowException(DeviceType deviceType) {
        deviceConfigurationService
                .findAndLockDeviceType(deviceType.getId())
                .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.NO_DEVICE_TYPE_FOUND, deviceType.getName()));
    }

    private void lockRegisterTypeOrThrowException(RegisterType registerType) {
        masterDataService
                .findAndLockRegisterTypeById(registerType.getId())
                .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.NO_REGISTER_TYPE_FOUND, registerType.getObisCode()));
    }

    private void lockLoadProfileTypeOrThrowException(LoadProfile loadProfile) {
        masterDataService
                .findAndLockLoadProfileTypeById(loadProfile.getLoadProfileTypeId())
                .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.NO_LOAD_PROFILE_TYPE_FOUND, loadProfile.getLoadProfileTypeObisCode()));
    }

    private void lockRegisterSpecOrThrowException(RegisterSpec registerSpec) {
        deviceConfigurationService.findAndLockRegisterSpecById(registerSpec.getId())
                .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.NO_REGISTER_SPEC_FOUND, registerSpec.getObisCode()));
    }

    private void lockChannelSpecOrThrowException(ChannelSpec channelSpec) {
        deviceConfigurationService.findAndLockChannelSpecById(channelSpec.getId())
                .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.NO_CHANNEL_SPEC_FOUND, channelSpec.getObisCode()));
    }

    private void setDeviceCPSProperty(Device device, String property, String value) {
        String cpsId = deviceInfo.getId();
        if (!getRegisteredCustomPropertySet(device, cpsId).isEditableByCurrentUser()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER, cpsId);
        }

        CustomPropertySetValues customPropertySetValues = customPropertySetService.getUniqueValuesFor(deviceInfo, device);
        customPropertySetValues.setProperty(property, value);
        customPropertySetService.setValuesFor(deviceInfo, device, customPropertySetValues);
        device.touchDevice();
    }

    private void addRegisterCustomPropertySetVersioned(Register register, String property, String value, Range<Instant> range) {
        String cpsId = registerInfo.getId();
        if (!getRegisteredCustomPropertySet(register, cpsId).isEditableByCurrentUser()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER, cpsId);
        }

        if (!setValuesVersionFor(registerInfo,
                register.getRegisterSpec(), register.getDevice().getId(), register.getObisCode(), property, value, range)) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.REGISTER_ALREADY_HAS_LRN,
                    register.getObisCode(), range.toString());
        }

        register.getRegisterSpec().save();
    }

    private void addChannelCustomPropertySetVersioned(com.energyict.mdc.common.device.data.Channel channel, String property, String value, Range<Instant> range) {
        String cpsId = channelInfo.getId();
        if (!getRegisteredCustomPropertySet(channel, cpsId).isEditableByCurrentUser()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER, cpsId);
        }

        if (!setValuesVersionFor(channelInfo,
                channel.getChannelSpec(), channel.getDevice().getId(), channel.getObisCode(), property, value, range)) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.CHANNEL_ALREADY_HAS_LRN,
                    channel.getObisCode(), range.toString());
        }

        channel.getChannelSpec().save();
    }

    private <D, T extends PersistentDomainExtension<D>> boolean setValuesVersionFor(CustomPropertySet<D, T> customPropertySet, D businesObject,
                                                                                    long deviceId, ObisCode obis,
                                                                                    String property, String value, Range<Instant> range) {
        CustomPropertySetValues customPropertySetValues;

        if (!range.hasLowerBound()) {
            customPropertySetValues = CustomPropertySetValues.empty();
        } else {
            customPropertySetValues = CustomPropertySetValues.emptyDuring(range);
        }

        customPropertySetValues.setProperty(property, value);

        CustomPropertySetValues savedCustomPropertySetValues = CustomPropertySetValues.empty();


        OverlapCalculatorBuilder overlapCalculatorBuilder = customPropertySetService
                .calculateOverlapsFor(customPropertySet,
                        businesObject, deviceId);

        for (ValuesRangeConflict conflict : overlapCalculatorBuilder.whenCreating(range)) {
            if (conflict.getType().equals(ValuesRangeConflictType.RANGE_OVERLAP_DELETE)) {
                return false;
            } else if (conflict.getType().equals(ValuesRangeConflictType.RANGE_GAP_AFTER)) {
                customPropertySetService.setValuesVersionFor(customPropertySet,
                        businesObject, CustomPropertySetValues.empty(), conflict.getConflictingRange(), deviceId);
            } else if (conflict.getType().equals(ValuesRangeConflictType.RANGE_GAP_BEFORE)) {
                customPropertySetService.setValuesVersionFor(customPropertySet,
                        businesObject, CustomPropertySetValues.empty(), conflict.getConflictingRange(), deviceId);
            } else if (conflict.getType().equals(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_START)) {
                if (conflict.getValues().getEffectiveRange().hasLowerBound()) {
                    return false;
                }
            } else if (conflict.getType().equals(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_END)) {
                if (conflict.getValues().getEffectiveRange().hasLowerBound()) {
                    if (conflict.getValues().getEffectiveRange().hasUpperBound() &&
                            (!conflict.getValues().getEffectiveRange().intersection(conflict.getConflictingRange()).isEmpty())) {
                        return false;
                    }
                } else {
                    Instant endTime;
                    if (conflict.getValues().getEffectiveRange().hasUpperBound()) {
                        endTime = conflict.getValues().getEffectiveRange().upperEndpoint();
                    } else {
                        endTime = null;
                    }
                    Instant startTime;
                    if (conflict.getConflictingRange().hasUpperBound()) {
                        startTime = conflict.getConflictingRange().upperEndpoint();
                    } else {
                        //throw new SAPWebServiceException(thesaurus,MessageSeeds.REGISTER_ALREADY_HAS_LRN,
                        //        register.getObisCode(), range.toString());
                        continue;
                    }

                    savedCustomPropertySetValues = CustomPropertySetValues.emptyDuring(getTimeInterval(startTime, endTime));
                    savedCustomPropertySetValues.setProperty(property, conflict.getValues().getProperty(property));
                }
            }
        }

        customPropertySetService.setValuesVersionFor(customPropertySet,
                businesObject, customPropertySetValues, range, deviceId);

        if (!savedCustomPropertySetValues.isEmpty()) {
            customPropertySetService.setValuesVersionFor(customPropertySet,
                    businesObject, savedCustomPropertySetValues, savedCustomPropertySetValues.getEffectiveRange(), deviceId);
        }
        return true;
    }

    private RegisteredCustomPropertySet getRegisteredCustomPropertySet(Device device, String cpsId) {
        return device.getDeviceType().getCustomPropertySets().stream()
                .filter(cps -> cps.getCustomPropertySetId().equals(cpsId) && cps.isViewableByCurrentUser())
                .findFirst()
                .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.COULD_NOT_FIND_ACTIVE_CPS, cpsId));
    }

    private RegisteredCustomPropertySet getRegisteredCustomPropertySet(Register register, String cpsId) {
        return register.getDevice().getDeviceType().getRegisterTypeTypeCustomPropertySet(register.getRegisterSpec().getRegisterType())
                .filter(f -> f.getCustomPropertySetId().equals(cpsId) && f.isViewableByCurrentUser())
                .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.COULD_NOT_FIND_ACTIVE_CPS, cpsId));
    }

    private RegisteredCustomPropertySet getRegisteredCustomPropertySet(com.energyict.mdc.common.device.data.Channel channel, String cpsId) {
        return channel.getDevice().getDeviceType().getLoadProfileTypeCustomPropertySet(channel.getChannelSpec().getLoadProfileSpec().getLoadProfileType())
                .filter(f -> f.getCustomPropertySetId().equals(cpsId) && f.isViewableByCurrentUser())
                .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.COULD_NOT_FIND_ACTIVE_CPS, cpsId));
    }
}
