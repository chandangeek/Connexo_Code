/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.Membership;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionService;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.logging.Logger;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

class MeterReadingSelector extends AbstractDataSelector {

    private MeterReadingSelectorConfigImpl selectorConfig;

    @Inject
    MeterReadingSelector(DataModel dataModel, TransactionService transactionService, Thesaurus thesaurus) {
        super(dataModel, transactionService, thesaurus);
    }

    static MeterReadingSelector from(DataModel dataModel, MeterReadingSelectorConfigImpl selectorConfig, Logger logger) {
        return dataModel.getInstance(MeterReadingSelector.class).init(selectorConfig, logger);
    }

    MeterReadingSelector init(MeterReadingSelectorConfigImpl selectorConfig, Logger logger) {
        super.init(logger);
        this.selectorConfig = selectorConfig;
        return this;
    }

    @Override
    MeterReadingSelectorConfigImpl getSelectorConfig() {
        return selectorConfig;
    }

    @Override
    void warnIfObjectsHaveNoneOfTheReadingTypes(DataExportOccurrence occurrence) {
        warnIfDevicesHaveNoneOfTheReadingTypes(occurrence);
    }

    private void warnIfDevicesHaveNoneOfTheReadingTypes(DataExportOccurrence occurrence) {
        Range<Instant> range = occurrence.getDefaultSelectorOccurrence()
                .map(DefaultSelectorOccurrence::getExportedDataInterval)
                .orElse(Range.all());
        boolean hasMismatchedMeters = decorate(getEndDeviceGroup()
                .getMembers(range)
                .stream())
                .map(Membership::getMember)
                .filterSubType(Meter.class)
                .anyMatch(meter -> meter.getReadingTypes(range)
                        .stream()
                        .noneMatch(readingType -> getSelectorConfig().getReadingTypes().contains(readingType))
                );
        if (hasMismatchedMeters) {
            MessageSeeds.SOME_DEVICES_HAVE_NONE_OF_THE_SELECTED_READINGTYPES.log(getLogger(), getThesaurus(), getEndDeviceGroup().getName());
        }
    }

    @Override
    AbstractItemDataSelector getItemDataSelector() {
        return getDataModel().getInstance(MeterReadingItemDataSelector.class).init(getLogger());
    }

    private EndDeviceGroup getEndDeviceGroup() {
        return getSelectorConfig().getEndDeviceGroup();
    }
}
