package com.elster.jupiter.metering.imports.impl.usagepoint.usagepointimport;
;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.imports.impl.MessageSeeds;
import com.elster.jupiter.metering.imports.impl.usagepoint.FileImportLogger;
import com.elster.jupiter.metering.imports.impl.usagepoint.FileImportProcessor;
import com.elster.jupiter.metering.imports.impl.usagepoint.MeteringDataImporterContext;
import com.elster.jupiter.metering.imports.impl.usagepoint.exceptions.ProcessorException;

import java.util.Map;
import java.util.Optional;


public class UsagePointsImportProcessor implements FileImportProcessor<UsagePointImportRecord> {

    private final MeteringDataImporterContext context;


    UsagePointsImportProcessor(MeteringDataImporterContext context) {
        this.context = context;
    }

    @Override
    public void process(UsagePointImportRecord data, FileImportLogger logger) throws ProcessorException {
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
            usagePoint = updateUsagePoint(usagePoint,data,logger);
        } else {
            usagePoint = createUsagePoint(serviceCategory.get().newUsagePoint(mRID),data,logger);
        }

        Optional<? extends UsagePointDetail> detail = usagePoint.getDetail(context.getClock().instant());

        if (detail.isPresent()) {
            switch (usagePoint.getServiceCategory().getKind()) {
                case ELECTRICITY:
                    addElectricityDetails(usagePoint.newElectricityDetailBuilder(context.getClock().instant()),
                            (ElectricityDetail) detail.get(), data, logger);
                    break;
                case GAS:
                    addGasDetails(usagePoint.newGasDetailBuilder(context.getClock().instant()),
                            (GasDetail) detail.get(), data, logger);
                    break;
                case WATER:
                    addWaterDetails(usagePoint.newWaterDetailBuilder(context.getClock().instant()),
                            (WaterDetail) detail.get(), data, logger);
                    break;
                case HEAT:
                    addHeatDetails(usagePoint.newHeatDetailBuilder(context.getClock().instant()),
                            (HeatDetail) detail.get(), data, logger);
                    break;
                default:
                    addDetails(usagePoint.newDefaultDetailBuilder(context.getClock().instant()),
                            detail.get(), data, logger);
            }
        } else {
            switch (usagePoint.getServiceCategory().getKind()){
                case ELECTRICITY:
                    addElectricityDetails(usagePoint.newElectricityDetailBuilder(context.getClock().instant()),
                            (ElectricityDetail) usagePoint.getServiceCategory().newUsagePointDetail(usagePoint,context.getClock().instant()),data,logger);
                    break;
                case GAS:
                    addGasDetails(usagePoint.newGasDetailBuilder(context.getClock().instant()),
                            (GasDetail) usagePoint.getServiceCategory().newUsagePointDetail(usagePoint,context.getClock().instant()),data,logger);
                    break;
                case WATER:
                    addWaterDetails(usagePoint.newWaterDetailBuilder(context.getClock().instant()),
                            (WaterDetail) usagePoint.getServiceCategory().newUsagePointDetail(usagePoint,context.getClock().instant()),data,logger);
                    break;
                case HEAT:
                    addHeatDetails(usagePoint.newHeatDetailBuilder(context.getClock().instant()),
                            (HeatDetail) usagePoint.getServiceCategory().newUsagePointDetail(usagePoint,context.getClock().instant()),data,logger);
                    break;
                default:
                    addDetails(usagePoint.newDefaultDetailBuilder(context.getClock().instant()),
                            usagePoint.getServiceCategory().newUsagePointDetail(usagePoint,context.getClock().instant()),data,logger);
            }
        }

        addCustomPropertySetValues(data, logger, usagePoint);

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
        usagePointBuilder.withInstallationTime(data.getInstallationTime().orElse(context.getClock().instant()));
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

    private ElectricityDetail addElectricityDetails(ElectricityDetailBuilder detailBuilder, ElectricityDetail oldDetail, UsagePointImportRecord data, FileImportLogger logger){
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
        return detailBuilder.create();
    }

    private GasDetail addGasDetails(GasDetailBuilder detailBuilder, GasDetail oldDetail, UsagePointImportRecord data, FileImportLogger logger){
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
        return detailBuilder.create();
    }

    private WaterDetail addWaterDetails(WaterDetailBuilder detailBuilder, WaterDetail oldDetail, UsagePointImportRecord data, FileImportLogger logger){
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
        return detailBuilder.create();
    }

    private HeatDetail addHeatDetails(HeatDetailBuilder detailBuilder, HeatDetail oldDetail, UsagePointImportRecord data, FileImportLogger logger){
        detailBuilder.withCollar(data.isCollarInstalled().orElse(oldDetail.isCollarInstalled()));
        detailBuilder.withPressure(data.getPressure().orElse(oldDetail.getPressure()));
        detailBuilder.withPhysicalCapacity(data.getPhysicalCapacity().orElse(oldDetail.getPhysicalCapacity()));
        detailBuilder.withBypass(data.isBypassInstalled().orElse(oldDetail.isBypassInstalled()));
        detailBuilder.withBypassStatus(data.getBypassStatus().orElse(oldDetail.getBypassStatus()));
        detailBuilder.withValve(data.isValveInstalled().orElse(oldDetail.isValveInstalled()));
        detailBuilder.withInterruptible(data.isInterruptible().orElse(oldDetail.isInterruptible()));
        return detailBuilder.create();
    }

    private UsagePointDetail addDetails(UsagePointDetailBuilder detailBuilder, UsagePointDetail oldDetail, UsagePointImportRecord data, FileImportLogger logger){
        detailBuilder.withCollar(data.isCollarInstalled().orElse(oldDetail.isCollarInstalled()));
        return detailBuilder.create();
    }

    @Override
    public void complete(FileImportLogger logger) {
        System.err.println("USAGE POINT CREATED");
    }

    public void addCustomPropertySetValues(UsagePointImportRecord data, FileImportLogger logger, UsagePoint usagePoint){

        Map<CustomPropertySet, CustomPropertySetValues> customPropertySetValues = data.getCustomPropertySetValues();

        for (RegisteredCustomPropertySet propertySet : usagePoint.getServiceCategory().getCustomPropertySets()) {
            if (customPropertySetValues.containsKey(propertySet.getCustomPropertySet())){
                if(propertySet.getCustomPropertySet().isVersioned()) {
                    context.getCustomPropertySetService()
                            .setValuesFor(propertySet.getCustomPropertySet(), usagePoint, customPropertySetValues.get(propertySet
                                    .getCustomPropertySet()));
                } else {
                    context.getCustomPropertySetService()
                            .setValuesFor(propertySet.getCustomPropertySet(), usagePoint, customPropertySetValues.get(propertySet
                                    .getCustomPropertySet()), data.getInstallationTime().orElse(context.getClock().instant()));
                }
            }
        }
    }
}
