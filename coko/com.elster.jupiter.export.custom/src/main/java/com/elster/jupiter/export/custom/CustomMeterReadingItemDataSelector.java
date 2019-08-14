/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.custom;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.export.IReadingTypeDataExportItem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;

class CustomMeterReadingItemDataSelector extends AbstractItemDataSelector {

    @Inject
    CustomMeterReadingItemDataSelector(Clock clock,
                                       ValidationService validationService,
                                       Thesaurus thesaurus,
                                       TransactionService transactionService,
                                       ThreadPrincipalService threadPrincipalService) {
        super(clock, validationService, thesaurus, transactionService, threadPrincipalService);
    }

    @Override
    void handleExcludeObject(IReadingTypeDataExportItem item, List<? extends BaseReadingRecord> readings, Range<Instant> interval, String itemDescription) {
        throw new UnsupportedOperationException(
                "Data exclusion on usage point not supported for meter reading item");
    }

    @Override
    Set<QualityCodeSystem> getQualityCodeSystems() {
        return ImmutableSet.of(QualityCodeSystem.MDC);
    }
}
