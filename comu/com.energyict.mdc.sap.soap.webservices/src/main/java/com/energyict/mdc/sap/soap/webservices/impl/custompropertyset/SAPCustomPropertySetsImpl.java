/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.custompropertyset;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.HardCodedFieldNames;
import com.elster.jupiter.cps.OverlapCalculatorBuilder;
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
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;

import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        customPropertySetService.addCustomPropertySet(deviceInfo = new DeviceSAPInfoCustomPropertySet(propertySpecService, thesaurus));
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
    public void addSapDeviceId(Device device, String sapDeviceId) {
        Device lockedDevice = lockDeviceOrThrowException(device.getId());
        lockDeviceTypeOrThrowException(device.getDeviceType().getId());

        if (!getSapDeviceId(device).isPresent()) {
            setDeviceCPSProperty(lockedDevice, deviceInfo.getId(), DeviceSAPInfoDomainExtension.FieldNames.DEVICE_IDENTIFIER.javaName(), sapDeviceId);
        } else {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.DEVICE_ALREADY_HAS_SAP_IDENTIFIER, device.getSerialNumber() );
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
    public void setLocation(Device device, String locationId){
        Device lockedDevice = lockDeviceOrThrowException(device.getId());
        lockDeviceTypeOrThrowException(device.getDeviceType().getId());

        setDeviceCPSProperty(lockedDevice, deviceInfo.getId(), DeviceSAPInfoDomainExtension.FieldNames.DEVICE_LOCATION.javaName(), locationId);
    }

    @Override
    public void setPod(Device device, String podId){
        Device lockedDevice = lockDeviceOrThrowException(device.getId());
        lockDeviceTypeOrThrowException(device.getDeviceType().getId());

        setDeviceCPSProperty(lockedDevice, deviceInfo.getId(), DeviceSAPInfoDomainExtension.FieldNames.POINT_OF_DELIVERY.javaName(), podId);
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
    public Map<String, RangeSet<Instant>> getLrn(com.energyict.mdc.device.data.Channel channel, Range<Instant> range) {
        return getLrn(channel.getDevice(), channel.getChannelSpec(), range);
    }

    @Override
    public Map<String, RangeSet<Instant>> getLrn(com.energyict.mdc.device.data.Register register, Range<Instant> range) {
        return getLrn(register.getDevice(), register.getRegisterSpec(), range);
    }

    @Override
    public boolean isAnyLrn(long deviceId){
        return isAnyRegisterLrn(deviceId);
                /*TODO: Is LRN needed?
                   || isAnyChannelLrn(deviceId)*/

    }

    @Override
    public void setLrn(Register register, String lrn, Instant startDateTime, Instant endDateTime) {
        Range<Instant> range = getTimeInterval(startDateTime, endDateTime);

        setLrn(register, lrn, range);
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

    private Optional<ChannelSpec> getChannelSpec(Device device, List<? extends ReadingType> readingTypes, Instant when) {
        return device.getHistory(when)
                .map(Device::getDeviceConfiguration)
                .map(DeviceConfiguration::getChannelSpecs)
                .flatMap(specs -> specs.stream().filter(spec -> readingTypes.contains(spec.getReadingType())).findAny());
    }

    private Optional<RegisterSpec> getRegisterSpec(Device device, List<? extends ReadingType> readingTypes, Instant when) {
        return device.getHistory(when)
                .map(Device::getDeviceConfiguration)
                .map(DeviceConfiguration::getRegisterSpecs)
                .flatMap(specs -> specs.stream().filter(spec -> readingTypes.contains(spec.getReadingType())).findAny());
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
        lockRegisterTypeOrThrowException(register.getRegisterSpec().getRegisterType().getId());
        lockRegisterSpecOrThrowException(register.getRegisterSpec().getId());

        addRegisterCustomPropertySetVersioned(register, registerInfo.getId(), DeviceRegisterSAPInfoDomainExtension.FieldNames.LOGICAL_REGISTER_NUMBER.javaName(), lrn, range);
    }


    private boolean isAnyRegisterLrn(long deviceId){
        return getCPSDataModel(DeviceRegisterSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceRegisterSAPInfoDomainExtension.class)
                .join(RegisterSpec.class)
                .join(ReadingType.class)
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.DEVICE_ID.javaName()).isEqualTo(deviceId))
                .findAny()
                .isPresent();
    }

    /*TODO: Is LRN needed?
    private boolean isAnyChannelLrn(long deviceId){
        return getCPSDataModel(DeviceChannelSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceChannelSAPInfoDomainExtension.class)
                .join(ChannelSpec.class)
                .join(ReadingType.class)
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.DEVICE_ID.javaName()).isEqualTo(deviceId))
                .findAny()
                .isPresent();
    }*/

    private Range<Instant> getTimeInterval(Instant startDateTime, Instant endDateTime) {
        Range<Instant> range;
        try {
            if (startDateTime == null) {
                if (endDateTime == null) {
                    range = Range.all();
                } else {
                    range = Range.lessThan(endDateTime);
                }
            } else if (endDateTime == null) {
                range = Range.atLeast(startDateTime);
            } else {
                range = Range.closedOpen(startDateTime, endDateTime);
            }
        } catch (IllegalArgumentException e) {
            throw new SAPWebServiceException(thesaurus,MessageSeeds.INTERVAL_INVALID,
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
                .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.NO_DEVICE_FOUND_BY_SERIAL_ID));
    }

    private void lockDeviceTypeOrThrowException(long id) {
        deviceConfigurationService
                .findAndLockDeviceType(id)
                .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.NO_DEVICE_FOUND_BY_SERIAL_ID));
    }

    private void lockRegisterTypeOrThrowException(long id) {
        masterDataService
                .findAndLockRegisterTypeById(id).orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.REGISTER_NOT_FOUND));

    }

    private void lockRegisterSpecOrThrowException(long registerSpecId) {
        deviceConfigurationService.findAndLockRegisterSpecById(registerSpecId)
                .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.REGISTER_NOT_FOUND));
    }

    private void setDeviceCPSProperty(Device device, String cpsId, String property, String value) {
        RegisteredCustomPropertySet registeredCustomPropertySet = getRegisteredCustomPropertySet(device, cpsId);
        if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.COULD_NOT_FIND_ACTIVE_CPS, cpsId);
        }

        CustomPropertySetValues customPropertySetValues = customPropertySetService.getUniqueValuesFor(registeredCustomPropertySet.getCustomPropertySet(), device);
        customPropertySetValues.setProperty(property, value);
        customPropertySetService.setValuesFor(registeredCustomPropertySet.getCustomPropertySet(), device, customPropertySetValues);
        device.touchDevice();
    }

    private void addRegisterCustomPropertySetVersioned(Register register, String cpsId, String property, String value, Range<Instant> range) {
        RegisteredCustomPropertySet registeredCustomPropertySet = getRegisteredCustomPropertySet(register, cpsId);
        if (!registeredCustomPropertySet.isEditableByCurrentUser()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.COULD_NOT_FIND_ACTIVE_CPS, cpsId);
        }

        CustomPropertySetValues customPropertySetValues;

        if (!range.hasLowerBound()) {
            customPropertySetValues = CustomPropertySetValues.empty();
        }else{
            customPropertySetValues = CustomPropertySetValues.emptyDuring(range);
        }

        customPropertySetValues.setProperty(property, value);

        CustomPropertySetValues savedCustomPropertySetValues = CustomPropertySetValues.empty();


        OverlapCalculatorBuilder overlapCalculatorBuilder = customPropertySetService
                .calculateOverlapsFor(registeredCustomPropertySet.getCustomPropertySet(),
                        register.getRegisterSpec(), register.getDevice().getId());

        for (ValuesRangeConflict conflict : overlapCalculatorBuilder.whenCreating(range)) {
            if (conflict.getType().equals(ValuesRangeConflictType.RANGE_OVERLAP_DELETE)) {
                throw new SAPWebServiceException(thesaurus,MessageSeeds.REGISTER_HAS_LRN_YET,
                        register.getObisCode(), range.toString());
            } else if (conflict.getType().equals(ValuesRangeConflictType.RANGE_GAP_AFTER)) {
                customPropertySetService.setValuesVersionFor(registeredCustomPropertySet.getCustomPropertySet(),
                        register.getRegisterSpec(), CustomPropertySetValues.empty(), conflict.getConflictingRange(), register.getDevice().getId());
            } else if (conflict.getType().equals(ValuesRangeConflictType.RANGE_GAP_BEFORE)) {
                customPropertySetService.setValuesVersionFor(registeredCustomPropertySet.getCustomPropertySet(),
                        register.getRegisterSpec(), CustomPropertySetValues.empty(), conflict.getConflictingRange(), register.getDevice().getId());
            }else if (conflict.getType().equals(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_START)) {
                if(conflict.getValues().getEffectiveRange().hasLowerBound()){
                        throw new SAPWebServiceException(thesaurus, MessageSeeds.REGISTER_HAS_LRN_YET,
                                register.getObisCode(), range.toString());
                }
            }else if (conflict.getType().equals(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_END)) {
                if(conflict.getValues().getEffectiveRange().hasLowerBound()){
                    if(conflict.getValues().getEffectiveRange().hasUpperBound() &&
                            (!conflict.getValues().getEffectiveRange().intersection(conflict.getConflictingRange()).isEmpty())) {
                        throw new SAPWebServiceException(thesaurus,MessageSeeds.REGISTER_HAS_LRN_YET,
                                register.getObisCode(), range.toString());
                    }
                }else{
                    Instant endTime;
                    if(conflict.getValues().getEffectiveRange().hasUpperBound())
                    {
                        endTime = conflict.getValues().getEffectiveRange().upperEndpoint();
                    }else{
                        endTime =  null;
                    }
                    Instant startTime;
                    if(conflict.getConflictingRange().hasUpperBound())
                    {
                        startTime = conflict.getConflictingRange().upperEndpoint();
                    }else{
                        //throw new SAPWebServiceException(thesaurus,MessageSeeds.REGISTER_HAS_LRN_YET,
                        //        register.getObisCode(), range.toString());
                        continue;
                    }

                    savedCustomPropertySetValues = CustomPropertySetValues.emptyDuring(getTimeInterval(startTime, endTime));
                    savedCustomPropertySetValues.setProperty(property, conflict.getValues().getProperty(property));
                }
            }
        }

        customPropertySetService.setValuesVersionFor(registeredCustomPropertySet.getCustomPropertySet(),
                register.getRegisterSpec(), customPropertySetValues, range, register.getDevice().getId());

        if(!savedCustomPropertySetValues.isEmpty()) {
            customPropertySetService.setValuesVersionFor(registeredCustomPropertySet.getCustomPropertySet(),
                    register.getRegisterSpec(), savedCustomPropertySetValues, savedCustomPropertySetValues.getEffectiveRange(), register.getDevice().getId());
        }

        register.getRegisterSpec().save();
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
}
