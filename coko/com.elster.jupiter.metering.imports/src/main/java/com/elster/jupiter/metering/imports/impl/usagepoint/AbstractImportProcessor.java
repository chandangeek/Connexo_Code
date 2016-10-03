package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.imports.impl.FileImportProcessor;
import com.elster.jupiter.metering.imports.impl.FileImportRecord;
import com.elster.jupiter.metering.imports.impl.MeteringDataImporterContext;

import java.time.Clock;
import java.util.Optional;

public abstract class AbstractImportProcessor<T extends FileImportRecord> implements FileImportProcessor<T> {

    private final MeteringDataImporterContext context;

    AbstractImportProcessor(MeteringDataImporterContext context) {
        this.context = context;
    }

    MeteringDataImporterContext getContext() {
        return context;
    }

    Clock getClock() {
        return context.getClock();
    }

    Optional<UsagePoint> findUsagePointByIdentifier(String mridOrName) {
        MeteringService meteringService = getContext().getMeteringService();
        Optional<UsagePoint> usagePoint = meteringService.findUsagePoint(mridOrName);
        return usagePoint.isPresent() ? usagePoint : meteringService.findUsagePointByName(mridOrName);
    }
}
