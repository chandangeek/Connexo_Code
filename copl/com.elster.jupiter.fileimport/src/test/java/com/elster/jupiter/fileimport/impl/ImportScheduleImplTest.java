package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.util.time.ScheduleExpressionParser;
import com.google.inject.matcher.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImportScheduleImplTest {

    private static final String DESTINATION_NAME = "test_destination";
    private ImportScheduleImpl importSchedule;

    //@Mock
    //private DestinationSpec destination;
    @Mock
    private ScheduleExpression scheduleExpression;
    @Mock
    File importDir, inProcessDir, failureDir, successDir;
    @Mock
    private File file;
    @Mock
    private DataMapper<ImportSchedule> importScheduleFactory;
    @Mock
    private MessageService messageService;
    @Mock
    private FileImportService fileImportService;
    @Mock
    private DataModel dataModel;
    @Mock
    private CronExpressionParser cronParser;
    @Mock
    private ScheduleExpressionParser scheduleParser;
    @Mock
    private FileNameCollisionResolver nameResolver;
    @Mock
    private FileSystem fileSystem;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void setUp() {
        when(dataModel.mapper(ImportSchedule.class)).thenReturn(importScheduleFactory);
        when(dataModel.getInstance(ImportScheduleImpl.class)).thenReturn(new ImportScheduleImpl(dataModel, fileImportService, messageService, cronParser, nameResolver, fileSystem, thesaurus));
        when(fileImportService.getImportFactory("importerName")).thenReturn(Optional.empty());
        importSchedule = ImportScheduleImpl.from(dataModel, "TEST_IMPORT_SCHEDULE", false, scheduleExpression, "importerName", DESTINATION_NAME, importDir, ".", inProcessDir, failureDir, successDir);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetImportDirectory() {
        assertThat(importSchedule.getImportDirectory()).isEqualTo(importDir);
    }

    @Test
    public void testGetInProcessDirectory() {
        assertThat(importSchedule.getInProcessDirectory()).isEqualTo(inProcessDir);
    }

    @Test
    public void testGetFailureDirectory() {
        assertThat(importSchedule.getFailureDirectory()).isEqualTo(failureDir);
    }

    @Test
    public void testGetSuccessDirectory() {
        assertThat(importSchedule.getSuccessDirectory()).isEqualTo(successDir);
    }

    @Test
    public void testGetScheduleExpression() {
        assertThat(importSchedule.getScheduleExpression()).isEqualTo(scheduleExpression);
    }

    @Test
    public void testCreateFileImport() {
        when(file.exists()).thenReturn(true);

        FileImportImpl fileImport = importSchedule.createFileImport(file);

        assertThat(fileImport.getImportSchedule()).isEqualTo(importSchedule);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCannotCreateFileImportIfFileDoesNotExist() {
        when(file.exists()).thenReturn(false);

        importSchedule.createFileImport(file);
    }
}
