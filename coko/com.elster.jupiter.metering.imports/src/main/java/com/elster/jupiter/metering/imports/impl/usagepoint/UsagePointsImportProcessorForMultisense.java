package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.imports.impl.FileImportLogger;
import com.elster.jupiter.metering.imports.impl.FileImportProcessor;
import com.elster.jupiter.metering.imports.impl.MessageSeeds;
import com.elster.jupiter.metering.imports.impl.MeteringDataImporterContext;
import com.elster.jupiter.metering.imports.impl.exceptions.ProcessorException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.Optional;

public class UsagePointsImportProcessorForMultisense implements FileImportProcessor<UsagePointImportRecord> {

    private final MeteringDataImporterContext context;

    UsagePointsImportProcessorForMultisense(MeteringDataImporterContext context) {
        this.context = context;
    }

    @Override
    public void process(UsagePointImportRecord data, FileImportLogger logger) throws ProcessorException {
        try {
            processUsagePoint(data);
        } catch (ConstraintViolationException e) {
            for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
                logger.warning(MessageSeeds.IMPORT_USAGEPOINT_CONSTRAINT_VOLATION, data.getLineNumber(),
                        violation.getPropertyPath(), violation.getMessage());
            }
            throw new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_INVALIDDATA, data.getLineNumber());
        }
    }

    private UsagePoint processUsagePoint(UsagePointImportRecord data) {
        UsagePoint usagePoint;
        // TODO: update
        String mRID = data.getmRID()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_MRID_INVALID, data.getLineNumber()));
        String serviceKindString = data.getServiceKind()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID, data.getLineNumber()));
        ServiceKind serviceKind = Arrays.stream(ServiceKind.values())
                .filter(candidate -> candidate.name().equalsIgnoreCase(serviceKindString))
                .findFirst()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_NO_SUCH_SERVICEKIND, data.getLineNumber(), serviceKindString));
        Optional<UsagePoint> usagePointOptional = context.getMeteringService().findUsagePoint(mRID);
        Optional<ServiceCategory> serviceCategory = context.getMeteringService().getServiceCategory(serviceKind);

        if (usagePointOptional.isPresent()) {
            usagePoint = usagePointOptional.get();
            if (usagePoint.getServiceCategory().getId() != serviceCategory.get().getId()) {
                throw new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICECATEGORY_CHANGE, data.getLineNumber(), serviceKindString);
            }
            return usagePoint;
        } else {
            return createUsagePoint(serviceCategory.get().newUsagePoint(mRID, data.getInstallationTime().orElse(context.getClock().instant())), data);
        }
    }

    private UsagePoint createUsagePoint(UsagePointBuilder usagePointBuilder, UsagePointImportRecord data) {
        usagePointBuilder.withIsSdp(false);
        usagePointBuilder.withIsVirtual(true);
        // TODO: update import & remove withName
        usagePointBuilder.withName(data.getName());
        UsagePoint usagePoint = usagePointBuilder.create();
        usagePoint.addDetail(usagePoint.getServiceCategory().newUsagePointDetail(usagePoint, context.getClock().instant()));
        usagePoint.update();
        setMetrologyConfigurationForUsagePoint(data, usagePoint);
        return usagePoint;
    }

    private void setMetrologyConfigurationForUsagePoint(UsagePointImportRecord data, UsagePoint usagePoint) {
        data.getMetrologyConfiguration().ifPresent(metrologyConfigurationName -> {
            UsagePointMetrologyConfiguration metrologyConfiguration = context.getMetrologyConfigurationService().findMetrologyConfiguration(metrologyConfigurationName)
                    .filter(mc -> mc instanceof UsagePointMetrologyConfiguration)
                    .map(UsagePointMetrologyConfiguration.class::cast)
                    .filter(UsagePointMetrologyConfiguration::isActive)
                    .orElseThrow(() -> new ProcessorException(MessageSeeds.BAD_METROLOGY_CONFIGURATION, data.getLineNumber()));
            if (!metrologyConfiguration.getServiceCategory().equals(usagePoint.getServiceCategory())) {
                throw new ProcessorException(MessageSeeds.SERVICE_CATEGORIES_DO_NOT_MATCH, data.getLineNumber());
            }
            if (!data.getMetrologyConfigurationApplyTime().isPresent()) {
                throw new ProcessorException(MessageSeeds.EMPTY_METROLOGY_CONFIGURATION_TIME, data.getLineNumber());
            }
            usagePoint.apply(metrologyConfiguration, data.getMetrologyConfigurationApplyTime().get());
        });
    }
}
