/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.GasDetailBuilder;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.HeatDetailBuilder;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.metering.UsagePointPropertySet;
import com.elster.jupiter.metering.UsagePointVersionedPropertySet;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.metering.WaterDetailBuilder;
import com.elster.jupiter.metering.imports.impl.CustomPropertySetRecord;
import com.elster.jupiter.metering.imports.impl.FileImportLogger;
import com.elster.jupiter.metering.imports.impl.MessageSeeds;
import com.elster.jupiter.metering.imports.impl.MeteringDataImporterContext;
import com.elster.jupiter.metering.imports.impl.exceptions.ProcessorException;
import com.elster.jupiter.properties.PropertySpec;

import com.google.common.collect.Range;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class UsagePointsImportProcessor extends AbstractImportProcessor<UsagePointImportRecord> {

    UsagePointsImportProcessor(MeteringDataImporterContext context) {
        super(context);
    }

    @Override
    public void process(UsagePointImportRecord data, FileImportLogger logger) throws ProcessorException {
        try {
            validate(data, logger);
            UsagePoint usagePoint = getUsagePoint(data, logger);
            if (usagePoint.getDetail(getClock().instant()).isPresent()) {
                updateDetails(usagePoint, data, logger).create();
            } else {
                createDetails(usagePoint, data, logger).create();
            }
        } catch (ConstraintViolationException e) {
            for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
                logger.warning(MessageSeeds.IMPORT_USAGEPOINT_CONSTRAINT_VOLATION, data.getLineNumber(),
                        violation.getPropertyPath(), violation.getMessage());
            }
            throw new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_INVALIDDATA, data.getLineNumber());
        }
    }

    private void validate(UsagePointImportRecord data, FileImportLogger logger) throws ProcessorException {
        String identifier = data.getUsagePointIdentifier()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_IDENTIFIER_INVALID, data.getLineNumber()));
        String serviceKindString = data.getServiceKind()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID, data.getLineNumber()));
        ServiceKind serviceKind = Arrays.stream(ServiceKind.values()).filter(candidate -> candidate.name().equalsIgnoreCase(serviceKindString)).findFirst()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_NO_SUCH_SERVICEKIND, data.getLineNumber(), serviceKindString));
        ServiceCategory serviceCategory = getContext().getMeteringService().getServiceCategory(serviceKind)
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICECATEGORY_INVALID, data.getLineNumber(), serviceKindString));

        Optional<UsagePoint> usagePoint = findUsagePointByIdentifier(identifier);
        if (usagePoint.isPresent()) {
            if (data.isAllowUpdate()) {
                updateDetails(usagePoint.get(), data, logger).validate();
                validateCustomPropertySetValues(usagePoint.get(), data);
            } else {
                throw new ProcessorException(MessageSeeds.UPDATE_NOT_ALLOWED, data.getLineNumber());
            }
        } else {
            if (data.isAllowUpdate()) {
                throw new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_NOT_FOUND, data.getLineNumber(), data.getUsagePointIdentifier().get());
            }
            UsagePoint dummyUsagePoint = serviceCategory.newUsagePoint(identifier, data.getInstallationTime().orElse(getClock().instant())).validate();
            createDetails(dummyUsagePoint, data, logger).validate();
            validateMandatoryCustomProperties(dummyUsagePoint, data);
            validateCustomPropertySetValues(dummyUsagePoint, data);
        }
    }

    private UsagePoint getUsagePoint(UsagePointImportRecord data, FileImportLogger logger) {
        String identifier = data.getUsagePointIdentifier()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_IDENTIFIER_INVALID, data.getLineNumber()));
        String serviceKindString = data.getServiceKind()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID, data.getLineNumber()));
        ServiceKind serviceKind = Arrays.stream(ServiceKind.values()).filter(candidate -> candidate.name().equalsIgnoreCase(serviceKindString)).findFirst()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_NO_SUCH_SERVICEKIND, data.getLineNumber(), serviceKindString));
        Optional<UsagePoint> foundUsagePoint = findUsagePointByIdentifier(identifier);
        Optional<ServiceCategory> serviceCategory = getContext().getMeteringService().getServiceCategory(serviceKind);
        if (foundUsagePoint.isPresent()) {
            UsagePoint usagePoint = foundUsagePoint.get();
            if (usagePoint.getServiceCategory().getId() != serviceCategory.get().getId()) {
                throw new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICECATEGORY_CHANGE, data.getLineNumber(), serviceKindString);
            }
            usagePoint = getContext().getMeteringService().findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion()).get();
            return updateUsagePoint(usagePoint, data);
        } else {
            UsagePointBuilder usagePointBuilder = serviceCategory.get().newUsagePoint(identifier, data.getInstallationTime().orElse(getContext().getClock().instant()));
            return createUsagePoint(usagePointBuilder, data);
        }
    }

    private UsagePointDetailBuilder createDetails(UsagePoint usagePoint, UsagePointImportRecord data, FileImportLogger logger) {
        switch (usagePoint.getServiceCategory().getKind()) {
            case ELECTRICITY:
                return buildElectricityDetails(usagePoint.newElectricityDetailBuilder(getClock().instant()),
                        (ElectricityDetail) usagePoint.getServiceCategory()
                                .newUsagePointDetail(usagePoint, getClock().instant()), data);
            case GAS:
                return buildGasDetails(usagePoint.newGasDetailBuilder(getClock().instant()),
                        (GasDetail) usagePoint.getServiceCategory()
                                .newUsagePointDetail(usagePoint, getClock().instant()), data);
            case WATER:
                return buildWaterDetails(usagePoint.newWaterDetailBuilder(getClock().instant()),
                        (WaterDetail) usagePoint.getServiceCategory()
                                .newUsagePointDetail(usagePoint, getClock().instant()), data);
            case HEAT:
                return buildHeatDetails(usagePoint.newHeatDetailBuilder(getClock().instant()),
                        (HeatDetail) usagePoint.getServiceCategory()
                                .newUsagePointDetail(usagePoint, getClock().instant()), data);
            default:
                return addBaseDetails(usagePoint.newDefaultDetailBuilder(getClock().instant()),
                        usagePoint.getServiceCategory()
                                .newUsagePointDetail(usagePoint, getClock().instant()), data);
        }
    }

    private UsagePointDetailBuilder updateDetails(UsagePoint usagePoint, UsagePointImportRecord data, FileImportLogger logger) {
        UsagePointDetail detail = usagePoint.getDetail(getClock().instant())
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICECATEGORY_INVALID, data.getLineNumber(), data.getServiceKind().get()));

        switch (usagePoint.getServiceCategory().getKind()) {
            case ELECTRICITY:
                return buildElectricityDetails(usagePoint.newElectricityDetailBuilder(getClock().instant()), (ElectricityDetail) detail, data);
            case GAS:
                return buildGasDetails(usagePoint.newGasDetailBuilder(getClock().instant()),
                        (GasDetail) detail, data);
            case WATER:
                return buildWaterDetails(usagePoint.newWaterDetailBuilder(getClock().instant()),
                        (WaterDetail) detail, data);
            case HEAT:
                return buildHeatDetails(usagePoint.newHeatDetailBuilder(getClock().instant()),
                        (HeatDetail) detail, data);
            default:
                return addBaseDetails(usagePoint.newDefaultDetailBuilder(getClock().instant()),
                        detail, data);
        }
    }

    private UsagePoint createUsagePoint(UsagePointBuilder usagePointBuilder, UsagePointImportRecord data) {
        usagePointBuilder.withIsSdp(data.isSdp());
        usagePointBuilder.withOutageRegion(data.getOutageRegion());
        usagePointBuilder.withReadRoute(data.getReadRoute());
        usagePointBuilder.withServicePriority(data.getServicePriority());
        usagePointBuilder.withServiceDeliveryRemark(data.getServiceDeliveryRemark());
        setLocation(usagePointBuilder, data);
        addCustomPropertySetsValues(usagePointBuilder, data);
        return usagePointBuilder.create();
    }

    private void addCustomPropertySetsValues(UsagePointBuilder usagePointBuilder, UsagePointImportRecord data) {
        data.getRegisteredCustomPropertySets().forEach((customPropertySet, customPropertySetRecord) -> {
                    CustomPropertySetValues values = null;
                    if (customPropertySet.getCustomPropertySet().isVersioned()) {
                        Range<Instant> rangeToCreate = getRangeToCreate(customPropertySetRecord);
                        if (!rangeToCreate.hasLowerBound()) {
                            rangeToCreate = Range.atLeast(data.getInstallationTime().orElse(getClock().instant())).intersection(rangeToCreate);
                        }
                        values = CustomPropertySetValues.emptyDuring(rangeToCreate);
                        copyValues(customPropertySetRecord.getCustomPropertySetValues(), values);
                    } else {
                        values = customPropertySetRecord.getCustomPropertySetValues();
                    }
                    usagePointBuilder.addCustomPropertySetValues(customPropertySet, values);
                }
        );
    }

    private void copyValues(CustomPropertySetValues source, CustomPropertySetValues target) {
        source.propertyNames().forEach(propertyName -> {
            Object propertyValue = source.getProperty(propertyName);
            target.setProperty(propertyName, propertyValue);
        });
    }

    @Override
    protected UsagePoint updateUsagePoint(UsagePoint usagePoint, UsagePointImportRecord data) {
        usagePoint = super.updateUsagePoint(usagePoint, data);
        usagePoint.setOutageRegion(data.getOutageRegion());
        usagePoint.setReadRoute(data.getReadRoute());
        usagePoint.setServicePriority(data.getServicePriority());
        usagePoint.setServiceDeliveryRemark(data.getServiceDeliveryRemark());
        addCustomPropertySetValues(usagePoint, data);
        usagePoint.update();
        return usagePoint;
    }

    private ElectricityDetailBuilder buildElectricityDetails(ElectricityDetailBuilder detailBuilder, ElectricityDetail oldDetail, UsagePointImportRecord data) {
        detailBuilder.withCollar(data.isCollarInstalled().orElse(oldDetail.isCollarInstalled()));
        detailBuilder.withGrounded(data.isGrounded().orElse(oldDetail.isGrounded()));
        detailBuilder.withNominalServiceVoltage(data.getNominalVoltage().orElse(oldDetail.getNominalServiceVoltage()));
        detailBuilder.withPhaseCode(data.getPhaseCode().orElse(oldDetail.getPhaseCode()));
        detailBuilder.withRatedCurrent(data.getRatedCurrent().orElse(oldDetail.getRatedCurrent()));
        detailBuilder.withRatedPower(data.getRatedPower().orElse(oldDetail.getRatedPower()));
        detailBuilder.withEstimatedLoad(data.getEstimatedLoad().orElse(oldDetail.getEstimatedLoad()));
        detailBuilder.withLimiter(data.isLimiterInstalled().orElse(oldDetail.isLimiter()));
        detailBuilder.withLoadLimiterType(data.getLoadLimiterType().orElse(oldDetail.getLoadLimiterType()));
        detailBuilder.withLoadLimit(data.getLoadLimit().orElse(oldDetail.getLoadLimit()));
        detailBuilder.withInterruptible(data.isInterruptible().orElse(oldDetail.isInterruptible()));
        return detailBuilder;
    }

    private GasDetailBuilder buildGasDetails(GasDetailBuilder detailBuilder, GasDetail oldDetail, UsagePointImportRecord data) {
        detailBuilder.withCollar(data.isCollarInstalled().orElse(oldDetail.isCollarInstalled()));
        detailBuilder.withGrounded(data.isGrounded().orElse(oldDetail.isGrounded()));
        detailBuilder.withPressure(data.getPressure().orElse(oldDetail.getPressure()));
        detailBuilder.withPhysicalCapacity(data.getPhysicalCapacity().orElse(oldDetail.getPhysicalCapacity()));
        detailBuilder.withLimiter(data.isLimiterInstalled().orElse(oldDetail.isLimiter()));
        detailBuilder.withLoadLimiterType(data.getLoadLimiterType().orElse(oldDetail.getLoadLimiterType()));
        detailBuilder.withLoadLimit(data.getLoadLimit().orElse(oldDetail.getLoadLimit()));
        detailBuilder.withBypass(data.isBypassInstalled().orElse(oldDetail.isBypassInstalled()));
        detailBuilder.withBypassStatus(data.getBypassStatus().orElse(oldDetail.getBypassStatus()));
        detailBuilder.withValve(data.isValveInstalled().orElse(oldDetail.isValveInstalled()));
        detailBuilder.withCap(data.isCapped().orElse(oldDetail.isCapped()));
        detailBuilder.withClamp(data.isClamped().orElse(oldDetail.isClamped()));
        detailBuilder.withInterruptible(data.isInterruptible().orElse(oldDetail.isInterruptible()));
        return detailBuilder;
    }

    private WaterDetailBuilder buildWaterDetails(WaterDetailBuilder detailBuilder, WaterDetail oldDetail, UsagePointImportRecord data) {
        detailBuilder.withCollar(data.isCollarInstalled().orElse(oldDetail.isCollarInstalled()));
        detailBuilder.withGrounded(data.isGrounded().orElse(oldDetail.isGrounded()));
        detailBuilder.withPressure(data.getPressure().orElse(oldDetail.getPressure()));
        detailBuilder.withPhysicalCapacity(data.getPhysicalCapacity().orElse(oldDetail.getPhysicalCapacity()));
        detailBuilder.withLimiter(data.isLimiterInstalled().orElse(oldDetail.isLimiter()));
        detailBuilder.withLoadLimiterType(data.getLoadLimiterType().orElse(oldDetail.getLoadLimiterType()));
        detailBuilder.withLoadLimit(data.getLoadLimit().orElse(oldDetail.getLoadLimit()));
        detailBuilder.withBypass(data.isBypassInstalled().orElse(oldDetail.isBypassInstalled()));
        detailBuilder.withBypassStatus(data.getBypassStatus().orElse(oldDetail.getBypassStatus()));
        detailBuilder.withValve(data.isValveInstalled().orElse(oldDetail.isValveInstalled()));
        detailBuilder.withCap(data.isCapped().orElse(oldDetail.isCapped()));
        detailBuilder.withClamp(data.isClamped().orElse(oldDetail.isClamped()));
        return detailBuilder;
    }

    private HeatDetailBuilder buildHeatDetails(HeatDetailBuilder detailBuilder, HeatDetail oldDetail, UsagePointImportRecord data) {
        detailBuilder.withCollar(data.isCollarInstalled().orElse(oldDetail.isCollarInstalled()));
        detailBuilder.withPressure(data.getPressure().orElse(oldDetail.getPressure()));
        detailBuilder.withPhysicalCapacity(data.getPhysicalCapacity().orElse(oldDetail.getPhysicalCapacity()));
        detailBuilder.withBypass(data.isBypassInstalled().orElse(oldDetail.isBypassInstalled()));
        detailBuilder.withBypassStatus(data.getBypassStatus().orElse(oldDetail.getBypassStatus()));
        detailBuilder.withValve(data.isValveInstalled().orElse(oldDetail.isValveInstalled()));
        return detailBuilder;
    }

    private UsagePointDetailBuilder addBaseDetails(UsagePointDetailBuilder detailBuilder, UsagePointDetail oldDetail, UsagePointImportRecord data) {
        detailBuilder.withCollar(data.isCollarInstalled().orElse(oldDetail.isCollarInstalled()));
        return detailBuilder;
    }

    private void addCustomPropertySetValues(UsagePoint usagePoint, UsagePointImportRecord data) {
        Map<CustomPropertySet, CustomPropertySetRecord> customPropertySetValues = data.getCustomPropertySets();

        for (UsagePointPropertySet propertySet : usagePoint.forCustomProperties().getAllPropertySets()) {
            if (customPropertySetValues.containsKey(propertySet.getCustomPropertySet())) {
                if (propertySet instanceof UsagePointVersionedPropertySet) {
                    createOrUpdateVersionedSet((UsagePointVersionedPropertySet) propertySet, customPropertySetValues.get(propertySet.getCustomPropertySet()));
                } else {
                    createOrUpdateNonVersionedSet(propertySet, customPropertySetValues.get(propertySet.getCustomPropertySet()));
                }
            }
        }
    }

    private void validateMandatoryCustomProperties(UsagePoint usagePoint, UsagePointImportRecord data) {
        Map<CustomPropertySet, CustomPropertySetRecord> customPropertySetValues = data.getCustomPropertySets();

        for (UsagePointPropertySet propertySet : usagePoint.forCustomProperties().getAllPropertySets()) {
            for (PropertySpec propertySpec : propertySet.getCustomPropertySet().getPropertySpecs()) {
                if (propertySpec.isRequired()) {
                    Optional.ofNullable(customPropertySetValues.get(propertySet.getCustomPropertySet()))
                            .filter(customPropertySetRecord -> customPropertySetRecord.getCustomPropertySetValues().getProperty(propertySpec.getName()) != null)
                            .orElseThrow(() -> new ProcessorException(
                                    MessageSeeds.NO_SUCH_MANDATORY_CPS_VALUE,
                                    data.getLineNumber(),
                                    propertySpec.getDisplayName(),
                                    propertySet.getCustomPropertySet().getName()));
                }
            }
        }
    }

    private void validateCustomPropertySetValues(UsagePoint usagePoint, UsagePointImportRecord data) {
        Map<CustomPropertySet, CustomPropertySetRecord> customPropertySetValues = data.getCustomPropertySets();

        for (UsagePointPropertySet propertySet : usagePoint.forCustomProperties().getAllPropertySets()) {
            if (customPropertySetValues.containsKey(propertySet.getCustomPropertySet())) {
                if (propertySet instanceof UsagePointVersionedPropertySet) {
                    validateCreateOrUpdateVersionedSet((UsagePointVersionedPropertySet) propertySet, customPropertySetValues.get(propertySet.getCustomPropertySet()));
                } else {
                    validateCreateOrUpdateNonVersionedSet(propertySet, customPropertySetValues.get(propertySet.getCustomPropertySet()));
                }
            }
        }
    }

    private void validateCreateOrUpdateVersionedSet(UsagePointVersionedPropertySet usagePointCustomPropertySet, CustomPropertySetRecord customPropertySetRecord) {
        if (customPropertySetRecord.getVersionId().isPresent()) {
            CustomPropertySetValues values = usagePointCustomPropertySet.getVersionValues(customPropertySetRecord.getVersionId().get());
            if (values != null) {
                usagePointCustomPropertySet.getCustomPropertySet()
                        .getPropertySpecs()
                        .stream()
                        .filter(spec -> customPropertySetRecord.getCustomPropertySetValues()
                                .getProperty(spec.getName()) != null)
                        .forEach(spec -> values.setProperty(spec.getName(),
                                customPropertySetRecord.getCustomPropertySetValues().getProperty(spec.getName())));
                getContext().getCustomPropertySetService()
                        .validateCustomPropertySetValues(usagePointCustomPropertySet.getCustomPropertySet(), values);
            } else {
                throw new ProcessorException(
                        MessageSeeds.IMPORT_VERSIONED_VALUES_NOT_FOUND,
                        customPropertySetRecord.getLineNumber(),
                        customPropertySetRecord.getVersionId().get().toString());
            }
        } else {
            getContext().getCustomPropertySetService()
                    .validateCustomPropertySetValues(usagePointCustomPropertySet.getCustomPropertySet(),
                            customPropertySetRecord.getCustomPropertySetValues());
        }
    }

    private void createOrUpdateVersionedSet(UsagePointVersionedPropertySet usagePointCustomPropertySet, CustomPropertySetRecord customPropertySetRecord) {
        if (customPropertySetRecord.getVersionId().isPresent()) {
            CustomPropertySetValues values = usagePointCustomPropertySet.getVersionValues(customPropertySetRecord.getVersionId().get());
            if (values != null) {
                usagePointCustomPropertySet.getCustomPropertySet()
                        .getPropertySpecs()
                        .stream()
                        .filter(spec -> customPropertySetRecord.getCustomPropertySetValues()
                                .getProperty(spec.getName()) != null)
                        .forEach(spec -> values.setProperty(spec.getName(),
                                customPropertySetRecord.getCustomPropertySetValues().getProperty(spec.getName())));
                getContext().getCustomPropertySetService()
                        .setValuesVersionFor(usagePointCustomPropertySet.getCustomPropertySet(),
                                usagePointCustomPropertySet.getUsagePoint(),
                                values,
                                getRangeToUpdate(
                                        customPropertySetRecord,
                                        values.getEffectiveRange()),
                                customPropertySetRecord.getVersionId().get());
            } else {
                throw new ProcessorException(
                        MessageSeeds.IMPORT_VERSIONED_VALUES_NOT_FOUND,
                        customPropertySetRecord.getLineNumber(),
                        customPropertySetRecord.getVersionId().get().toString());
            }
        } else {
            getContext().getCustomPropertySetService()
                    .setValuesVersionFor(usagePointCustomPropertySet.getCustomPropertySet(), usagePointCustomPropertySet
                                    .getUsagePoint(),
                            customPropertySetRecord.getCustomPropertySetValues(), getRangeToCreate(customPropertySetRecord));
        }
    }

    private void validateCreateOrUpdateNonVersionedSet(UsagePointPropertySet usagePointCustomPropertySet, CustomPropertySetRecord customPropertySetRecord) {
        CustomPropertySetValues values = usagePointCustomPropertySet.getValues();
        if (values != null) {
            usagePointCustomPropertySet.getCustomPropertySet()
                    .getPropertySpecs()
                    .stream()
                    .filter(spec -> customPropertySetRecord.getCustomPropertySetValues()
                            .getProperty(spec.getName()) != null)
                    .forEach(spec -> values.setProperty(spec.getName(), customPropertySetRecord.getCustomPropertySetValues()
                            .getProperty(spec.getName())));
            getContext().getCustomPropertySetService()
                    .validateCustomPropertySetValues(usagePointCustomPropertySet.getCustomPropertySet(), values);
        }
        getContext().getCustomPropertySetService()
                .validateCustomPropertySetValues(usagePointCustomPropertySet.getCustomPropertySet(), values);
    }

    private void createOrUpdateNonVersionedSet(UsagePointPropertySet usagePointCustomPropertySet, CustomPropertySetRecord customPropertySetRecord) {
        CustomPropertySetValues values = usagePointCustomPropertySet.getValues();
        if (values != null) {
            usagePointCustomPropertySet.getCustomPropertySet()
                    .getPropertySpecs()
                    .stream()
                    .filter(spec -> customPropertySetRecord.getCustomPropertySetValues()
                            .getProperty(spec.getName()) != null)
                    .forEach(spec -> values.setProperty(spec.getName(), customPropertySetRecord.getCustomPropertySetValues()
                            .getProperty(spec.getName())));
            getContext().getCustomPropertySetService()
                    .setValuesFor(usagePointCustomPropertySet.getCustomPropertySet(), usagePointCustomPropertySet.getUsagePoint(),
                            values);
        }

        usagePointCustomPropertySet.setValues(values);
    }

    private Range<Instant> getRangeToCreate(CustomPropertySetRecord customPropertySetRecord) {
        if ((!customPropertySetRecord.getStartTime().isPresent() || customPropertySetRecord.getStartTime().get().equals(Instant.EPOCH))
                && (!customPropertySetRecord.getEndTime().isPresent() || customPropertySetRecord.getEndTime().get().equals(Instant.EPOCH))) {
            return Range.all();
        } else if (!customPropertySetRecord.getStartTime().isPresent() || customPropertySetRecord.getStartTime().get().equals(Instant.EPOCH)) {
            return Range.lessThan(customPropertySetRecord.getEndTime().get());
        } else if (!customPropertySetRecord.getEndTime().isPresent() || customPropertySetRecord.getEndTime().get().equals(Instant.EPOCH)) {
            return Range.atLeast(customPropertySetRecord.getStartTime().get());
        } else {
            return Range.closedOpen(
                    customPropertySetRecord.getStartTime().get(),
                    customPropertySetRecord.getEndTime().get());
        }
    }

    private Range<Instant> getRangeToUpdate(CustomPropertySetRecord customPropertySetRecord, Range<Instant> oldRange) {
        if (!customPropertySetRecord.getStartTime().isPresent() && !customPropertySetRecord.getEndTime().isPresent()) {
            return oldRange;
        } else if (!customPropertySetRecord.getStartTime().isPresent()) {
            if (oldRange.hasUpperBound()) {
                return Range.closedOpen(customPropertySetRecord.getStartTime().get(), oldRange.upperEndpoint());
            } else if (customPropertySetRecord.getEndTime().get().equals(Instant.EPOCH)) {
                return Range.all();
            } else {
                return Range.lessThan(customPropertySetRecord.getEndTime().get());
            }
        } else if (!customPropertySetRecord.getEndTime().isPresent()) {
            if (oldRange.hasUpperBound()) {
                return Range.closedOpen(oldRange.lowerEndpoint(), customPropertySetRecord.getEndTime().get());
            } else if (customPropertySetRecord.getStartTime().get().equals(Instant.EPOCH)) {
                return Range.all();
            } else {
                return Range.atLeast(customPropertySetRecord.getStartTime().get());
            }
        } else {
            return getRangeToCreate(customPropertySetRecord);
        }
    }
}
