package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.DataSelectorFactory;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by igh on 3/06/2015.
 */
public class SingleDeviceDataSelectorFactory implements DataSelectorFactory {

    private final TransactionService transactionService;
    private final Thesaurus thesaurus;
    private final MeteringService meteringService;

    private final TimeService timeService;
    private final PropertySpecService propertySpecService;

    public SingleDeviceDataSelectorFactory(TransactionService transactionService, MeteringService meteringService, Thesaurus thesaurus, PropertySpecService propertySpecService, TimeService timeService) {
        this.transactionService = transactionService;
        this.meteringService = meteringService;
        this.thesaurus = thesaurus;
        this.timeService = timeService;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public DataSelector createDataSelector(Map<String, Object> properties, Logger logger) {
        return DelegatingDataSelector.INSTANCE;
    }

    @Override
    public void validateProperties(List<DataExportProperty> properties) {
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {

        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();

        builder.add(propertySpecService.bigDecimalPropertySpec("Device id", true, new BigDecimal(1)));

        return builder.build();
    }

    @Override
    public String getName() {
        return "Single Device Data Selector";
    }

    @Override
    public String getDisplayName() {
        return thesaurus.getString(getNlsKey().getKey(), getName());
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    private NlsKey getNlsKey() {
        return SimpleNlsKey.key(DataExportService.COMPONENTNAME, Layer.DOMAIN, ReadingTypeDataSelectorImpl.class.getName());
    }

    private static enum DelegatingDataSelector implements DataSelector {

        INSTANCE;

        @Override
        public Stream<ExportData> selectData(DataExportOccurrence dataExportOccurrence) {
            //todo: to be implemented
            return dataExportOccurrence.getTask().getReadingTypeDataSelector().orElseThrow(IllegalStateException::new).selectData(dataExportOccurrence);
        }
    }
}
