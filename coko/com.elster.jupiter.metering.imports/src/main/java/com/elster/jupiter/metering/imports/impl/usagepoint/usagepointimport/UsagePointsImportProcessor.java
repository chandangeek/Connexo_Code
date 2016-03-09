package com.elster.jupiter.metering.imports.impl.usagepoint.usagepointimport;

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
import com.elster.jupiter.metering.imports.impl.MessageSeeds;
import com.elster.jupiter.metering.imports.impl.usagepoint.CustomPropertySetRecord;
import com.elster.jupiter.metering.imports.impl.usagepoint.FileImportLogger;
import com.elster.jupiter.metering.imports.impl.usagepoint.FileImportProcessor;
import com.elster.jupiter.metering.imports.impl.usagepoint.MeteringDataImporterContext;
import com.elster.jupiter.metering.imports.impl.usagepoint.exceptions.ProcessorException;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;


public class UsagePointsImportProcessor implements FileImportProcessor<UsagePointImportRecord> {

    private final MeteringDataImporterContext context;


    UsagePointsImportProcessor(MeteringDataImporterContext context) {
        this.context = context;
    }

    @Override
    public void process(UsagePointImportRecord data, FileImportLogger logger) throws ProcessorException {

        validate(data, logger);

        UsagePoint usagePoint = getUsagePoint(data, logger);

        if (usagePoint.getDetail(context.getClock().instant()).isPresent()) {
            updateDetails(usagePoint, data, logger).create();
        } else {
            createDetails(usagePoint, data, logger).create();
        }

        addCustomPropertySetValues(usagePoint, data, logger);
    }

    private void validate(UsagePointImportRecord data, FileImportLogger logger) throws ProcessorException{
        String mRID = data.getmRID().orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_MRID_INVALID, data.getLineNumber()));
        ServiceKind serviceKind = ServiceKind.valueOf(data.getServiceKind()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID, data.getLineNumber())));
        ServiceCategory serviceCategory = context.getMeteringService().getServiceCategory(serviceKind)
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID, data.getLineNumber()));

        Optional<UsagePoint> usagePointOptional = context.getMeteringService().findUsagePoint(mRID);
        if (usagePointOptional.isPresent()){
            updateDetails(usagePointOptional.get(),data,logger).validate();
            validateCustomPropertySetValues(usagePointOptional.get(),data,logger);
        } else {
            UsagePoint dummyUsagePoint = serviceCategory.newUsagePoint("dummyUsagePointForValidation" + data.hashCode(), context
                    .getClock()
                    .instant()).validate();
            createDetails(dummyUsagePoint,data,logger).validate();
            validateCustomPropertySetValues(dummyUsagePoint,data,logger);
        }
    }

    private UsagePoint getUsagePoint(UsagePointImportRecord data, FileImportLogger logger){
        UsagePoint usagePoint;
        String mRID = data.getmRID().orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_MRID_INVALID, data.getLineNumber()));
        ServiceKind serviceKind = ServiceKind.valueOf(data.getServiceKind()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID, data.getLineNumber())));
        Optional<UsagePoint> usagePointOptional = context.getMeteringService().findUsagePoint(mRID);
        Optional<ServiceCategory> serviceCategory = context.getMeteringService().getServiceCategory(serviceKind);

        if (usagePointOptional.isPresent()) {
            usagePoint = usagePointOptional.get();
            if (usagePoint.getServiceCategory().getId() != serviceCategory.get().getId()) {
                throw new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID, data.getLineNumber());
            }
            return updateUsagePoint(usagePoint,data,logger);
        } else {
            return createUsagePoint(serviceCategory.get()
                    .newUsagePoint(mRID, data.getInstallationTime()
                            .orElse(context.getClock().instant())), data, logger);
        }
    }

    private UsagePointDetailBuilder createDetails(UsagePoint usagePoint, UsagePointImportRecord data, FileImportLogger logger){
            switch (usagePoint.getServiceCategory().getKind()){
                case ELECTRICITY:
                    return buildElectricityDetails(usagePoint.newElectricityDetailBuilder(context.getClock().instant()),
                            (ElectricityDetail) usagePoint.getServiceCategory().newUsagePointDetail(usagePoint,context.getClock().instant()),data,logger);
                case GAS:
                    return buildGasDetails(usagePoint.newGasDetailBuilder(context.getClock().instant()),
                            (GasDetail) usagePoint.getServiceCategory().newUsagePointDetail(usagePoint,context.getClock().instant()),data,logger);
                case WATER:
                    return buildWaterDetails(usagePoint.newWaterDetailBuilder(context.getClock().instant()),
                            (WaterDetail) usagePoint.getServiceCategory().newUsagePointDetail(usagePoint,context.getClock().instant()),data,logger);
                case HEAT:
                    return buildHeatDetails(usagePoint.newHeatDetailBuilder(context.getClock().instant()),
                            (HeatDetail) usagePoint.getServiceCategory().newUsagePointDetail(usagePoint,context.getClock().instant()),data,logger);
                default:
                    return addBaseDetails(usagePoint.newDefaultDetailBuilder(context.getClock().instant()),
                            usagePoint.getServiceCategory().newUsagePointDetail(usagePoint,context.getClock().instant()),data,logger);
            }
    }

    private UsagePointDetailBuilder updateDetails(UsagePoint usagePoint, UsagePointImportRecord data, FileImportLogger logger){
        UsagePointDetail detail = usagePoint.getDetail(context.getClock().instant())
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID, data.getLineNumber()));

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

    private UsagePoint createUsagePoint(UsagePointBuilder usagePointBuilder,UsagePointImportRecord data, FileImportLogger logger){
        usagePointBuilder.withIsSdp(false);
        if (!data.getServiceLocationString().isPresent()) {
            usagePointBuilder.withIsVirtual(true);
            logger.warning(MessageSeeds.IMPORT_USAGEPOINT_SERVICELOCATION_INVALID, data.getLineNumber());
        }else {
            usagePointBuilder.withIsVirtual(false);
            usagePointBuilder.withServiceLocationString(data.getServiceLocationString().get());
        }
        usagePointBuilder.withOutageRegion(data.getOutageRegion().orElse(null));
        usagePointBuilder.withReadRoute(data.getReadRoute().orElse(null));
        usagePointBuilder.withServicePriority(data.getServicePriority().orElse(null));
        usagePointBuilder.withServiceDeliveryRemark(data.getServiceDeliveryRemark().orElse(null));

        return usagePointBuilder.create();
    }

    private UsagePoint updateUsagePoint(UsagePoint usagePoint,UsagePointImportRecord data, FileImportLogger logger){
        if (data.getServiceLocationString().isPresent()) {
            usagePoint.setVirtual(false);
            usagePoint.setServiceLocationString(data.getServiceLocationString().get());
        }
        usagePoint.setOutageRegion(data.getOutageRegion().orElse(null));
        usagePoint.setReadRoute(data.getReadRoute().orElse(null));
        usagePoint.setServicePriority(data.getServicePriority().orElse(null));
        usagePoint.setServiceDeliveryRemark(data.getServiceDeliveryRemark().orElse(null));

        usagePoint.update();

        return usagePoint;
    }

    private ElectricityDetailBuilder buildElectricityDetails(ElectricityDetailBuilder detailBuilder, ElectricityDetail oldDetail, UsagePointImportRecord data, FileImportLogger logger){
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

    private GasDetailBuilder buildGasDetails(GasDetailBuilder detailBuilder, GasDetail oldDetail, UsagePointImportRecord data, FileImportLogger logger){
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

    private WaterDetailBuilder buildWaterDetails(WaterDetailBuilder detailBuilder, WaterDetail oldDetail, UsagePointImportRecord data, FileImportLogger logger){
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

    private HeatDetailBuilder buildHeatDetails(HeatDetailBuilder detailBuilder, HeatDetail oldDetail, UsagePointImportRecord data, FileImportLogger logger){
        detailBuilder.withCollar(data.isCollarInstalled().orElse(oldDetail.isCollarInstalled()));
        detailBuilder.withPressure(data.getPressure().orElse(oldDetail.getPressure()));
        detailBuilder.withPhysicalCapacity(data.getPhysicalCapacity().orElse(oldDetail.getPhysicalCapacity()));
        detailBuilder.withBypass(data.isBypassInstalled().orElse(oldDetail.isBypassInstalled()));
        detailBuilder.withBypassStatus(data.getBypassStatus().orElse(oldDetail.getBypassStatus()));
        detailBuilder.withValve(data.isValveInstalled().orElse(oldDetail.isValveInstalled()));
        return detailBuilder;
    }

    private UsagePointDetailBuilder addBaseDetails(UsagePointDetailBuilder detailBuilder, UsagePointDetail oldDetail, UsagePointImportRecord data, FileImportLogger logger){
        detailBuilder.withCollar(data.isCollarInstalled().orElse(oldDetail.isCollarInstalled()));
        return detailBuilder;
    }

    @Override
    public void complete(FileImportLogger logger) {
        System.err.println("USAGE POINT CREATED");
    }


    public void addCustomPropertySetValues(UsagePoint usagePoint, UsagePointImportRecord data, FileImportLogger logger){
        Map<CustomPropertySet, CustomPropertySetRecord> customPropertySetValues = data.getCustomPropertySets();

        for (UsagePointPropertySet propertySet : usagePoint.forCustomProperties().getAllPropertySets()) {
            if (customPropertySetValues.containsKey(propertySet.getCustomPropertySet())){
                if(propertySet instanceof UsagePointVersionedPropertySet){
                    createOrUpdateVersionedSet((UsagePointVersionedPropertySet) propertySet, customPropertySetValues.get(propertySet.getCustomPropertySet()));
                } else {
                    createOrUpdateNonVersionedSet(propertySet, customPropertySetValues.get(propertySet.getCustomPropertySet()));
                }
            }
        }
    }

    public void validateCustomPropertySetValues(UsagePoint usagePoint, UsagePointImportRecord data, FileImportLogger logger){
        Map<CustomPropertySet, CustomPropertySetRecord> customPropertySetValues = data.getCustomPropertySets();

        for (UsagePointPropertySet propertySet : usagePoint.forCustomProperties().getAllPropertySets()) {
            if (customPropertySetValues.containsKey(propertySet.getCustomPropertySet())){
                if(propertySet instanceof UsagePointVersionedPropertySet){
                    validateCreateOrUpdateVersionedSet((UsagePointVersionedPropertySet) propertySet, customPropertySetValues.get(propertySet.getCustomPropertySet()));
                } else {
                    validateCreateOrUpdateNonVersionedSet(propertySet, customPropertySetValues.get(propertySet.getCustomPropertySet()));
                }
            }
        }
    }

    private void validateCreateOrUpdateVersionedSet(UsagePointVersionedPropertySet usagePointCustomPropertySet, CustomPropertySetRecord customPropertySetRecord){
        if (customPropertySetRecord.getVersionId().isPresent()){
            CustomPropertySetValues values = usagePointCustomPropertySet.getVersionValues(customPropertySetRecord.getVersionId().get());
            if (values!=null){
                usagePointCustomPropertySet.getCustomPropertySet()
                        .getPropertySpecs()
                        .stream()
                        .filter(spec -> customPropertySetRecord.getCustomPropertySetValues()
                                .getProperty(spec.getName()) != null)
                        .forEach(spec -> {values.setProperty(spec.getName(),
                                customPropertySetRecord.getCustomPropertySetValues().getProperty(spec.getName()));});
                context.getCustomPropertySetService().validateCustomPropertySetValues(usagePointCustomPropertySet.getCustomPropertySet(), values);
            } else {
                throw new ProcessorException(MessageSeeds.IMPORT_VERSIONED_VALUES_NOT_FOUND, customPropertySetRecord.getLineNumber());
            }
        } else {
            context.getCustomPropertySetService().validateCustomPropertySetValues(usagePointCustomPropertySet.getCustomPropertySet(),
                    customPropertySetRecord.getCustomPropertySetValues());
        }
    }

    private void createOrUpdateVersionedSet(UsagePointVersionedPropertySet usagePointCustomPropertySet, CustomPropertySetRecord customPropertySetRecord){
        if (customPropertySetRecord.getVersionId().isPresent()){
            CustomPropertySetValues values = usagePointCustomPropertySet.getVersionValues(customPropertySetRecord.getVersionId().get());
            if (values!=null){
                usagePointCustomPropertySet.getCustomPropertySet()
                        .getPropertySpecs()
                        .stream()
                        .filter(spec -> customPropertySetRecord.getCustomPropertySetValues()
                                .getProperty(spec.getName()) != null)
                        .forEach(spec -> {values.setProperty(spec.getName(),
                                customPropertySetRecord.getCustomPropertySetValues().getProperty(spec.getName()));});
                context.getCustomPropertySetService().setValuesVersionFor(usagePointCustomPropertySet.getCustomPropertySet(),
                        usagePointCustomPropertySet.getUsagePoint(),
                        values,
                        getRangeToUpdate(customPropertySetRecord, values.getEffectiveRange()), customPropertySetRecord.getVersionId().get());
            } else {
                throw new ProcessorException(MessageSeeds.IMPORT_VERSIONED_VALUES_NOT_FOUND, customPropertySetRecord.getLineNumber());
            }
        } else {
            context.getCustomPropertySetService().setValuesVersionFor(usagePointCustomPropertySet.getCustomPropertySet(),usagePointCustomPropertySet.getUsagePoint(),
                    customPropertySetRecord.getCustomPropertySetValues(), getRangeToCreate(customPropertySetRecord));
        }
    }

    private void validateCreateOrUpdateNonVersionedSet(UsagePointPropertySet usagePointCustomPropertySet, CustomPropertySetRecord customPropertySetRecord){
        CustomPropertySetValues values = usagePointCustomPropertySet.getValues();
        if (values!=null){
            usagePointCustomPropertySet.getCustomPropertySet()
                    .getPropertySpecs()
                    .stream()
                    .filter(spec -> customPropertySetRecord.getCustomPropertySetValues().getProperty(spec.getName()) != null)
                    .forEach(spec -> {values.setProperty(spec.getName(), customPropertySetRecord.getCustomPropertySetValues().getProperty(spec.getName()));});
            context.getCustomPropertySetService().validateCustomPropertySetValues(usagePointCustomPropertySet.getCustomPropertySet(), values);
        }
        context.getCustomPropertySetService().validateCustomPropertySetValues(usagePointCustomPropertySet.getCustomPropertySet(), values);
    }

    private void createOrUpdateNonVersionedSet(UsagePointPropertySet usagePointCustomPropertySet, CustomPropertySetRecord customPropertySetRecord){
            CustomPropertySetValues values = usagePointCustomPropertySet.getValues();
            if (values!=null){
                usagePointCustomPropertySet.getCustomPropertySet()
                        .getPropertySpecs()
                        .stream()
                        .filter(spec -> customPropertySetRecord.getCustomPropertySetValues().getProperty(spec.getName()) != null)
                        .forEach(spec -> {values.setProperty(spec.getName(), customPropertySetRecord.getCustomPropertySetValues().getProperty(spec.getName()));});
                context.getCustomPropertySetService().setValuesFor(usagePointCustomPropertySet.getCustomPropertySet(),usagePointCustomPropertySet.getUsagePoint(),
                        values);
            }

            usagePointCustomPropertySet.setValues(values);

    }

    private Range<Instant> getRangeToCreate (CustomPropertySetRecord customPropertySetRecord){
        if (!customPropertySetRecord.getStartTime().isPresent() && !customPropertySetRecord.getEndTime().isPresent()){
            return Range.all();
        }
        else if (!customPropertySetRecord.getStartTime().isPresent()){
            return Range.lessThan(customPropertySetRecord.getEndTime().get());
        }
        else if (!customPropertySetRecord.getEndTime().isPresent()){
            return Range.atLeast(customPropertySetRecord.getStartTime().get());
        }
        else {
            return Range.closedOpen(customPropertySetRecord.getStartTime().get(),customPropertySetRecord.getEndTime().get());
        }
    }

    private Range<Instant> getRangeToUpdate (CustomPropertySetRecord customPropertySetRecord, Range<Instant> oldRange){
        if (!customPropertySetRecord.getStartTime().isPresent() && !customPropertySetRecord.getEndTime().isPresent()){
            return oldRange;
        }
        else if (!customPropertySetRecord.getStartTime().isPresent()){
            if(oldRange.hasUpperBound()){
                return Range.closedOpen(customPropertySetRecord.getStartTime().get(),oldRange.upperEndpoint());
            }
            else {
                return Range.lessThan(customPropertySetRecord.getEndTime().get());
            }
        }
        else if (!customPropertySetRecord.getEndTime().isPresent()){
            if(oldRange.hasUpperBound()){
                return Range.closedOpen(oldRange.lowerEndpoint(),customPropertySetRecord.getEndTime().get());
            }
            else {
                return Range.atLeast(customPropertySetRecord.getStartTime().get());
            }
        }
        else {
            return Range.closedOpen(customPropertySetRecord.getStartTime().get(),customPropertySetRecord.getEndTime().get());
        }
    }
}
