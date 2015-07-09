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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileImportOccurrenceScheduleResourceTest extends FileImportOccurrenceApplicationTest {
    @Before
    public void setUp() throws Exception {
        super.setUp();
        mockTransaction();
        mockImporters();
    }

    @Test
    public void testGetImportSchedules() {
        mockImportSchedules(mockImportSchedule(1));
        Response response = target("/importservices").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        FileImportScheduleInfos infos = response.readEntity(FileImportScheduleInfos.class);
        assertThat(infos.total).isEqualTo(1);
        assertThat(infos.importSchedules).hasSize(1);
    }

    @Test
    public void testGetImportSchedule() {
        ImportSchedule schedule = mockImportSchedule(1);
        when(fileImportService.getImportSchedule(anyLong())).thenReturn(Optional.of(schedule));

        Response response = target("/importservices/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        FileImportScheduleInfo info = response.readEntity(FileImportScheduleInfo.class);
        assertThat(info.name).isEqualTo("Schedule 1");
        assertThat(info.application).isEqualTo("SYS");
        assertThat(info.id).isEqualTo(1);
    }

    @Test
    public void testAddImportSchedules() {
        ImportSchedule schedule = mockImportSchedule(1);
        FileImportScheduleInfo info = new FileImportScheduleInfo(schedule, appService, thesaurus, propertyUtils);
        info.importerInfo = new FileImporterInfo();
        info.importerInfo.name = "Test importer";
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

        when(builder.build()).thenReturn(schedule);

        Response response = target("/importservices").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testEditImportSchedule() {
        ImportSchedule schedule = mockImportSchedule(1);

        FileImportScheduleInfo info = new FileImportScheduleInfo(schedule, appService, thesaurus, propertyUtils);
        info.importerInfo = new FileImporterInfo();
        info.name = "New name";
        info.importDirectory = "New folder";
        info.importerInfo.name = "Test importer";
        info.importerInfo.displayName = "Display test importer";
        info.importerInfo.properties = ImmutableList.of();
        Entity<FileImportScheduleInfo> json = Entity.json(info);

        when(fileImportService.getImportSchedule(1)).thenReturn(Optional.of(schedule));

        FileImporterFactory importerFactory = mock(FileImporterFactory.class);
        when(fileImportService.getImportFactory(any(String.class))).thenReturn(Optional.of(importerFactory));
        when(importerFactory.getDestinationName()).thenReturn("Test destination");

        Response response = target("/importservices/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testRemoveImportSchedule() {
        ImportSchedule schedule = mockImportSchedule(1);
        when(fileImportService.getImportSchedule(1)).thenReturn(Optional.of(schedule));

        Response response = target("/importservices/1").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @SuppressWarnings("unchecked")
    private void mockTransaction() {
        when(transactionService.<Object>execute(Matchers.any(Transaction.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                @SuppressWarnings("rawtypes")
                Transaction transaction = (Transaction) invocation.getArguments()[0];
                return transaction.perform();
            }
        });
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

    private void mockImportSchedules(ImportSchedule... importSchedules) {
        when(appService.getImportScheduleAppServers(Matchers.anyLong())).thenReturn(Collections.emptyList());
        Finder<ImportSchedule> finder = mock(Finder.class);
        when(fileImportService.findImportSchedules(Matchers.anyString())).thenReturn(finder);
        when(finder.from(any())).thenReturn(finder);
        when(finder.find()).thenReturn(Arrays.asList(importSchedules));
    }


    private ImportSchedule mockImportSchedule(long id) {
        Path testFolder = mock(Path.class);
        when(testFolder.toString()).thenReturn("//test/path");

        ImportSchedule schedule = mock(ImportSchedule.class);
        when(schedule.getId()).thenReturn(id);
        when(schedule.getName()).thenReturn("Schedule 1");
        when(schedule.getApplicationName()).thenReturn("SYS");
        when(schedule.getImporterName()).thenReturn("Test importer");
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
}
