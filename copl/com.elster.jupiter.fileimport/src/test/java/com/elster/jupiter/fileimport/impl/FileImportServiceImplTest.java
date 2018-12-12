/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.time.ScheduleExpression;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.osgi.framework.BundleContext;

import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileImportServiceImplTest {

    //private static final Path IMPORT_DIRECTORY = Paths.get("/import");
    private static final Instant NOW = Instant.ofEpochMilli(10L);
    private static final Instant NEXT = Instant.ofEpochMilli(20L);
    private FileImportServiceImpl fileImportService;

    @Mock
    private FileImporter fileImporter;
    @Mock
    private OrmService ormService;
    @Mock
    private DataModel dataModel;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Table table;
    @Mock
    private DataMapper<ImportSchedule> importScheduleFactory;
    @Mock
    private ImportSchedule importSchedule;
    @Mock
    private BundleContext context;
    @Mock
    private Clock clock;
    @Mock
    private ScheduleExpression scheduleExpression;
    @Mock
    private FileUtils fileSystem;
    @Mock
    private DirectoryStream<Path> directoryStream;
    @Mock
    private UpgradeService upgradeService;

    private FileSystem testFileSystem;

    @Before
    public void setUp() {

        testFileSystem = Jimfs.newFileSystem(Configuration.windows());
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.addTable(anyString(), any())).thenReturn(table);
        when(dataModel.mapper(ImportSchedule.class)).thenReturn(importScheduleFactory);
        when(importScheduleFactory.getOptional(15L)).thenReturn(Optional.of(importSchedule));
        when(importSchedule.getId()).thenReturn(15L);
        when(clock.instant()).thenReturn(NOW);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        fileImportService = new FileImportServiceImpl();

        fileImportService.setOrmService(ormService);
        fileImportService.setClock(clock);
        fileImportService.setOrmService(ormService);
        fileImportService.setUpgradeService(upgradeService);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testNewBuilder() {
        assertThat(fileImportService.newBuilder()).isNotNull();
    }

    @Test
    public void testNewFileImportHistoryBuilder() {
        assertThat(fileImportService.newFileImportHistoryBuilder()).isNotNull();
    }

    @Test
    public void testCreateMessageHandler() {
        assertThat(fileImportService.createMessageHandler()).isNotNull();
    }

    @Test
    public void testGetImportSchedule() {
        assertThat(fileImportService.getImportSchedule(15L).get()).isEqualTo(importSchedule);
    }

    @Test
    public void testSchedule() throws InterruptedException {
        Path IMPORT_DIRECTORY = testFileSystem.getPath("import");
        when(importScheduleFactory.find()).thenReturn(Arrays.asList(importSchedule));

        when(importSchedule.getImportDirectory()).thenReturn(IMPORT_DIRECTORY);
        when(importSchedule.getScheduleExpression()).thenReturn(scheduleExpression);
        ZonedDateTime now = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault());
        ZonedDateTime next = ZonedDateTime.ofInstant(NEXT, ZoneId.systemDefault());
        when(scheduleExpression.nextOccurrence(now)).thenReturn(Optional.of(next));

        try {
            fileImportService.activate();

            final CountDownLatch requestedDirectoryStream = new CountDownLatch(1);
            when(fileSystem.newDirectoryStream(IMPORT_DIRECTORY,"*.*")).thenAnswer(invocationOnMock -> {
                requestedDirectoryStream.countDown();
                return directoryStream;
            });

            fileImportService.schedule(importSchedule);

            requestedDirectoryStream.await(1, TimeUnit.SECONDS);

        } finally {
            fileImportService.deactivate();
        }

    }

}
