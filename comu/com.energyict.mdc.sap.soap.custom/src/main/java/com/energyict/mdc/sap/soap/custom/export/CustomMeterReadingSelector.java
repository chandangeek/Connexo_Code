/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.custom.export;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.MeterReadingSelectorConfig;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.Membership;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.logging.Logger;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

public class CustomMeterReadingSelector extends AbstractDataSelector {

    private SAPCustomPropertySets sapCustomPropertySets;
    private MeterReadingSelectorConfig selectorConfig;

    @Inject
    CustomMeterReadingSelector(DataModel dataModel, TransactionService transactionService, Thesaurus thesaurus) {
        super(dataModel, transactionService, thesaurus);
    }

    static CustomMeterReadingSelector from(DataModel dataModel, MeterReadingSelectorConfig selectorConfig, SAPCustomPropertySets sapCustomPropertySets, Logger logger) {
        return dataModel.getInstance(CustomMeterReadingSelector.class).init(selectorConfig, sapCustomPropertySets, logger);
    }

    CustomMeterReadingSelector init(MeterReadingSelectorConfig selectorConfig, SAPCustomPropertySets sapCustomPropertySets, Logger logger) {
        super.init(logger);
        this.selectorConfig = selectorConfig;
        this.sapCustomPropertySets = sapCustomPropertySets;
        return this;
    }

    @Override
    MeterReadingSelectorConfig getSelectorConfig() {
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
    CustomMeterReadingItemDataSelector getItemDataSelector() {
        return getDataModel().getInstance(CustomMeterReadingItemDataSelector.class).init(sapCustomPropertySets, getLogger());
    }

    private EndDeviceGroup getEndDeviceGroup() {
        return getSelectorConfig().getEndDeviceGroup();
    }
}
