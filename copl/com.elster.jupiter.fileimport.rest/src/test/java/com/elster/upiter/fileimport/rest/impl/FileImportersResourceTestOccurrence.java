package com.elster.upiter.fileimport.rest.impl;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.rest.impl.FileImporterInfos;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileImportersResourceTestOccurrence extends FileImportOccurrenceApplicationTest {
    @Before
    public void setUp() throws Exception {
        super.setUp();
        mockImporters();
    }

    @Test
    public void testSysGetImporters() {
        Response response = target("/importers").queryParam("application", "SYS").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        FileImporterInfos infos = response.readEntity(FileImporterInfos.class);
        assertThat(infos.total).isEqualTo(1);
        assertThat(infos.fileImporters).hasSize(1);

        assertThat(infos.fileImporters.get(0).name).isEqualTo("Test importer");
        assertThat(infos.fileImporters.get(0).properties).hasSize(0);
    }

    @Test
    public void testMdcGetImporters() {
        Response response = target("/importers").queryParam("application", "MDC").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        FileImporterInfos infos = response.readEntity(FileImporterInfos.class);
        assertThat(infos.total).isEqualTo(0);
        assertThat(infos.fileImporters).hasSize(0);
    }

    @SuppressWarnings("unchecked")
    private void mockImporters() {
        FileImporterFactory importerFactory = mock(FileImporterFactory.class);
        FileImporter importer = mock(FileImporter.class);

        when(fileImportService.getAvailableImporters("SYS")).thenReturn(ImmutableList.of(importerFactory));
        when(importerFactory.getApplicationName()).thenReturn("SYS");
        when(importerFactory.getName()).thenReturn("Test importer");
        when(importerFactory.getDestinationName()).thenReturn("TestDestination");
        when(importerFactory.getProperties()).thenReturn(ImmutableList.of());
        when(importerFactory.createImporter(anyMap())).thenReturn(importer);
    }
}