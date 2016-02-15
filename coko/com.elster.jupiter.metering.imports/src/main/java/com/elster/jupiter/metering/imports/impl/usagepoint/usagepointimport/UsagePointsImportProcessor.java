package com.elster.jupiter.metering.imports.impl.usagepoint.usagepointimport;
;
import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.imports.impl.MessageSeeds;
import com.elster.jupiter.metering.imports.impl.usagepoint.FileImportLogger;
import com.elster.jupiter.metering.imports.impl.usagepoint.FileImportProcessor;
import com.elster.jupiter.metering.imports.impl.usagepoint.MeteringDataImporterContext;
import com.elster.jupiter.metering.imports.impl.usagepoint.exceptions.ProcessorException;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;


public class UsagePointsImportProcessor implements FileImportProcessor<UsagePointImportRecord> {

    private final MeteringDataImporterContext context;

    private UsagePoint usagePoint;

    UsagePointsImportProcessor(MeteringDataImporterContext context) {
        this.context = context;
    }

    @Override
    public void process(UsagePointImportRecord data, FileImportLogger logger) throws ProcessorException {

        MeteringService meteringService = context.getMeteringService();

        UsagePoint usagePoint;
        boolean isUpdatable = data.getAllowUpdate();
        Optional<UsagePoint> usagePointOptional = meteringService.findUsagePoint(data.getmRID());
        Optional<ServiceCategory> serviceCategory = meteringService.getServiceCategory(ServiceKind.valueOf(data.getServiceKind()));
        if (usagePointOptional.isPresent()) {
            usagePoint = usagePointOptional.get();
            if (usagePoint.getServiceCategory().getId() != serviceCategory.get().getId()) {
                throw new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID, data.getLineNumber());
            }
        } else {
            usagePoint = serviceCategory.get().newUsagePoint(data.getmRID()).create();
            isUpdatable = true;
        }
        if (isUpdatable) {
            usagePoint.setSdp(false);
            if (data.getServiceLocationID() > 0) {
                Optional<ServiceLocation> serviceLocation = meteringService.findServiceLocation(data.getServiceLocationID());
                if (serviceLocation.isPresent()) {
                    usagePoint.setVirtual(false);
                    usagePoint.setServiceLocation(serviceLocation.get());
                } else {
                    usagePoint.setVirtual(true);
                    usagePoint.setServiceLocation(null);
                    logger.warning(MessageSeeds.IMPORT_USAGEPOINT_SERVICELOCATION_INVALID, data.getLineNumber());
                }
            }
            if (!Checks.is(data.getName()).emptyOrOnlyWhiteSpace()) {
                usagePoint.setName(data.getName());
            }
            if (!Checks.is(data.getAliasName()).emptyOrOnlyWhiteSpace()) {
                usagePoint.setAliasName(data.getAliasName());
            }
            if (!Checks.is(data.getDescription()).emptyOrOnlyWhiteSpace()) {
                usagePoint.setDescription(data.getDescription());
            }
            if (!Checks.is(data.getOutageregion()).emptyOrOnlyWhiteSpace()) {
                usagePoint.setOutageRegion(data.getOutageregion());
            }
            if (!Checks.is(data.getReadcycle()).emptyOrOnlyWhiteSpace()) {
                usagePoint.setReadCycle(data.getReadcycle());
            }
            if (!Checks.is(data.getReadroute()).emptyOrOnlyWhiteSpace()) {
                usagePoint.setReadRoute(data.getReadroute());
            }
            if (!Checks.is(data.getServicePriority()).emptyOrOnlyWhiteSpace()) {
                usagePoint.setServicePriority(data.getServicePriority());
            }
            UsagePointDetail usagePointDetail = usagePoint.getServiceCategory().newUsagePointDetail(usagePoint, context.getClock().instant());
            if (usagePointDetail instanceof ElectricityDetail) {
                ElectricityDetail eDetail = (ElectricityDetail) usagePointDetail;
                eDetail.setGrounded(data.getGrounded());
                if (!data.getPhaseCode().isEmpty()) {
                    eDetail.setPhaseCode(PhaseCode.valueOf(data.getPhaseCode()));
                }
                if (data.getRatedPowerValue()!=null) {
                    eDetail.setRatedPower(Quantity.create(data.getRatedPowerValue(), data.getRatedPowerMultiplier(), data.getRatedPowerUnit()));
                }
                if (data.getRatedCurrentValue()!=null) {
                    eDetail.setRatedCurrent(Quantity.create(data.getRatedCurrentValue(), data.getRatedCurrentMultiplier(), data.getRatedCurrentUnit()));
                }
                if (data.getEstimatedLoadValue()!=null) {
                    eDetail.setEstimatedLoad(Quantity.create(data.getEstimatedLoadValue(), data.getEstimatedLoadMultiplier(), data.getEstimatedLoadUnit()));
                }
                if (data.getNominalVoltageValue()!=null) {
                    eDetail.setNominalServiceVoltage(Quantity.create(data.getNominalVoltageValue(), data.getNominalVoltageMultiplier(), data.getNominalVoltageUnit()));
                }
                usagePoint.addDetail(usagePointDetail);
            }
            usagePoint.update();
        }

        addCustomPropertySetValues(data.getCustomPropertySetValues(), logger, usagePoint);

    }

    @Override
    public void complete(FileImportLogger logger) {
        System.err.println("USAGE POINT CREATED");
    }

    public void addCustomPropertySetValues(Map<CustomPropertySet, CustomPropertySetValues> customPropertySetValues, FileImportLogger logger, UsagePoint usagePoint){
        for (RegisteredCustomPropertySet propertySet : usagePoint.getServiceCategory().getCustomPropertySets()) {
            if (customPropertySetValues.containsKey(propertySet.getCustomPropertySet())){
                context.getCustomPropertySetService().setValuesFor(propertySet.getCustomPropertySet(),usagePoint,customPropertySetValues.get(propertySet.getCustomPropertySet()));
            }
        }
    }

}
