package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.GasDetailBuilder;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.HeatDetailBuilder;
import com.elster.jupiter.metering.LocationBuilder;
import com.elster.jupiter.metering.LocationTemplate;
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
import com.elster.jupiter.metering.imports.impl.FileImportProcessor;
import com.elster.jupiter.metering.imports.impl.MessageSeeds;
import com.elster.jupiter.metering.imports.impl.MeteringDataImporterContext;
import com.elster.jupiter.metering.imports.impl.exceptions.ProcessorException;
import com.elster.jupiter.util.geo.SpatialCoordinatesFactory;

import com.google.common.collect.Range;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class UsagePointsImportProcessor implements FileImportProcessor<UsagePointImportRecord> {

    private final MeteringDataImporterContext context;


    UsagePointsImportProcessor(MeteringDataImporterContext context) {
        this.context = context;
    }

    @Override
    public void process(UsagePointImportRecord data, FileImportLogger logger) throws ProcessorException {
        try {
            validate(data, logger);

            UsagePoint usagePoint = getUsagePoint(data, logger);

            if (usagePoint.getDetail(context.getClock().instant()).isPresent()) {
                updateDetails(usagePoint, data, logger).create();
            } else {
                createDetails(usagePoint, data, logger).create();
            }

            addCustomPropertySetValues(usagePoint, data, logger);

        } catch (ConstraintViolationException e) {
            for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
                logger.warning(MessageSeeds.IMPORT_USAGEPOINT_CONSTRAINT_VOLATION, data.getLineNumber(), violation.getPropertyPath(), violation
                        .getMessage());
            }
            throw new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_INVALIDDATA, data.getLineNumber());
        }
    }

    @Override
    public void complete(FileImportLogger logger) {
    }

    private void validate(UsagePointImportRecord data, FileImportLogger logger) throws ProcessorException {
        String mRID = data.getmRID()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_MRID_INVALID, data.getLineNumber()));
        String serviceKindString = data.getServiceKind()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_NO_SUCH_SERVICEKIND, data.getLineNumber()));
        ServiceKind serviceKind =  Arrays.stream(ServiceKind.values()).filter(candidate -> candidate.name().equalsIgnoreCase(serviceKindString)).findFirst()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID, data.getLineNumber(), serviceKindString));
        ServiceCategory serviceCategory = context.getMeteringService().getServiceCategory(serviceKind)
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICECATEGORY_INVALID, data.getLineNumber(), serviceKindString));

        Optional<UsagePoint> usagePointOptional = context.getMeteringService().findUsagePoint(mRID);
        if (usagePointOptional.isPresent()) {
            if (data.isAllowUpdate()) {
                updateDetails(usagePointOptional.get(), data, logger).validate();
                validateCustomPropertySetValues(usagePointOptional.get(), data, logger);
            } else {
                throw new ProcessorException(MessageSeeds.UPDATE_NOT_ALLOWED, data.getLineNumber());
            }
        } else {
            if (data.isAllowUpdate()) {
                throw new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_NOT_FOUND, data.getLineNumber(), data.getmRID().get());
            }
            UsagePoint dummyUsagePoint = serviceCategory.newUsagePoint(mRID, data.getInstallationTime()
                    .orElse(context.getClock().instant())).validate();
            createDetails(dummyUsagePoint, data, logger).validate();
            validateCustomPropertySetValues(dummyUsagePoint, data, logger);
        }
    }

    private UsagePoint getUsagePoint(UsagePointImportRecord data, FileImportLogger logger) {
        UsagePoint usagePoint;
        String mRID = data.getmRID()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_MRID_INVALID, data.getLineNumber()));
        String serviceKindString = data.getServiceKind()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_NO_SUCH_SERVICEKIND, data.getLineNumber()));
        ServiceKind serviceKind =  Arrays.stream(ServiceKind.values()).filter(candidate -> candidate.name().equalsIgnoreCase(serviceKindString)).findFirst()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID, data.getLineNumber(), serviceKindString));
        Optional<UsagePoint> usagePointOptional = context.getMeteringService().findUsagePoint(mRID);
        Optional<ServiceCategory> serviceCategory = context.getMeteringService().getServiceCategory(serviceKind);

        if (usagePointOptional.isPresent()) {
            usagePoint = usagePointOptional.get();
            if (usagePoint.getServiceCategory().getId() != serviceCategory.get().getId()) {
                throw new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICECATEGORY_CHANGE, data.getLineNumber(), serviceKindString);
            }
            return updateUsagePoint(usagePoint, data, logger);
        } else {
            return createUsagePoint(serviceCategory.get()
                    .newUsagePoint(mRID, data.getInstallationTime()
                            .orElse(context.getClock().instant())), data, logger);
        }
    }

    private UsagePointDetailBuilder createDetails(UsagePoint usagePoint, UsagePointImportRecord data, FileImportLogger logger) {
        switch (usagePoint.getServiceCategory().getKind()) {
            case ELECTRICITY:
                return buildElectricityDetails(usagePoint.newElectricityDetailBuilder(context.getClock().instant()),
                        (ElectricityDetail) usagePoint.getServiceCategory()
                                .newUsagePointDetail(usagePoint, context.getClock().instant()), data, logger);
            case GAS:
                return buildGasDetails(usagePoint.newGasDetailBuilder(context.getClock().instant()),
                        (GasDetail) usagePoint.getServiceCategory()
                                .newUsagePointDetail(usagePoint, context.getClock().instant()), data, logger);
            case WATER:
                return buildWaterDetails(usagePoint.newWaterDetailBuilder(context.getClock().instant()),
                        (WaterDetail) usagePoint.getServiceCategory()
                                .newUsagePointDetail(usagePoint, context.getClock().instant()), data, logger);
            case HEAT:
                return buildHeatDetails(usagePoint.newHeatDetailBuilder(context.getClock().instant()),
                        (HeatDetail) usagePoint.getServiceCategory()
                                .newUsagePointDetail(usagePoint, context.getClock().instant()), data, logger);
            default:
                return addBaseDetails(usagePoint.newDefaultDetailBuilder(context.getClock().instant()),
                        usagePoint.getServiceCategory()
                                .newUsagePointDetail(usagePoint, context.getClock().instant()), data, logger);
        }
    }

    private UsagePointDetailBuilder updateDetails(UsagePoint usagePoint, UsagePointImportRecord data, FileImportLogger logger) {
        UsagePointDetail detail = usagePoint.getDetail(context.getClock().instant())
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICECATEGORY_INVALID, data.getLineNumber(), data.getServiceKind().get()));

        switch (usagePoint.getServiceCategory().getKind()) {
            case ELECTRICITY:
                return buildElectricityDetails(usagePoint.newElectricityDetailBuilder(context.getClock().instant()),
                        (ElectricityDetail) detail, data, logger);
            case GAS:
                return buildGasDetails(usagePoint.newGasDetailBuilder(context.getClock().instant()),
                        (GasDetail) detail, data, logger);
            case WATER:
                return buildWaterDetails(usagePoint.newWaterDetailBuilder(context.getClock().instant()),
                        (WaterDetail) detail, data, logger);
            case HEAT:
                return buildHeatDetails(usagePoint.newHeatDetailBuilder(context.getClock().instant()),
                        (HeatDetail) detail, data, logger);
            default:
                return addBaseDetails(usagePoint.newDefaultDetailBuilder(context.getClock().instant()),
                        detail, data, logger);
        }
    }

    private UsagePoint createUsagePoint(UsagePointBuilder usagePointBuilder, UsagePointImportRecord data, FileImportLogger logger) {
        usagePointBuilder.withIsSdp(false);
        boolean isVirtual = true;
        List<String> locationData = data.getLocation();
        List<String> geoCoordinatesData = data.getGeoCoordinates();

        if(locationData.stream().anyMatch(s -> s != null)) {
            context.getMeteringService()
                    .getLocationTemplate()
                    .getTemplateMembers()
                    .stream()
                    .filter(LocationTemplate.TemplateField::isMandatory)
                    .forEach(field -> {
                        if(locationData.get(field.getRanking()) == null){
                            throw new ProcessorException(MessageSeeds.LINE_MISSING_LOCATION_VALUE, data.getLineNumber(), field.getName());
                        }else if(locationData.get(field.getRanking()).equals("")){
                            throw new ProcessorException(MessageSeeds.LINE_MISSING_LOCATION_VALUE, data.getLineNumber(), field.getName());
                        }
                    });
            LocationBuilder builder = usagePointBuilder.newLocationBuilder();
            Map<String, Integer> ranking = context.getMeteringService().getLocationTemplate().getTemplateMembers().stream()
                    .collect(Collectors.toMap(LocationTemplate.TemplateField::getName, LocationTemplate.TemplateField::getRanking));

            Optional<LocationBuilder.LocationMemberBuilder> memberBuilder = builder.getMemberBuilder(locationData
                    .get(ranking.get("locale")));
            if (memberBuilder.isPresent()) {
                setLocationAttributes(memberBuilder.get(), data, ranking);
            } else {
                setLocationAttributes(builder.member(), data, ranking).add();
            }
            usagePointBuilder.withLocation(builder.create());
            isVirtual = false;
        }
        if(geoCoordinatesData != null && geoCoordinatesData.size() > 1) {
            usagePointBuilder.withGeoCoordinates(new SpatialCoordinatesFactory().fromStringValue((geoCoordinatesData.stream().collect(Collectors.joining(":")))));
            isVirtual = false;
        }
        usagePointBuilder.withIsVirtual(isVirtual);
        usagePointBuilder.withName(data.getName().orElse(null));
        usagePointBuilder.withOutageRegion(data.getOutageRegion().orElse(null));
        usagePointBuilder.withReadRoute(data.getReadRoute().orElse(null));
        usagePointBuilder.withServicePriority(data.getServicePriority().orElse(null));
        usagePointBuilder.withServiceDeliveryRemark(data.getServiceDeliveryRemark().orElse(null));
        return usagePointBuilder.create();
    }

    private UsagePoint updateUsagePoint(UsagePoint usagePoint, UsagePointImportRecord data, FileImportLogger logger) {
        List<String> locationData = data.getLocation();
        List<String> geoCoordinatesData = data.getGeoCoordinates();

        if(locationData.stream().anyMatch(s -> s != null)) {
            context.getMeteringService()
                    .getLocationTemplate()
                    .getTemplateMembers()
                    .stream()
                    .filter(LocationTemplate.TemplateField::isMandatory)
                    .forEach(field -> {
                        if(locationData.get(field.getRanking()) == null){
                            throw new ProcessorException(MessageSeeds.LINE_MISSING_LOCATION_VALUE, data.getLineNumber(), field.getName());
                        }else if(locationData.get(field.getRanking()).equals("")){
                            throw new ProcessorException(MessageSeeds.LINE_MISSING_LOCATION_VALUE, data.getLineNumber(), field.getName());
                        }
                    });
            LocationBuilder builder = usagePoint.updateLocation();
            Map<String, Integer> ranking = context.getMeteringService().getLocationTemplate().getTemplateMembers().stream()
                    .collect(Collectors.toMap(LocationTemplate.TemplateField::getName, LocationTemplate.TemplateField::getRanking));

            Optional<LocationBuilder.LocationMemberBuilder> memberBuilder = builder.getMemberBuilder(locationData
                    .get(ranking.get("locale")));
            if (memberBuilder.isPresent()) {
                setLocationAttributes(memberBuilder.get(), data, ranking);
            } else {
                setLocationAttributes(builder.member(), data, ranking).add();
            }
            usagePoint.setLocation(builder.create().getId());
        }
        if(geoCoordinatesData != null && geoCoordinatesData.stream().allMatch(s -> s != null)) {
            usagePoint.setSpatialCoordinates(new SpatialCoordinatesFactory().fromStringValue(geoCoordinatesData.stream().reduce((s, t) -> s + ":" + t).get()));
        }
        usagePoint.setName(data.getName().orElse(null));
        usagePoint.setOutageRegion(data.getOutageRegion().orElse(null));
        usagePoint.setReadRoute(data.getReadRoute().orElse(null));
        usagePoint.setServicePriority(data.getServicePriority().orElse(null));
        usagePoint.setServiceDeliveryRemark(data.getServiceDeliveryRemark().orElse(null));
        usagePoint.update();
        return usagePoint;
    }

    private ElectricityDetailBuilder buildElectricityDetails(ElectricityDetailBuilder detailBuilder, ElectricityDetail oldDetail, UsagePointImportRecord data, FileImportLogger logger) {
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

    private GasDetailBuilder buildGasDetails(GasDetailBuilder detailBuilder, GasDetail oldDetail, UsagePointImportRecord data, FileImportLogger logger) {
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

    private WaterDetailBuilder buildWaterDetails(WaterDetailBuilder detailBuilder, WaterDetail oldDetail, UsagePointImportRecord data, FileImportLogger logger) {
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

    private HeatDetailBuilder buildHeatDetails(HeatDetailBuilder detailBuilder, HeatDetail oldDetail, UsagePointImportRecord data, FileImportLogger logger) {
        detailBuilder.withCollar(data.isCollarInstalled().orElse(oldDetail.isCollarInstalled()));
        detailBuilder.withPressure(data.getPressure().orElse(oldDetail.getPressure()));
        detailBuilder.withPhysicalCapacity(data.getPhysicalCapacity().orElse(oldDetail.getPhysicalCapacity()));
        detailBuilder.withBypass(data.isBypassInstalled().orElse(oldDetail.isBypassInstalled()));
        detailBuilder.withBypassStatus(data.getBypassStatus().orElse(oldDetail.getBypassStatus()));
        detailBuilder.withValve(data.isValveInstalled().orElse(oldDetail.isValveInstalled()));
        return detailBuilder;
    }

    private UsagePointDetailBuilder addBaseDetails(UsagePointDetailBuilder detailBuilder, UsagePointDetail oldDetail, UsagePointImportRecord data, FileImportLogger logger) {
        detailBuilder.withCollar(data.isCollarInstalled().orElse(oldDetail.isCollarInstalled()));
        return detailBuilder;
    }

    public void addCustomPropertySetValues(UsagePoint usagePoint, UsagePointImportRecord data, FileImportLogger logger) {
        Map<CustomPropertySet, CustomPropertySetRecord> customPropertySetValues = data.getCustomPropertySets();

        for (UsagePointPropertySet propertySet : usagePoint.forCustomProperties().getAllPropertySets()) {
            if (customPropertySetValues.containsKey(propertySet.getCustomPropertySet())) {
                if (propertySet instanceof UsagePointVersionedPropertySet) {
                    createOrUpdateVersionedSet((UsagePointVersionedPropertySet) propertySet, customPropertySetValues.get(propertySet
                            .getCustomPropertySet()));
                } else {
                    createOrUpdateNonVersionedSet(propertySet, customPropertySetValues.get(propertySet.getCustomPropertySet()));
                }
            }
        }
    }

    public void validateCustomPropertySetValues(UsagePoint usagePoint, UsagePointImportRecord data, FileImportLogger logger) {
        Map<CustomPropertySet, CustomPropertySetRecord> customPropertySetValues = data.getCustomPropertySets();

        for (UsagePointPropertySet propertySet : usagePoint.forCustomProperties().getAllPropertySets()) {
            if (customPropertySetValues.containsKey(propertySet.getCustomPropertySet())) {
                if (propertySet instanceof UsagePointVersionedPropertySet) {
                    validateCreateOrUpdateVersionedSet((UsagePointVersionedPropertySet) propertySet, customPropertySetValues
                            .get(propertySet.getCustomPropertySet()));
                } else {
                    validateCreateOrUpdateNonVersionedSet(propertySet, customPropertySetValues.get(propertySet.getCustomPropertySet()));
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
                context.getCustomPropertySetService()
                        .validateCustomPropertySetValues(usagePointCustomPropertySet.getCustomPropertySet(), values);
            } else {
                throw new ProcessorException(MessageSeeds.IMPORT_VERSIONED_VALUES_NOT_FOUND, customPropertySetRecord.getLineNumber(), customPropertySetRecord
                        .getVersionId().get()
                        .toString());
            }
        } else {
            context.getCustomPropertySetService()
                    .validateCustomPropertySetValues(usagePointCustomPropertySet.getCustomPropertySet(),
                            customPropertySetRecord.getCustomPropertySetValues());
        }
    }

    private void createOrUpdateVersionedSet(UsagePointVersionedPropertySet usagePointCustomPropertySet, CustomPropertySetRecord customPropertySetRecord) {
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
                context.getCustomPropertySetService()
                        .setValuesVersionFor(usagePointCustomPropertySet.getCustomPropertySet(),
                                usagePointCustomPropertySet.getUsagePoint(),
                                values,
                                getRangeToUpdate(customPropertySetRecord, values.getEffectiveRange()), customPropertySetRecord
                                        .getVersionId()
                                        .get());
            } else {
                throw new ProcessorException(MessageSeeds.IMPORT_VERSIONED_VALUES_NOT_FOUND, customPropertySetRecord.getLineNumber(), customPropertySetRecord
                        .getVersionId().get()
                        .toString());
            }
        } else {
            context.getCustomPropertySetService()
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
            context.getCustomPropertySetService()
                    .validateCustomPropertySetValues(usagePointCustomPropertySet.getCustomPropertySet(), values);
        }
        context.getCustomPropertySetService()
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
            context.getCustomPropertySetService()
                    .setValuesFor(usagePointCustomPropertySet.getCustomPropertySet(), usagePointCustomPropertySet.getUsagePoint(),
                            values);
        }

        usagePointCustomPropertySet.setValues(values);

    }

    private Range<Instant> getRangeToCreate(CustomPropertySetRecord customPropertySetRecord) {
        if ((!customPropertySetRecord.getStartTime().isPresent() || customPropertySetRecord.getStartTime()
                .get()
                .equals(Instant.EPOCH))
                && (!customPropertySetRecord.getEndTime().isPresent() || customPropertySetRecord.getEndTime()
                .get()
                .equals(Instant.EPOCH))) {
            return Range.all();
        } else if (!customPropertySetRecord.getStartTime().isPresent() || customPropertySetRecord.getStartTime()
                .get()
                .equals(Instant.EPOCH)) {
            return Range.lessThan(customPropertySetRecord.getEndTime().get());
        } else if (!customPropertySetRecord.getEndTime().isPresent() || customPropertySetRecord.getEndTime()
                .get()
                .equals(Instant.EPOCH)) {
            return Range.atLeast(customPropertySetRecord.getStartTime().get());
        } else {
            return Range.closedOpen(customPropertySetRecord.getStartTime().get(), customPropertySetRecord.getEndTime()
                    .get());
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

    private LocationBuilder.LocationMemberBuilder setLocationAttributes(LocationBuilder.LocationMemberBuilder builder, UsagePointImportRecord data, Map<String, Integer> ranking) {
        builder.setCountryCode(data.getLocation().get(ranking.get("countryCode")))
                .setCountryName(data.getLocation().get(ranking.get("countryName")))
                .setAdministrativeArea(data.getLocation().get(ranking.get("administrativeArea")))
                .setLocality(data.getLocation().get(ranking.get("locality")))
                .setSubLocality(data.getLocation().get(ranking.get("subLocality")))
                .setStreetType(data.getLocation().get(ranking.get("streetType")))
                .setStreetName(data.getLocation().get(ranking.get("streetName")))
                .setStreetNumber(data.getLocation().get(ranking.get("streetNumber")))
                .setEstablishmentType(data.getLocation().get(ranking.get("establishmentType")))
                .setEstablishmentName(data.getLocation().get(ranking.get("establishmentName")))
                .setEstablishmentNumber(data.getLocation().get(ranking.get("establishmentNumber")))
                .setAddressDetail(data.getLocation().get(ranking.get("addressDetail")))
                .setZipCode(data.getLocation().get(ranking.get("zipCode")))
                .isDaultLocation(true)
                .setLocale(data.getLocation().get(ranking.get("locale")));
        return builder;
    }

    private boolean validateGeoCoordinatesData(List<String> geoCoordinatesData){
        int count = 0;
        for(String geoElement : geoCoordinatesData){
            if (geoElement == null){
                count++;
            }
        }
        return count < 2;
    }

}
