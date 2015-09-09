package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.google.common.collect.ImmutableList;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileImportersResourceTest extends FileImportApplicationTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        mockImporters();
    }

    @Test
    public void testSysGetImporters() {
        String response = target("/importers").request().header("X-CONNEXO-APPLICATION-NAME", "SYS").get(String.class);
        JsonModel jsonModel = JsonModel.model(response);

        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<?>>get("$.fileImporters")).hasSize(1);
        assertThat(jsonModel.<String>get("$.fileImporters[0].name")).isEqualTo("Test importer");
        assertThat(jsonModel.<List<?>>get("$.fileImporters[0].properties")).isEmpty();
    }

    @Test
    public void testMdcGetImporters() {
        String response = target("/importers").request().header("X-CONNEXO-APPLICATION-NAME", "MDC").get(String.class);
        JsonModel jsonModel = JsonModel.model(response);

        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List<?>>get("$.fileImporters")).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private void mockImporters() {
        FileImporterFactory importerFactory = mock(FileImporterFactory.class);
        FileImporter importer = mock(FileImporter.class);

        when(fileImportService.getAvailableImporters("SYS")).thenReturn(ImmutableList.of(importerFactory));
        when(importerFactory.getApplicationName()).thenReturn("SYS");
        when(importerFactory.getName()).thenReturn("Test importer");
        when(importerFactory.getDestinationName()).thenReturn("TestDestination");
        when(importerFactory.getPropertySpecs()).thenReturn(ImmutableList.of());
        when(importerFactory.createImporter(anyMap())).thenReturn(importer);
    }
}