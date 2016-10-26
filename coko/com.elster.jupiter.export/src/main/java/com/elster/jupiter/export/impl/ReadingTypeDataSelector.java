package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionService;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

class ReadingTypeDataSelector extends AbstractDataSelector {

    @Inject
    public ReadingTypeDataSelector(DataModel dataModel, TransactionService transactionService, Thesaurus thesaurus) {
        super(dataModel, transactionService, thesaurus);
    }

    static DataSelector from(DataModel dataModel, StandardDataSelectorImpl selector, Logger logger) {
        return dataModel.getInstance(ReadingTypeDataSelector.class).init(selector, logger);
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
                .map(EndDeviceMembership::getEndDevice)
                .filterSubType(Meter.class)
                .anyMatch(meter -> meter.getReadingTypes(range)
                                .stream()
                        .noneMatch(readingType -> getSelector().getReadingTypes().contains(readingType))
                );
        if (hasMismatchedMeters) {
            MessageSeeds.SOME_DEVICES_HAVE_NONE_OF_THE_SELECTED_READINGTYPES.log(getLogger(), getThesaurus(), getEndDeviceGroup().getName());
        }
    }

    @Override
    public Set<IReadingTypeDataExportItem> getActiveItems(DataExportOccurrence occurrence) {
        return decorate(getEndDeviceGroup()
                .getMembers(occurrence.getDefaultSelectorOccurrence()
                        .map(DefaultSelectorOccurrence::getExportedDataInterval)
                        .orElse(Range.all()))
                .stream())
                .map(EndDeviceMembership::getEndDevice)
                .filterSubType(Meter.class)
                .flatMap(super::readingTypeDataExportItems)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    AbstractItemDataSelector getItemDataSelector() {
        return getDataModel().getInstance(ReadingTypeDataItemDataSelector.class).init(getLogger());
    }

    private EndDeviceGroup getEndDeviceGroup() {
        return getSelector().getEndDeviceGroup();
    }
}
