package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
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
            getUsagePointForMdc(data, logger);
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

    private UsagePoint getUsagePointForMdc(UsagePointImportRecord data, FileImportLogger logger) {
        UsagePoint usagePoint;
        String mRID = data.getmRID()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_MRID_INVALID, data.getLineNumber()));
        String serviceKindString = data.getServiceKind()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_NO_SUCH_SERVICEKIND, data.getLineNumber()));
        ServiceKind serviceKind = Arrays.stream(ServiceKind.values())
                .filter(candidate -> candidate.name().equalsIgnoreCase(serviceKindString))
                .findFirst()
                .orElseThrow(() -> new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICEKIND_INVALID, data.getLineNumber(), serviceKindString));
        Optional<UsagePoint> usagePointOptional = context.getMeteringService().findUsagePoint(mRID);
        Optional<ServiceCategory> serviceCategory = context.getMeteringService().getServiceCategory(serviceKind);

        if (usagePointOptional.isPresent()) {
            usagePoint = usagePointOptional.get();
            if (usagePoint.getServiceCategory().getId() != serviceCategory.get().getId()) {
                throw new ProcessorException(MessageSeeds.IMPORT_USAGEPOINT_SERVICECATEGORY_CHANGE, data.getLineNumber(), serviceKindString);
            }
            return updateUsagePointForMdc(usagePoint, data, logger);
        } else {
            return createUsagePointForMdc(serviceCategory.get()
                    .newUsagePoint(mRID, data.getInstallationTime()
                            .orElse(context.getClock().instant())), data, logger);
        }
    }

    private UsagePoint createUsagePointForMdc(UsagePointBuilder usagePointBuilder, UsagePointImportRecord data, FileImportLogger logger) {
        usagePointBuilder.withIsSdp(false);
        usagePointBuilder.withIsVirtual(true);
        usagePointBuilder.withName(data.getName().orElse(null));
        UsagePoint usagePoint = usagePointBuilder.create();
        usagePoint.addDetail(usagePoint.getServiceCategory()
                .newUsagePointDetail(usagePoint, context.getClock().instant()));
        usagePoint.update();
        return usagePoint;
    }

    private UsagePoint updateUsagePointForMdc(UsagePoint usagePoint, UsagePointImportRecord data, FileImportLogger logger) {
        usagePoint.setName(data.getName().orElse(null));
        usagePoint.update();
        return usagePoint;
    }
}
