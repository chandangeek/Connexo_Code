package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.util.cron.CronExpression;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DefaultImportScheduleBuilderTest {

    private static final File PROCESSING_DIRECTORY = new File("./processing");
    private static final File IMPORT_DIRECTORY = new File("./import");
    private static final File SUCCESS_DIRECTORY = new File("./success");
    private static final File FAILURE_DIRECTORY = new File("./failure");
    @Mock
    private DestinationSpec destination;
    @Mock
    private CronExpression cronExpression;

    @Test
    public void testDestination() {
        ImportSchedule schedule = new DefaultImportScheduleBuilder()
                .setDestination(destination)
                .setCronExpression(cronExpression)
                .setProcessingDirectory(PROCESSING_DIRECTORY)
                .setImportDirectory(IMPORT_DIRECTORY)
                .setSuccessDirectory(SUCCESS_DIRECTORY)
                .setFailureDirectory(FAILURE_DIRECTORY)
                .build();

        assertThat(schedule.getDestination()).isEqualTo(destination);
    }

    @Test
    public void testCronExpression() {
        ImportSchedule schedule = new DefaultImportScheduleBuilder()
                .setDestination(destination)
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
        ImportSchedule schedule = new DefaultImportScheduleBuilder()
                .setDestination(destination)
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
        ImportSchedule schedule = new DefaultImportScheduleBuilder()
                .setDestination(destination)
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
        ImportSchedule schedule = new DefaultImportScheduleBuilder()
                .setDestination(destination)
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
        ImportSchedule schedule = new DefaultImportScheduleBuilder()
                .setDestination(destination)
                .setCronExpression(cronExpression)
                .setProcessingDirectory(PROCESSING_DIRECTORY)
                .setImportDirectory(IMPORT_DIRECTORY)
                .setSuccessDirectory(SUCCESS_DIRECTORY)
                .setFailureDirectory(FAILURE_DIRECTORY)
                .build();

        assertThat(schedule.getFailureDirectory()).isEqualTo(FAILURE_DIRECTORY);
    }

}
