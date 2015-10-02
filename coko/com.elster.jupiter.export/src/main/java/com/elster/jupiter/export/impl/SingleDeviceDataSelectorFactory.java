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
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.collect.ImmutableList;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.export.processor.SingleDeviceDataSelectorFactory",
        service = DataSelectorFactory.class, property = { DataExportService.DATA_TYPE_PROPERTY + "="+ DataExportService.STANDARD_READING_DATA_TYPE},
        immediate = true)
public class SingleDeviceDataSelectorFactory implements DataSelectorFactory {

    private volatile TransactionService transactionService;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;

    private volatile TimeService timeService;
    private volatile PropertySpecService propertySpecService;

    public SingleDeviceDataSelectorFactory() {}

    @Inject
    public SingleDeviceDataSelectorFactory(TransactionService transactionService, MeteringService meteringService, Thesaurus thesaurus, PropertySpecService propertySpecService, TimeService timeService) {
        this.transactionService = transactionService;
        this.meteringService = meteringService;
        this.thesaurus = thesaurus;
        this.timeService = timeService;
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
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
        return "Single Device Data Selector";
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    private NlsKey getNlsKey() {
        return SimpleNlsKey.key(DataExportService.COMPONENTNAME, Layer.DOMAIN, StandardDataSelectorImpl.class.getName());
    }

    private static enum DelegatingDataSelector implements DataSelector {

        INSTANCE;

        @Override
        public Stream<ExportData> selectData(DataExportOccurrence dataExportOccurrence) {
            return null;
        }
    }
}
