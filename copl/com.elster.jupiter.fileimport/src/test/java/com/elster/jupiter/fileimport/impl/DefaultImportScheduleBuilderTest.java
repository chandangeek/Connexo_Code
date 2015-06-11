package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
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

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultImportScheduleBuilderTest {

    private static final String DESTINATION_NAME = "test_destination";
    private static final Path PROCESSING_DIRECTORY = Paths.get("./processing");
    private static final Path IMPORT_DIRECTORY = Paths.get("./import");
    private static final Path SUCCESS_DIRECTORY = Paths.get("./success");
    private static final Path FAILURE_DIRECTORY = Paths.get("./failure");
    private static final Path BASE_PATH = Paths.get("/");
    @Mock
    private DestinationSpec destination;
    @Mock
    private ScheduleExpression scheduleExpression;
    @Mock
    private MessageService messageService;
    @Mock
    private FileImportService fileImportService;
    @Mock
    private DataModel dataModel;
    @Mock
    private ScheduleExpressionParser scheduleExpressionParser;

    @Mock
    private FileImporterFactory fileImporterFactory;

    @Mock
    private FileNameCollisionResolver nameResolver;
    @Mock
    private FileUtils fileUtils;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private JsonService jsonService;

    private FileSystem testFileSystem;

    @Before
    public void setUp() {
        testFileSystem = Jimfs.newFileSystem(Configuration.windows());
        when(fileImportService.getImportFactory(Matchers.any())).thenReturn(Optional.of(fileImporterFactory));
        when(fileImportService.getBasePath()).thenReturn(BASE_PATH);
        when(fileImporterFactory.getDestinationName()).thenReturn("DEST_1");
        when(dataModel.getInstance(ImportScheduleImpl.class)).thenReturn(
                new ImportScheduleImpl(dataModel, fileImportService, messageService, scheduleExpressionParser, nameResolver, fileUtils,jsonService, thesaurus, testFileSystem));
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCronExpression() {
        ImportSchedule schedule = new DefaultImportScheduleBuilder(dataModel, fileImportService)
                .setDestination(DESTINATION_NAME)
                .setScheduleExpression(scheduleExpression)
                .setProcessingDirectory(PROCESSING_DIRECTORY)
                .setImportDirectory(IMPORT_DIRECTORY)
                .setSuccessDirectory(SUCCESS_DIRECTORY)
                .setFailureDirectory(FAILURE_DIRECTORY)
                .build();

        assertThat(schedule.getScheduleExpression()).isEqualTo(scheduleExpression);
    }

    @Test
    public void testProcessingDirectory() {
        ImportSchedule schedule = new DefaultImportScheduleBuilder(dataModel, fileImportService)
                .setDestination(DESTINATION_NAME)
                .setScheduleExpression(scheduleExpression)
                .setProcessingDirectory(PROCESSING_DIRECTORY)
                .setImportDirectory(IMPORT_DIRECTORY)
                .setSuccessDirectory(SUCCESS_DIRECTORY)
                .setFailureDirectory(FAILURE_DIRECTORY)
                .build();

        assertThat(schedule.getInProcessDirectory()).isEqualTo(PROCESSING_DIRECTORY);
    }

    @Test
    public void testImportDirectory() {
        ImportSchedule schedule = new DefaultImportScheduleBuilder(dataModel, fileImportService)
                .setDestination(DESTINATION_NAME)
                .setScheduleExpression(scheduleExpression)
                .setProcessingDirectory(PROCESSING_DIRECTORY)
                .setImportDirectory(IMPORT_DIRECTORY)
                .setSuccessDirectory(SUCCESS_DIRECTORY)
                .setFailureDirectory(FAILURE_DIRECTORY)
                .build();

        assertThat(schedule.getImportDirectory()).isEqualTo(IMPORT_DIRECTORY);
    }

    @Test
    public void testSuccessDirectory() {
        ImportSchedule schedule = new DefaultImportScheduleBuilder(dataModel, fileImportService)
                .setDestination(DESTINATION_NAME)
                .setScheduleExpression(scheduleExpression)
                .setProcessingDirectory(PROCESSING_DIRECTORY)
                .setImportDirectory(IMPORT_DIRECTORY)
                .setSuccessDirectory(SUCCESS_DIRECTORY)
                .setFailureDirectory(FAILURE_DIRECTORY)
                .build();

        assertThat(schedule.getSuccessDirectory()).isEqualTo(SUCCESS_DIRECTORY);
    }

    @Test
    public void testFailureDirectory() {
        ImportSchedule schedule = new DefaultImportScheduleBuilder(dataModel, fileImportService)
                .setDestination(DESTINATION_NAME)
                .setScheduleExpression(scheduleExpression)
                .setProcessingDirectory(PROCESSING_DIRECTORY)
                .setImportDirectory(IMPORT_DIRECTORY)
                .setSuccessDirectory(SUCCESS_DIRECTORY)
                .setFailureDirectory(FAILURE_DIRECTORY)
                .build();

        assertThat(schedule.getFailureDirectory()).isEqualTo(FAILURE_DIRECTORY);
    }

}
