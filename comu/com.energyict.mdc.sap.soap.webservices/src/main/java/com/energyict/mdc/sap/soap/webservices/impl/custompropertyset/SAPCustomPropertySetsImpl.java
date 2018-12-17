/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.custompropertyset;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.HardCodedFieldNames;
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
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;

import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.math.BigDecimal;
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

    private CustomPropertySet<Device, DeviceSAPInfoDomainExtension> deviceInfo;
    private CustomPropertySet<ChannelSpec, DeviceChannelSAPInfoDomainExtension> channelInfo;
    private CustomPropertySet<RegisterSpec, DeviceRegisterSAPInfoDomainExtension> registerInfo;

    public SAPCustomPropertySetsImpl() {
        // for OSGi purposes
    }

    @Inject
    public SAPCustomPropertySetsImpl(DeviceService deviceService, CustomPropertySetService customPropertySetService,
                                     PropertySpecService propertySpecService, OrmService ormService,
                                     NlsService nlsService) {
        setDeviceService(deviceService);
        setCustomPropertySetService(customPropertySetService);
        setPropertySpecService(propertySpecService);
        setOrmService(ormService);
        setNlsService(nlsService);
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
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(getComponentName(), getLayer());
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
    public Optional<BigDecimal> getSapDeviceId(Device device) {
        return getCPSDataModel(DeviceSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceSAPInfoDomainExtension.class)
                .filter(Where.where(DeviceSAPInfoDomainExtension.FieldNames.DOMAIN.javaName()).isEqualTo(device))
                .findAny()
                .flatMap(DeviceSAPInfoDomainExtension::getDeviceIdentifier);
    }

    @Override
    public Optional<BigDecimal> getSapDeviceId(String deviceName) {
        return deviceService.findDeviceByName(deviceName)
                .flatMap(this::getSapDeviceId);
    }

    @Override
    public Optional<Device> getDevice(BigDecimal sapDeviceId) {
        return getCPSDataModel(DeviceSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceSAPInfoDomainExtension.class)
                .join(Device.class)
                .filter(Where.where(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_IDENTIFIER.javaName()).isEqualTo(sapDeviceId))
                .findAny()
                .map(DeviceSAPInfoDomainExtension::getDevice);
    }

    @Override
    public Map<BigDecimal, RangeSet<Instant>> getLrn(Channel channel, Range<Instant> range) {
        return channel.getChannelsContainer().getMeter()
                .map(Meter::getId)
                .flatMap(deviceService::findDeviceByMeterId)
                .map(device -> getLrn(device, channel, range))
                .orElseGet(Collections::emptyMap);
    }

    @Override
    public Map<BigDecimal, RangeSet<Instant>> getLrn(com.energyict.mdc.device.data.Channel channel, Range<Instant> range) {
        return getLrn(channel.getDevice(), channel.getChannelSpec(), range);
    }

    @Override
    public Map<BigDecimal, RangeSet<Instant>> getLrn(com.energyict.mdc.device.data.Register register, Range<Instant> range) {
        return getLrn(register.getDevice(), register.getRegisterSpec(), range);
    }

    @Override
    public Optional<Channel> getChannel(BigDecimal lrn, Instant when) {
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

    private Optional<Pair<Long, ReadingType>> getChannelIdentification(BigDecimal lrn, Instant when) {
        return getCPSDataModel(DeviceChannelSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceChannelSAPInfoDomainExtension.class)
                .join(ChannelSpec.class)
                .join(ReadingType.class)
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.LOGICAL_REGISTER_NUMBER.javaName()).isEqualTo(lrn))
                .filter(Where.where(HardCodedFieldNames.INTERVAL.javaName()).isEffective(when))
                .findAny()
                .map(ext -> Pair.of(ext.getDeviceId(), ext.getChannelSpec().getReadingType()));
    }

    private Optional<Pair<Long, ReadingType>> getRegisterIdentification(BigDecimal lrn, Instant when) {
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

    private Map<BigDecimal, RangeSet<Instant>> getLrn(Device device, Channel channel, Range<Instant> range) {
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

    private Map<BigDecimal, RangeSet<Instant>> getLrn(Device device, ChannelSpec channelSpec, Range<Instant> range) {
        return getCPSDataModel(DeviceChannelSAPInfoCustomPropertySet.MODEL_NAME)
                .stream(DeviceChannelSAPInfoDomainExtension.class)
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.DOMAIN.javaName()).isEqualTo(channelSpec))
                .filter(Where.where(DeviceChannelSAPInfoDomainExtension.FieldNames.DEVICE_ID.javaName()).isEqualTo(device.getId()))
                .filter(Where.where(HardCodedFieldNames.INTERVAL.javaName()).isEffective(range))
                .map(ext -> ext.getLogicalRegisterNumber().map(lrn -> Pair.of(lrn, ImmutableRangeSet.of(ext.getRange().intersection(range)))))
                .flatMap(Functions.asStream())
                .collect(Collectors.toMap(Pair::getFirst, Pair::getLast, RangeSets::union));
    }

    private Map<BigDecimal, RangeSet<Instant>> getLrn(Device device, RegisterSpec registerSpec, Range<Instant> range) {
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
}
