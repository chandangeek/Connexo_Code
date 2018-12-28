/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig;

import com.elster.connexo._2017.schema.customattributes.Attribute;
import com.elster.connexo._2017.schema.customattributes.CustomAttributeSet;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;

import ch.iec.tc57._2011.usagepointconfig.ConfigurationEvent;
import ch.iec.tc57._2011.usagepointconfig.Name;
import ch.iec.tc57._2011.usagepointconfig.RationalNumber;
import ch.iec.tc57._2011.usagepointconfig.ReadingInterharmonic;
import ch.iec.tc57._2011.usagepointconfig.ServiceCategory;
import ch.iec.tc57._2011.usagepointconfig.ServiceKind;
import ch.iec.tc57._2011.usagepointconfig.Status;
import ch.iec.tc57._2011.usagepointconfig.UsagePoint.MetrologyRequirements;
import ch.iec.tc57._2011.usagepointconfig.UsagePointConfig;
import ch.iec.tc57._2011.usagepointconfig.UsagePointConnectedKind;
import sun.util.calendar.ZoneInfo;

import javax.inject.Inject;
import java.math.BigInteger;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

class UsagePointConfigFactory {
    private final Clock clock;
    private final CustomPropertySetService customPropertySetService;

    @Inject
    UsagePointConfigFactory(Clock clock, CustomPropertySetService customPropertySetService) {
        this.clock = clock;
        this.customPropertySetService = customPropertySetService;
    }

    UsagePointConfig configFrom(UsagePoint... usagePoints) {
        UsagePointConfig usagePointConfig = new UsagePointConfig();
        List<ch.iec.tc57._2011.usagepointconfig.UsagePoint> usagePointsInfo = usagePointConfig.getUsagePoint();
        List<ch.iec.tc57._2011.usagepointconfig.ReadingType> readingTypesInfo = usagePointConfig.getReadingType();
        Set<ReadingType> referencedReadingTypes = new HashSet<>();
        Arrays.stream(usagePoints)
                .forEach(usagePoint -> addUsagePoint(usagePoint, usagePointsInfo, referencedReadingTypes));
        referencedReadingTypes.stream()
                .map(UsagePointConfigFactory::createReadingType)
                .forEach(readingTypesInfo::add);
        return usagePointConfig;
    }

    void addUsagePoint(UsagePoint usagePoint,
                       List<ch.iec.tc57._2011.usagepointconfig.UsagePoint> usagePoints,
                       Set<ReadingType> allReadingTypes) {
        ch.iec.tc57._2011.usagepointconfig.UsagePoint info = new ch.iec.tc57._2011.usagepointconfig.UsagePoint();
        info.setMRID(usagePoint.getMRID());
        info.getNames().add(createName(usagePoint.getName()));

        ServiceCategory serviceCategory = new ServiceCategory();
        ServiceKind serviceKind = ServiceKind.fromValue(usagePoint.getServiceCategory().getKind().getDisplayName());
        serviceCategory.setKind(serviceKind);
        info.setServiceCategory(serviceCategory);

        //change life cycle fails because of this.
        /*if (serviceKind == ServiceKind.ELECTRICITY) {
            usagePoint.getDetail(clock.instant())
                    .filter(ElectricityDetail.class::isInstance)
                    .map(ElectricityDetail.class::cast)
                    .map(ElectricityDetail::getPhaseCode)
                    .flatMap(Optional::ofNullable)
                    .map(PhaseCode::getValue)
                    .map(ch.iec.tc57._2011.usagepointconfig.PhaseCode::fromValue)
                    .ifPresent(info::setPhaseCode);
        }*/

        info.setIsSdp(usagePoint.isSdp());
        info.setIsVirtual(usagePoint.isVirtual());

        addMetrologyRequirements(usagePoint, info.getMetrologyRequirements(), allReadingTypes);

        Status status = new Status();
        String stateKey = usagePoint.getState().getName();
        status.setValue(DefaultUsagePointStateFinder.findForKey(stateKey)
                .map(DefaultState::getTranslation)
                .map(TranslationKey::getDefaultFormat)
                .orElse(stateKey));
        info.setStatus(status);

        ConfigurationEvent configuration = new ConfigurationEvent();
        configuration.setCreatedDateTime(usagePoint.getInstallationTime());
        info.setConfigurationEvents(configuration);

        Set<ConnectionState> supportedStates = Arrays.stream(ConnectionState.supportedValues())
                .collect(Collectors.toSet());
        usagePoint.getCurrentConnectionState()
                .map(UsagePointConnectionState::getConnectionState)
                .filter(supportedStates::contains)
                .map(ConnectionState::getId)
                .map(UsagePointConnectedKind::fromValue)
                .ifPresent(info::setConnectionState);
        info.getCustomAttributeSet().addAll(getCustomAttributes(usagePoint));

        usagePoints.add(info);
    }

    private Collection<CustomAttributeSet> getCustomAttributes(UsagePoint usagePoint) {
        return usagePoint.forCustomProperties().getAllPropertySets()
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .map(registeredCustomPropertySet -> convertToCustomAttributeSet(registeredCustomPropertySet, usagePoint))
                .collect(Collectors.toList());

    }

    private CustomAttributeSet convertToCustomAttributeSet(UsagePointPropertySet registeredCustomPropertySet, UsagePoint usagePoint) {
        CustomAttributeSet customAttribute = new CustomAttributeSet();
        CustomPropertySetValues values = null;
        CustomPropertySet propertySet = registeredCustomPropertySet.getCustomPropertySet();
        if (!propertySet.isVersioned()) {
            values = customPropertySetService.getUniqueValuesFor(propertySet, usagePoint);
        } else {
            values = customPropertySetService.getUniqueValuesFor(propertySet, usagePoint, Instant.now());
        }
        if (values == null || values.isEmpty()) {
            List<PropertySpec> propertySpecs = propertySet.getPropertySpecs();
            customAttribute.setId(propertySet.getName());
            for (PropertySpec propertySpec : propertySpecs) {
                Attribute attr = new Attribute();
                attr.setName(propertySpec.getName());
                Object propertyValue = getPropertyValue(propertySpec);
                attr.setValue(convertPropertyValue(propertyValue));
                customAttribute.getAttribute().add(attr);
            }
        } else {
            setAttrToCustomAttribute(propertySet, values, customAttribute);
        }
        return customAttribute;
    }

    private void setAttrToCustomAttribute(CustomPropertySet propertySet, CustomPropertySetValues values, CustomAttributeSet customAttribute) {
        customAttribute.setId(propertySet.getName());
        for (String property : values.propertyNames()) {
            Attribute attr = new Attribute();
            attr.setName(property);
            attr.setValue(convertPropertyValue(values.getProperty(property)));
            customAttribute.getAttribute().add(attr);
            if (propertySet.isVersioned()) {
                if (values.getEffectiveRange().hasLowerBound()) {
                    customAttribute.setFromDateTime(values.getEffectiveRange().lowerEndpoint());
                } else {
                    customAttribute.setFromDateTime(Instant.MIN.plusNanos(1));
                }
                if (values.getEffectiveRange().hasUpperBound()) {
                    customAttribute.setToDateTime(Instant.ofEpochMilli(Long.MIN_VALUE));
                } else {
                    customAttribute.setToDateTime(Instant.ofEpochMilli(Long.MAX_VALUE));
                }
            }
        }
    }

    private Object getPropertyValue(PropertySpec propertySpec) {
        return propertySpec.getPossibleValues() == null ? null : propertySpec.getPossibleValues().getDefault();
    }

    private String convertPropertyValue(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof ZoneInfo) {
            return ((ZoneInfo)value).getID();
        } else {
            return String.valueOf(value);
        }
    }

    private void addMetrologyRequirements(UsagePoint usagePoint,
                                          List<MetrologyRequirements> metrologyRequirements,
                                          Set<ReadingType> allReadingTypes) {
        Instant now = clock.instant();
        usagePoint.getEffectiveMetrologyConfiguration(now)
                .ifPresent(effectiveMC -> {
                    MetrologyConfiguration mc = effectiveMC.getMetrologyConfiguration();
                    mc.getContracts().stream()
                            .filter(contract -> effectiveMC.getChannelsContainer(contract, now).isPresent())
                            .map(contract -> createMetrologyRequirement(contract, mc, allReadingTypes))
                            .forEach(metrologyRequirements::add);
                });
    }

    private MetrologyRequirements createMetrologyRequirement(MetrologyContract contract,
                                                             MetrologyConfiguration mc,
                                                             Set<ReadingType> allReadingTypes) {
        MetrologyRequirements info = new MetrologyRequirements();
        MetrologyRequirements.Names name = new MetrologyRequirements.Names();
        name.setName(mc.getName());
        info.getNames().add(name);
        info.setReason(contract.getMetrologyPurpose().getName());
        List<MetrologyRequirements.ReadingTypes> references = info.getReadingTypes();
        contract.getDeliverables().stream()
                .map(ReadingTypeDeliverable::getReadingType)
                .peek(readingType -> addReadingTypeReference(readingType, references))
                .forEach(allReadingTypes::add);
        return info;
    }

    private void addReadingTypeReference(ReadingType readingType,
                                         List<MetrologyRequirements.ReadingTypes> references) {
        MetrologyRequirements.ReadingTypes reference = new MetrologyRequirements.ReadingTypes();
        reference.setRef(readingType.getMRID());
        references.add(reference);
    }

    private static ch.iec.tc57._2011.usagepointconfig.ReadingType createReadingType(ReadingType readingType) {
        ch.iec.tc57._2011.usagepointconfig.ReadingType info = new ch.iec.tc57._2011.usagepointconfig.ReadingType();
        info.setMRID(readingType.getMRID());
        info.getNames().add(createName(readingType.getFullAliasName()));
        info.setAccumulation(readingType.getAccumulation().getDescription());
        info.setAggregate(readingType.getAggregate().getDescription());
        info.setArgument(createRationalNumber(readingType.getArgument()));
        info.setCommodity(readingType.getCommodity().getDescription());
        info.setConsumptionTier(BigInteger.valueOf(readingType.getConsumptionTier()));
        info.setCpp(BigInteger.valueOf(readingType.getCpp()));
        info.setCurrency(readingType.getCurrency().getCurrencyCode());
        info.setFlowDirection(readingType.getFlowDirection().getDescription());
        info.setInterharmonic(createReadingInterharmonic(readingType.getInterharmonic()));
        info.setMacroPeriod(readingType.getMacroPeriod().getDescription());
        info.setMeasurementKind(readingType.getMeasurementKind().getDescription());
        info.setMeasuringPeriod(readingType.getMeasuringPeriod().getDescription());
        info.setMultiplier(readingType.getMultiplier().toString());
        info.setPhases(readingType.getPhases().getDescription());
        info.setTou(BigInteger.valueOf(readingType.getTou()));
        info.setUnit(readingType.getUnit().getName());
        return info;
    }

    private static RationalNumber createRationalNumber(com.elster.jupiter.cbo.RationalNumber rational) {
        return Optional.ofNullable(rational)
                .filter(number -> !com.elster.jupiter.cbo.RationalNumber.NOTAPPLICABLE.equals(number))
                .map(number -> {
                    RationalNumber info = new RationalNumber();
                    info.setNumerator(BigInteger.valueOf(rational.getNumerator()));
                    info.setDenominator(BigInteger.valueOf(rational.getDenominator()));
                    return info;
                })
                .orElse(null);
    }

    private static ReadingInterharmonic createReadingInterharmonic(com.elster.jupiter.cbo.RationalNumber rational) {
        return Optional.ofNullable(rational)
                .filter(number -> !com.elster.jupiter.cbo.RationalNumber.NOTAPPLICABLE.equals(number))
                .map(number -> {
                    ReadingInterharmonic info = new ReadingInterharmonic();
                    info.setNumerator(BigInteger.valueOf(rational.getNumerator()));
                    info.setDenominator(BigInteger.valueOf(rational.getDenominator()));
                    return info;
                })
                .orElse(null);
    }

    private static Name createName(String value) {
        Name name = new Name();
        name.setName(value);
        return name;
    }
}
