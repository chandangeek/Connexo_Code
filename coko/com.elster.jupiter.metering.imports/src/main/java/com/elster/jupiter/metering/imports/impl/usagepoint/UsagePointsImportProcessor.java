/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.GasDetailBuilder;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.HeatDetailBuilder;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.metering.UsagePointMeterActivator;
import com.elster.jupiter.metering.UsagePointPropertySet;
import com.elster.jupiter.metering.UsagePointVersionedPropertySet;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.metering.WaterDetailBuilder;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.imports.impl.CustomPropertySetRecord;
import com.elster.jupiter.metering.imports.impl.FileImportLogger;
import com.elster.jupiter.metering.imports.impl.MessageSeeds;
import com.elster.jupiter.metering.imports.impl.MeteringDataImporterContext;
import com.elster.jupiter.metering.imports.impl.exceptions.ProcessorException;
import com.elster.jupiter.metering.imports.impl.parsers.InstantParser;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroCheckViolation;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UsagePointsImportProcessor extends AbstractImportProcessor<UsagePointImportRecord> {

    private UsagePointImportHelper usagePointImportHelper;
    private String dateFormat;
    private String timeZone;

    UsagePointsImportProcessor(MeteringDataImporterContext context, String dateFormat, String timeZone) {
        super(context);
        usagePointImportHelper = new UsagePointImportHelper(context, getClock());
        this.dateFormat = dateFormat;
        this.timeZone = timeZone;
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
            usagePointImportHelper.setMetrologyConfigurationForUsagePoint(data, usagePoint);
            activateMeters(data, usagePoint);
            performUsagePointTransition(data, usagePoint);
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
        ServiceKind serviceKind = Arrays.stream(ServiceKind.values())
                .filter(candidate -> candidate.name().equalsIgnoreCase(serviceKindString))
                .findFirst()
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
                throw new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_NOT_FOUND, data.getLineNumber(), data.getUsagePointIdentifier()
                        .get());
            }
            UsagePoint dummyUsagePoint = serviceCategory.newUsagePoint(identifier, data.getInstallationTime()
                    .orElse(getClock().instant())).validate();
            createDetails(dummyUsagePoint, data, logger).validate();
            validateMandatoryCustomProperties(dummyUsagePoint, data);
            validateCustomPropertySetValues(dummyUsagePoint, data);
            validateMeterConfigurationAndTransition(dummyUsagePoint, data);
        }
    }

    private UsagePoint getUsagePoint(UsagePointImportRecord data, FileImportLogger logger) {
        String identifier = data.getUsagePointIdentifier()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_IDENTIFIER_INVALID, data.getLineNumber()));
        String serviceKindString = data.getServiceKind()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID, data.getLineNumber()));
        ServiceKind serviceKind = Arrays.stream(ServiceKind.values())
                .filter(candidate -> candidate.name().equalsIgnoreCase(serviceKindString))
                .findFirst()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_NO_SUCH_SERVICEKIND, data.getLineNumber(), serviceKindString));
        Optional<UsagePoint> foundUsagePoint = findUsagePointByIdentifier(identifier);
        Optional<ServiceCategory> serviceCategory = getContext().getMeteringService().getServiceCategory(serviceKind);
        if (foundUsagePoint.isPresent()) {
            UsagePoint usagePoint = foundUsagePoint.get();
            if (usagePoint.getServiceCategory().getId() != serviceCategory.get().getId()) {
                throw new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICECATEGORY_CHANGE, data.getLineNumber(), serviceKindString);
            }
            usagePoint = getContext().getMeteringService()
                    .findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion())
                    .get();
            return usagePointImportHelper.updateUsagePointForInsight(usagePoint, data);
        } else {
            return usagePointImportHelper.createUsagePointForInsight(serviceCategory.get().newUsagePoint(identifier,
                    data.getInstallationTime().orElse(getContext().getClock().instant())), data);
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
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICECATEGORY_INVALID, data.getLineNumber(), data
                        .getServiceKind()
                        .get()));

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

    private void validateMeterConfigurationAndTransition(UsagePoint usagePoint, UsagePointImportRecord data) {
        data.getMetrologyConfiguration().ifPresent(metrologyConfigurationName -> {
            usagePointImportHelper.validateMetrologyConfiguration(metrologyConfigurationName, usagePoint, data);
            usagePoint.apply((UsagePointMetrologyConfiguration) getContext().getMetrologyConfigurationService()
                    .findMetrologyConfiguration(data.getMetrologyConfiguration().get())
                    .get(), data.getMetrologyConfigurationApplyTime().get());

            validateMeterActivation(usagePoint, data);
            validateUsagePointTransitionValues(usagePoint, data);
        });
    }


    private void activateMeters(UsagePointImportRecord record, UsagePoint usagePoint) {
        UsagePointMeterActivator usagePointMeterActivator = usagePoint.linkMeters();
        record.getMeterRoles().stream().forEach(meterRole -> {
            MeterRole role = getContext().getMetrologyConfigurationService()
                    .findMeterRole(meterRole.getMeterRole())
                    .get();
            Meter meter = getContext().getMeteringService().findMeterByName(meterRole.getMeter()).get();
            usagePointMeterActivator.activate(meter, role);
            usagePointMeterActivator.complete();
        });
    }


    private void performUsagePointTransition(UsagePointImportRecord record, UsagePoint usagePoint) {
        if (record.getTransition().isPresent()) {
            PropertyValueInfoService propertyValueInfoService = getContext().getPropertyValueInfoService();
            UsagePointTransition usagePointTransition = getOptionalTransition(usagePoint.getState(), record.getTransition()
                    .get()).get();

            checkPreTransitionRequirements(usagePointTransition, usagePoint, record.getTransitionDate());

            List<PropertySpec> transitionAttributes = usagePointTransition.getMicroActionsProperties();

            Map<String, String> propertiesFromCsv = record.getTransitionAttributes();
            Map<String, Object> propertiesMap = new HashMap<>();
            for (String propertyName : propertiesFromCsv.keySet()) {
                Optional<PropertySpec> propertySpec = transitionAttributes
                        .stream()
                        .filter(spec -> spec.getDisplayName()
                                .replaceAll(" ", "")
                                .equalsIgnoreCase(propertyName))
                        .findFirst();
                if (propertySpec.isPresent()) {
                    Map<String, Object> propertyValueToSet = new HashMap<>(1);
                    //here trim(), upperCase and replaceAll() is required for searching enum values (id of propertySpec is enum value)
                    propertyValueToSet.put(propertySpec.get().getName(), propertySpec.get()
                            .getValueFactory()
                            .fromStringValue(propertiesFromCsv.get(propertyName)
                                    .toUpperCase()
                                    .trim()
                                    .replaceAll(" ", "_")));
                    List<PropertyInfo> propertyInfoList = propertyValueInfoService.getPropertyInfos(transitionAttributes, propertyValueToSet);
                    propertiesMap.put(propertySpec.get()
                            .getName(), propertyValueInfoService.findPropertyValue(propertySpec.get(), propertyInfoList));
                }
            }

            getContext().getUsagePointLifeCycleService()
                    .scheduleTransition(usagePoint, usagePointTransition, record.getTransitionDate(), "INS", propertiesMap);
        }
    }

    private Optional<UsagePointTransition> getOptionalTransition(UsagePointState usagePointState, String transitionName) {
        return getContext().getUsagePointLifeCycleService()
                .getAvailableTransitions(usagePointState, "INS")
                .stream()
                .filter(usagePointTransition -> usagePointTransition.getName().equals(transitionName))
                .findAny();
    }


    private void checkPreTransitionRequirements(UsagePointTransition usagePointTransition, UsagePoint usagePoint, Instant transitionDate) {
        if (transitionDate == null) {
            throw new ProcessorException(MessageSeeds.TRANSITION_DATE_IS_NOT_SPECIFIED);
        } else if (transitionDate.isBefore(getClock().instant())) {
            throw new ProcessorException(MessageSeeds.ACTIVATION_DATE_OF_TRANSITION_IS_BEFORE_UP_CREATION);
        }
        List<ExecutableMicroCheckViolation> violations = usagePointTransition.getChecks().stream()
                .filter(check -> check instanceof ExecutableMicroCheck)
                .map(ExecutableMicroCheck.class::cast)
                .map(check -> check.execute(usagePoint, transitionDate))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        if (!violations.isEmpty()) {
            throw new ProcessorException(MessageSeeds.PRE_TRANSITION_CHECK_FAILED, violations.get(0)
                    .getLocalizedMessage());
        }
    }

    private void validateUsagePointTransitionValues(UsagePoint usagePoint, UsagePointImportRecord data) {
        if (data.getTransition().isPresent()) {
            Optional<UsagePointTransition> optionalTransition = getOptionalTransition(usagePoint.getState(), data.getTransition()
                    .get());

            if (!optionalTransition.isPresent()) {
                throw new ProcessorException(MessageSeeds.NO_SUCH_TRANSITION_FOUND, data.getTransition().get());
            }
        }
    }

    private void validateMandatoryCustomProperties(UsagePoint usagePoint, UsagePointImportRecord data) {
        Map<String, CustomPropertySetRecord> customPropertySetValues = data.getRegisteredCustomPropertySets();

        for (UsagePointPropertySet propertySet : usagePoint.forCustomProperties().getAllPropertySets()) {
            for (PropertySpec propertySpec : propertySet.getCustomPropertySet().getPropertySpecs()) {
                if (propertySpec.isRequired()) {
                    Optional.ofNullable(customPropertySetValues.get(propertySet.getCustomPropertySet().getId()))
                            .filter(customPropertySetRecord -> customPropertySetRecord.getCustomPropertySetValues()
                                    .getProperty(propertySpec.getName()) != null)
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
        Map<String, CustomPropertySetRecord> customPropertySetValues = data.getRegisteredCustomPropertySets();

        for (UsagePointPropertySet propertySet : usagePoint.forCustomProperties().getAllPropertySets()) {
            if (customPropertySetValues.containsKey(propertySet.getCustomPropertySet().getId())) {
                if (propertySet instanceof UsagePointVersionedPropertySet) {
                    validateCreateOrUpdateVersionedSet((UsagePointVersionedPropertySet) propertySet, customPropertySetValues
                            .get(propertySet.getCustomPropertySet().getId()));
                } else {
                    validateCreateOrUpdateNonVersionedSet(propertySet, customPropertySetValues.get(propertySet.getCustomPropertySet()
                            .getId()));
                }
            }
        }
    }

    private void validateCreateOrUpdateVersionedSet(UsagePointVersionedPropertySet usagePointCustomPropertySet, CustomPropertySetRecord customPropertySetRecord) {
        if (customPropertySetRecord.getVersionId().isPresent()) {
            CustomPropertySetValues values = usagePointCustomPropertySet.getVersionValues(customPropertySetRecord.getVersionId()
                    .get());
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

    private void validateMeterActivation(UsagePoint usagePoint, UsagePointImportRecord data) {
        if (!data.getMeterRoles().isEmpty()) {
            UsagePointMeterActivator linker = usagePoint.linkMeters();
            data.getMeterRoles().forEach(role -> {
                checkEmptyFieldValues(role);
                checkMeterRolesActivationTime(role);
                Meter meter = findMeterOrThrowException(role.getMeter());
                MeterRole meterRole = findMeterRoleOrThrowException(role.getMeterRole());
                linker.activate(getActivationDate(role.getActivation()), meter, meterRole);
            });
        }
    }

    private Meter findMeterOrThrowException(String meterName) {
        return getContext().getMeteringService().findMeterByName(meterName)
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_SUCH_METER_WITH_NAME, meterName));
    }

    private MeterRole findMeterRoleOrThrowException(String meterRoleKey) {
        return getContext().getMetrologyConfigurationService().findMeterRole(meterRoleKey)
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_SUCH_METER_ROLE_WITH_KEY, meterRoleKey));
    }

    private Instant getActivationDate(String activationDate) {
        return new InstantParser(dateFormat, timeZone).parse(activationDate);
    }

    private void checkMeterRolesActivationTime(MeterRoleWithMeterAndActivationDate meterRole) {
        if (getActivationDate(meterRole.getActivation()).isBefore(getClock().instant())) {
            throw new ProcessorException(MessageSeeds.ACTIVATION_DATE_OF_METER_ROLE_IS_BEFORE_UP_CREATION, meterRole.getActivation());
        }
    }

    private void checkEmptyFieldValues(MeterRoleWithMeterAndActivationDate meterRoleWithMeterAndActivationDate) {
        if (meterRoleWithMeterAndActivationDate.getActivation().isEmpty() ||
                meterRoleWithMeterAndActivationDate.getMeter().isEmpty() ||
                meterRoleWithMeterAndActivationDate.getMeterRole().isEmpty()) {
            throw new ProcessorException(MessageSeeds.SOME_REQUIRED_FIELDS_ARE_EMPTY);
        }
    }

}
