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
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImportScheduleImplTest {

    private static final String DESTINATION_NAME = "test_destination";
    private static final String FILE_NAME = "fileName";
    private static final String NOT_EXIST_FILE_NAME = "notExistsFileName";
    private ImportScheduleImpl importSchedule;

    //@Mock
    //private DestinationSpec destination;
    @Mock
    private ScheduleExpression scheduleExpression;
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
    private DefaultFileSystem fileSystem;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private FileImporterFactory fileImporterFactory;
    @Mock
    private JsonService jsonService;
    @Mock
    private Clock clock;

    private java.nio.file.FileSystem testFileSystem;

    private Path sourceFilePath;
    private Path notExistFilePath;
    private Path sourceDirectory, inProcessDirectory, successDirectory, failureDirectory, basePath;


    @Before
    public void setUp() throws IOException {
        testFileSystem = Jimfs.newFileSystem(Configuration.windows());

        //when(fileSystem.getInputStream(any(Path.class))).thenReturn(contentsAsStream());
        when(fileSystem.move(any(Path.class), any(Path.class))).thenCallRealMethod();
        when(fileSystem.exists(any(Path.class))).thenCallRealMethod();
        when(fileSystem.newDirectoryStream(any(Path.class), any(String.class))).thenCallRealMethod();

        //fileNameCollisionResolver = new SimpleFileNameCollisionResolver(fileSystem);

        Path root = testFileSystem.getRootDirectories().iterator().next();
        basePath = Files.createDirectory(root.resolve("baseImportPath"));

        inProcessDirectory = testFileSystem.getPath("process");
        failureDirectory = testFileSystem.getPath("failure");
        successDirectory = testFileSystem.getPath("success");
        sourceDirectory = testFileSystem.getPath("source");

        Files.createDirectory(basePath.resolve(inProcessDirectory));
        Files.createDirectory(basePath.resolve(failureDirectory));
        Files.createDirectory(basePath.resolve(successDirectory));
        Files.createDirectory(basePath.resolve(sourceDirectory));

        sourceFilePath = Files.createFile(basePath.resolve(sourceDirectory).resolve(FILE_NAME));

        //Files.createFile(basePath.resolve("source").resolve(NOT_EXIST_FILE_NAME));

        //sourceFilePath = sourceDirectory.resolve(FILE_NAME);
        notExistFilePath = sourceDirectory.resolve(NOT_EXIST_FILE_NAME);

        when(clock.instant()).thenReturn(Instant.now());
        when(dataModel.mapper(ImportSchedule.class)).thenReturn(importScheduleFactory);
        when(fileImportService.getBasePath()).thenReturn(basePath);

        when(fileImportService.getImportFactory(Matchers.any())).thenReturn(Optional.of(fileImporterFactory));
        when(fileImporterFactory.getDestinationName()).thenReturn("DEST_1");
        when(fileImporterFactory.getApplicationName()).thenReturn("SYS");
        when(dataModel.getInstance(ImportScheduleImpl.class)).thenReturn(new ImportScheduleImpl(dataModel, fileImportService,
                messageService, cronParser, nameResolver, fileSystem,jsonService, thesaurus));
        when(fileImportService.getImportFactory("importerName")).thenReturn(Optional.empty());
        importSchedule = ImportScheduleImpl.from(dataModel, "TEST_IMPORT_SCHEDULE", false, scheduleExpression,"SYS","importerName",
                DESTINATION_NAME, sourceDirectory, ".", inProcessDirectory, failureDirectory, successDirectory);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetImportDirectory() {
        assertThat(importSchedule.getImportDirectory()).isEqualTo(sourceDirectory);
    }

    @Test
    public void testGetInProcessDirectory() {
        assertThat(importSchedule.getInProcessDirectory()).isEqualTo(inProcessDirectory);
    }

    @Test
    public void testGetFailureDirectory() {
        assertThat(importSchedule.getFailureDirectory()).isEqualTo(failureDirectory);
    }

    @Test
    public void testGetSuccessDirectory() {
        assertThat(importSchedule.getSuccessDirectory()).isEqualTo(successDirectory);
    }

    @Test
    public void testGetScheduleExpression() {
        assertThat(importSchedule.getScheduleExpression()).isEqualTo(scheduleExpression);
    }

    @Test
    public void testCreateFileImport() {

        FileImportOccurrence fileImport = importSchedule.createFileImportOccurrence(sourceFilePath, clock);
        assertThat(fileImport.getImportSchedule()).isEqualTo(importSchedule);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCannotCreateFileImportIfFileDoesNotExist() {

        importSchedule.createFileImportOccurrence(notExistFilePath, clock);
    }
}
