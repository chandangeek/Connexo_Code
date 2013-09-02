package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileImportServiceImplTest {

    private static final File IMPORT_DIRECTORY = new File("/import");
    private static final Date NOW = new Date(10L);
    private static final Date NEXT = new Date(20L);
    private FileImportServiceImpl fileImportService;

    @Mock
    private FileImporter fileImporter;
    @Mock
    private OrmService ormService;
    @Mock
    private DataModel dataModel;
    @Mock
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
    private CronExpression cronExpression;
    @Mock
    private FileSystem fileSystem;
    @Mock
    private DirectoryStream<Path> directoryStream;

    @Before
    public void setUp() {

        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.addTable(anyString())).thenReturn(table);
        when(dataModel.getDataMapper(ImportSchedule.class, ImportScheduleImpl.class, TableSpecs.FIM_IMPORT_SCHEDULE.name())).thenReturn(importScheduleFactory);
        when(importScheduleFactory.get(15L)).thenReturn(Optional.of(importSchedule));

        fileImportService = new FileImportServiceImpl();

        fileImportService.setOrmService(ormService);
        fileImportService.setClock(clock);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testNewBuilder() {
        assertThat(fileImportService.newBuilder()).isNotNull();
    }

    @Test
    public void testCreateMessageHandler() {
        assertThat(fileImportService.createMessageHandler(fileImporter)).isNotNull();
    }

    @Test
    public void testGetImportSchedule() {
        assertThat(fileImportService.getImportSchedule(15L).isPresent()).isTrue();
        assertThat(fileImportService.getImportSchedule(15L).get()).isEqualTo(importSchedule);
    }

    @Test
    public void testSchedule() throws InterruptedException {
        when(importScheduleFactory.find()).thenReturn(Arrays.asList(importSchedule));
        when(importSchedule.getImportDirectory()).thenReturn(IMPORT_DIRECTORY);
        when(importSchedule.getScheduleExpression()).thenReturn(cronExpression);
        when(clock.now()).thenReturn(NOW);
        when(cronExpression.nextAfter(NOW)).thenReturn(NEXT);

        try {
            fileImportService.activate(context);

            // mock the fileSystem
            ServiceLocator serviceLocator = spy(fileImportService);
            when(serviceLocator.getFileSystem()).thenReturn(fileSystem);
            Bus.setServiceLocator(serviceLocator);

            final CountDownLatch requestedDirectoryStream = new CountDownLatch(1);
            when(fileSystem.newDirectoryStream(IMPORT_DIRECTORY.toPath())).thenAnswer(new Answer<DirectoryStream<Path>>() {
                @Override
                public DirectoryStream<Path> answer(InvocationOnMock invocationOnMock) throws Throwable {
                    requestedDirectoryStream.countDown();
                    return directoryStream;
                }
            });

            fileImportService.schedule(importSchedule);

            requestedDirectoryStream.await(1, TimeUnit.SECONDS);

        } finally {
            fileImportService.deactivate();
        }

    }

}
