package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultImportScheduleBuilderTest {

    private static final String DESTINATION_NAME = "test_destination";
    private static final File PROCESSING_DIRECTORY = new File("./processing");
    private static final File IMPORT_DIRECTORY = new File("./import");
    private static final File SUCCESS_DIRECTORY = new File("./success");
    private static final File FAILURE_DIRECTORY = new File("./failure");
    @Mock
    private DestinationSpec destination;
    @Mock
    private CronExpression cronExpression;
    @Mock
    private MessageService messageService;
    @Mock
    private FileImportService fileImportService;
    @Mock
    private DataModel dataModel;
    @Mock
    private CronExpressionParser cronParser;
    @Mock
    private FileNameCollisionResolver nameResolver;
    @Mock
    private FileSystem fileSystem;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void setUp() {
        when(dataModel.getInstance(ImportScheduleImpl.class)).thenReturn(new ImportScheduleImpl(dataModel, fileImportService, messageService, cronParser, nameResolver, fileSystem, thesaurus));
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCronExpression() {
        ImportSchedule schedule = new DefaultImportScheduleBuilder(dataModel)
                .setDestination(DESTINATION_NAME)
                .setCronExpression(cronExpression)
                .setProcessingDirectory(PROCESSING_DIRECTORY)
                .setImportDirectory(IMPORT_DIRECTORY)
                .setSuccessDirectory(SUCCESS_DIRECTORY)
                .setFailureDirectory(FAILURE_DIRECTORY)
                .build();

        assertThat(schedule.getScheduleExpression()).isEqualTo(cronExpression);
    }

    @Test
    public void testProcessingDirectory() {
        ImportSchedule schedule = new DefaultImportScheduleBuilder(dataModel)
                .setDestination(DESTINATION_NAME)
                .setCronExpression(cronExpression)
                .setProcessingDirectory(PROCESSING_DIRECTORY)
                .setImportDirectory(IMPORT_DIRECTORY)
                .setSuccessDirectory(SUCCESS_DIRECTORY)
                .setFailureDirectory(FAILURE_DIRECTORY)
                .build();

        assertThat(schedule.getInProcessDirectory()).isEqualTo(PROCESSING_DIRECTORY);
    }

    @Test
    public void testImportDirectory() {
        ImportSchedule schedule = new DefaultImportScheduleBuilder(dataModel)
                .setDestination(DESTINATION_NAME)
                .setCronExpression(cronExpression)
                .setProcessingDirectory(PROCESSING_DIRECTORY)
                .setImportDirectory(IMPORT_DIRECTORY)
                .setSuccessDirectory(SUCCESS_DIRECTORY)
                .setFailureDirectory(FAILURE_DIRECTORY)
                .build();

        assertThat(schedule.getImportDirectory()).isEqualTo(IMPORT_DIRECTORY);
    }

    @Test
    public void testSuccessDirectory() {
        ImportSchedule schedule = new DefaultImportScheduleBuilder(dataModel)
                .setDestination(DESTINATION_NAME)
                .setCronExpression(cronExpression)
                .setProcessingDirectory(PROCESSING_DIRECTORY)
                .setImportDirectory(IMPORT_DIRECTORY)
                .setSuccessDirectory(SUCCESS_DIRECTORY)
                .setFailureDirectory(FAILURE_DIRECTORY)
                .build();

        assertThat(schedule.getSuccessDirectory()).isEqualTo(SUCCESS_DIRECTORY);
    }

    @Test
    public void testFailureDirectory() {
        ImportSchedule schedule = new DefaultImportScheduleBuilder(dataModel)
                .setDestination(DESTINATION_NAME)
                .setCronExpression(cronExpression)
                .setProcessingDirectory(PROCESSING_DIRECTORY)
                .setImportDirectory(IMPORT_DIRECTORY)
                .setSuccessDirectory(SUCCESS_DIRECTORY)
                .setFailureDirectory(FAILURE_DIRECTORY)
                .build();

        assertThat(schedule.getFailureDirectory()).isEqualTo(FAILURE_DIRECTORY);
    }

}
