/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.ExportData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;

import java.util.Collections;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class NullDataFormatterTest {
    private static final Thesaurus THESAURUS = NlsModule.FakeThesaurus.INSTANCE;

    @Mock
    private DataExportOccurrence dataExportOccurrence;
    @Mock
    private ReadingTypeDataExportItem dataExportItem;
    @Mock
    private ExportData exportData;
    private Logger logger = Logger.getAnonymousLogger();

    @Test
    public void testNames() {
        assertThat(getFactory().getName()).isEqualTo("No operation data formatter");
        assertThat(getFactory().getDisplayName()).isEqualTo("Not applicable (for 'Web service' destination)");
    }

    @Test
    public void testProperties() {
        assertThat(getFactory().getPropertySpecs()).isEmpty();
        getFactory().validateProperties(Collections.emptyList()); // no exception
    }

    @Test
    public void testGetFormatter() {
        assertThat(getFactory().createDataFormatter(Collections.emptyMap())).isInstanceOf(NullDataFormatter.class);
    }

    @Test
    public void testFormatter() {
        NullDataFormatter formatter = getFormatter();
        formatter.startExport(dataExportOccurrence, logger);
        formatter.startItem(dataExportItem);
        assertThatThrownBy(() -> formatter.processData(Stream.of(exportData)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No data formatter is found to format the data.");
        formatter.endItem(dataExportItem);
        formatter.endExport();
        //no exceptions for start- & end- methods
    }

    private NullDataFormatter getFormatter() {
        return new NullDataFormatter();
    }

    private NullDataFormatterFactory getFactory() {
        return new NullDataFormatterFactory(THESAURUS);
    }
}
