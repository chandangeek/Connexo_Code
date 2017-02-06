/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.fileimport.csvimport.FileImportLogger;
import com.elster.jupiter.metering.imports.impl.MessageSeeds;
import com.elster.jupiter.metering.imports.impl.MeteringDataImporterContext;
import com.elster.jupiter.fileimport.csvimport.exceptions.ProcessorException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.Optional;

public class UsagePointsImportProcessorForMultisense extends AbstractImportProcessor<UsagePointImportRecord> {

    UsagePointsImportProcessorForMultisense(MeteringDataImporterContext context) {
        super(context);
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

    @Override
    public void complete(FileImportLogger logger) {

    }

    private UsagePoint processUsagePoint(UsagePointImportRecord data) {
        String identifier = data.getUsagePointIdentifier()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_IDENTIFIER_INVALID, data.getLineNumber()));
        String serviceKindString = data.getServiceKind()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID, data.getLineNumber()));
        ServiceKind serviceKind = Arrays.stream(ServiceKind.values())
                .filter(candidate -> candidate.name().equalsIgnoreCase(serviceKindString))
                .findFirst()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_NO_SUCH_SERVICEKIND, data.getLineNumber(), serviceKindString));
        Optional<UsagePoint> usagePoint = findUsagePointByIdentifier(identifier);
        Optional<ServiceCategory> serviceCategory = getContext().getMeteringService().getServiceCategory(serviceKind);

        if (usagePoint.isPresent()) {
            if (usagePoint.get().getServiceCategory().getId() != serviceCategory.get().getId()) {
                throw new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICECATEGORY_CHANGE, data.getLineNumber(), serviceKindString);
            }
            return usagePoint.get();
        } else {
            return createUsagePoint(serviceCategory.get().newUsagePoint(identifier, data.getInstallationTime().orElse(getClock().instant())), data);
        }
    }

    private UsagePoint createUsagePoint(UsagePointBuilder usagePointBuilder, UsagePointImportRecord data) {
        usagePointBuilder.withIsSdp(false);
        usagePointBuilder.withIsVirtual(true);
        UsagePoint usagePoint = usagePointBuilder.create();
        usagePoint.addDetail(usagePoint.getServiceCategory().newUsagePointDetail(usagePoint, getClock().instant()));
        setMetrologyConfigurationForUsagePoint(data, usagePoint);
        usagePoint.update();
        return usagePoint;
    }

    private void setMetrologyConfigurationForUsagePoint(UsagePointImportRecord data, UsagePoint usagePoint) {
        data.getMetrologyConfiguration().ifPresent(metrologyConfigurationName -> {
            UsagePointMetrologyConfiguration metrologyConfiguration = getContext().getMetrologyConfigurationService().findMetrologyConfiguration(metrologyConfigurationName)
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
