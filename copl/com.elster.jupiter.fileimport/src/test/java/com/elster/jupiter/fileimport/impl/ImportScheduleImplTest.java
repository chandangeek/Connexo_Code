package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.util.time.ScheduleExpressionParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImportScheduleImplTest {

    private static final String DESTINATION_NAME = "test_destination";
    private static final Path BASE_PATH = Paths.get("");
    private ImportScheduleImpl importSchedule;

    //@Mock
    //private DestinationSpec destination;
    @Mock
    private ScheduleExpression scheduleExpression;
    @Mock
    Path importDir, inProcessDir, failureDir, successDir;
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
    @Mock
    private FileImporterFactory fileImporterFactory;
    @Mock
    private JsonService jsonService;
    @Mock
    private Clock clock;

    @Before
    public void setUp() {
        when(clock.instant()).thenReturn(Instant.now());
        when(dataModel.mapper(ImportSchedule.class)).thenReturn(importScheduleFactory);
        when(fileImportService.getBasePath()).thenReturn(BASE_PATH);
        when(fileImportService.getImportFactory(Matchers.any())).thenReturn(Optional.of(fileImporterFactory));
        when(fileImporterFactory.getDestinationName()).thenReturn("DEST_1");
        when(dataModel.getInstance(ImportScheduleImpl.class)).thenReturn(new ImportScheduleImpl(dataModel, fileImportService, messageService, cronParser, nameResolver, fileSystem,jsonService, thesaurus));
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

        FileImportOccurrence fileImport = importSchedule.createFileImportOccurrence(file, clock);

        assertThat(fileImport.getImportSchedule()).isEqualTo(importSchedule);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCannotCreateFileImportIfFileDoesNotExist() {
        when(file.exists()).thenReturn(false);

        importSchedule.createFileImportOccurrence(file, clock);
    }
}
