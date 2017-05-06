/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.MeterReadingValidationData;
import com.elster.jupiter.export.MissingDataOption;
import com.elster.jupiter.export.ReadingDataSelectorConfig;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.StructureMarker;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.ExtraCollectors.toImmutableRangeSet;

class MeterReadingItemDataSelector extends AbstractItemDataSelector {

    @Inject
    MeterReadingItemDataSelector(Clock clock,
                                 ValidationService validationService,
                                 Thesaurus thesaurus,
                                 TransactionService transactionService,
                                 ThreadPrincipalService threadPrincipalService) {
        super(clock, validationService, thesaurus, transactionService, threadPrincipalService);
    }

    @Override
    Set<QualityCodeSystem> getQualityCodeSystems() {
        return ImmutableSet.of(QualityCodeSystem.MDC);
    }
}
