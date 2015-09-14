package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.ImportScheduleBuilder;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileImportScheduleResourceTest extends FileImportApplicationTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        mockTransaction();
        mockImporters();
    }

    @Test
    public void testGetImportSchedules() {
        mockImportSchedules(mockImportSchedule(1));

        String response = target("/importservices").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<?>>get("$.importSchedules")).hasSize(1);
    }

    @Test
    public void testGetImportSchedule() {
        ImportSchedule schedule = mockImportSchedule(13L);
        when(fileImportService.getImportSchedule(13L)).thenReturn(Optional.of(schedule));

        String response = target("/importservices/13").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(13);
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("Schedule 1");
        assertThat(jsonModel.<Boolean>get("$.active")).isEqualTo(true);
        assertThat(jsonModel.<Boolean>get("$.deleted")).isEqualTo(false);
        assertThat(jsonModel.<Boolean>get("$.importerAvailable")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.importDirectory")).isEqualTo("//test/path");
        assertThat(jsonModel.<String>get("$.inProcessDirectory")).isEqualTo("//test/path");
        assertThat(jsonModel.<String>get("$.successDirectory")).isEqualTo("//test/path");
        assertThat(jsonModel.<String>get("$.failureDirectory")).isEqualTo("//test/path");
        assertThat(jsonModel.<String>get("$.pathMatcher")).isEqualTo("*.csv");
        assertThat(jsonModel.<String>get("$.importerName")).isEqualTo("Test importer");
        assertThat(jsonModel.<String>get("$.application")).isEqualTo("Admin");
        assertThat(jsonModel.<Number>get("$.scanFrequency")).isEqualTo(1);
        assertThat(jsonModel.<List<?>>get("$.properties")).isEmpty();
    }

    @Test
    public void testAddImportSchedules() {
        FileImportScheduleInfo info = new FileImportScheduleInfo();
        info.importerInfo = new FileImporterInfo();
        info.importerInfo.name = "Test importer";
        info.scanFrequency = -1;
        info.importerInfo.displayName = "Display test importer";
        info.importerInfo.properties = ImmutableList.of();
        Entity<FileImportScheduleInfo> json = Entity.json(info);

        ImportScheduleBuilder builder = mock(ImportScheduleBuilder.class);
        when(fileImportService.newBuilder()).thenReturn(builder);
        when(builder.setName(any(String.class))).thenReturn(builder);
        when(builder.setDestination(any(String.class))).thenReturn(builder);
        when(builder.setPathMatcher(any(String.class))).thenReturn(builder);
        when(builder.setImportDirectory(any(Path.class))).thenReturn(builder);
        when(builder.setFailureDirectory(any(Path.class))).thenReturn(builder);
        when(builder.setProcessingDirectory(any(Path.class))).thenReturn(builder);
        when(builder.setSuccessDirectory(any(Path.class))).thenReturn(builder);
        when(builder.setImporterName(any(String.class))).thenReturn(builder);
        when(builder.setScheduleExpression(any(ScheduleExpression.class))).thenReturn(builder);

        FileImporterFactory importerFactory = mock(FileImporterFactory.class);
        when(fileImportService.getImportFactory(any(String.class))).thenReturn(Optional.of(importerFactory));
        when(importerFactory.getDestinationName()).thenReturn("Test destination");

        ImportSchedule importSchedule = mockImportSchedule(12L);
        when(builder.build()).thenReturn(importSchedule);

        Response response = target("/importservices").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testEditImportSchedule() {
        FileImportScheduleInfo info = new FileImportScheduleInfo();
        info.importerInfo = new FileImporterInfo();
        info.id = 1;
        info.name = "New name";
        info.scanFrequency = -1;
        info.importDirectory = "New folder";
        info.importerInfo.name = "Test importer";
        info.importerInfo.displayName = "Display test importer";
        info.importerInfo.properties = ImmutableList.of();
        Entity<FileImportScheduleInfo> json = Entity.json(info);

        ImportSchedule schedule = mockImportSchedule(1);
        when(fileImportService.getImportSchedule(1)).thenReturn(Optional.of(schedule));

        FileImporterFactory importerFactory = mock(FileImporterFactory.class);
        when(fileImportService.getImportFactory(any(String.class))).thenReturn(Optional.of(importerFactory));
        when(importerFactory.getDestinationName()).thenReturn("Test destination");

        Response response = target("/importservices/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testRemoveImportSchedule() {
        ImportSchedule schedule = mockImportSchedule(1);
        when(fileImportService.getImportSchedule(1)).thenReturn(Optional.of(schedule));

        Response response = target("/importservices/1").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    private void mockTransaction() {
        when(transactionService.execute(Matchers.any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = (Transaction) invocation.getArguments()[0];
            return transaction.perform();
        });
    }

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

    private void mockImportSchedules(ImportSchedule... importSchedules) {
        when(appService.getImportScheduleAppServers(Matchers.anyLong())).thenReturn(Collections.emptyList());
        Finder<ImportSchedule> finder = mock(Finder.class);
        when(fileImportService.findImportSchedules(Matchers.anyString())).thenReturn(finder);
        when(finder.from(any())).thenReturn(finder);
        when(finder.stream()).thenReturn(Arrays.asList(importSchedules).stream());
    }

    private ImportSchedule mockImportSchedule(long id) {
        Path testFolder = mock(Path.class);
        when(testFolder.toString()).thenReturn("//test/path");

        ImportSchedule schedule = mock(ImportSchedule.class);
        when(schedule.getId()).thenReturn(id);
        when(schedule.getName()).thenReturn("Schedule 1");
        when(schedule.getApplicationName()).thenReturn("SYS");
        when(schedule.getImporterName()).thenReturn("Test importer");
        mockFileImporerFactory("Test importer");
        when(schedule.getDestination()).thenReturn(mock(DestinationSpec.class));
        when(schedule.getImportDirectory()).thenReturn(testFolder);
        when(schedule.getPathMatcher()).thenReturn("*.csv");
        when(schedule.getScheduleExpression()).thenReturn(mock(ScheduleExpression.class));
        when(schedule.getFailureDirectory()).thenReturn(testFolder);
        when(schedule.getInProcessDirectory()).thenReturn(testFolder);
        when(schedule.getSuccessDirectory()).thenReturn(testFolder);
        when(schedule.getPropertySpecs()).thenReturn(ImmutableList.of());
        when(schedule.getProperties()).thenReturn(ImmutableMap.of());
        when(schedule.isImporterAvailable()).thenReturn(true);
        when(schedule.isDeleted()).thenReturn(false);
        when(schedule.isActive()).thenReturn(true);

        return  schedule;
    }

    private FileImporterFactory mockFileImporerFactory(String name) {
        FileImporterFactory importer = mock(FileImporterFactory.class);
        when(importer.getName()).thenReturn(name);
        when(fileImportService.getImportFactory("Test importer")).thenReturn(Optional.of(importer));
        return importer;
    }
}
