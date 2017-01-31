/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CsvUsagePointDataFormatterFactoryTest {

    @Mock
    private TimeService timeService;
    @Mock
    private BeanService beanService;
    @Mock
    private OrmService ormService;
    @Mock
    private DataExportService dataExportService;
    @Mock
    private NlsService nlsService;

    private PropertySpecService propertySpecService;

    private CsvUsagePointDataFormatterFactory csvUsagePointDataFormatterFactory;

    @Before
    public void before() {
        when(nlsService.getThesaurus(DataExportService.COMPONENTNAME, Layer.REST)).thenReturn(NlsModule.FakeThesaurus.INSTANCE);
        propertySpecService = new PropertySpecServiceImpl(timeService, ormService, beanService);

        csvUsagePointDataFormatterFactory = new CsvUsagePointDataFormatterFactory(propertySpecService, dataExportService, nlsService);
    }

    @Test
    public void getName() {
        // Business method
        String name = csvUsagePointDataFormatterFactory.getName();

        // Asserts
        assertThat(name).isEqualTo(CsvUsagePointDataFormatterFactory.NAME);
    }

    @Test
    public void getDisplayName() {
        // Business method
        String displayName = csvUsagePointDataFormatterFactory.getDisplayName();

        // Asserts
        assertThat(displayName).isEqualTo(Translations.Labels.CSV_USAGEPOINT_DATA_FORMATTER.getDefaultFormat());
    }

    @Test
    public void getProperties() {
        // Business method
        List<PropertySpec> propertySpecs = csvUsagePointDataFormatterFactory.getPropertySpecs();

        // Asserts
        assertThat(propertySpecs).hasSize(2);
        assertThat(propertySpecs.get(0).getName()).isEqualTo(FormatterProperties.TAG.getKey());
        assertThat(propertySpecs.get(1).getName()).isEqualTo(FormatterProperties.SEPARATOR.getKey());
    }
}
