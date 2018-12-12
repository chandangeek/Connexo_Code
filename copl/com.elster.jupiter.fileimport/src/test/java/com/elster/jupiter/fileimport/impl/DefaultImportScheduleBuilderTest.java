/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.FileImporterFactory;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.util.time.ScheduleExpressionParser;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultImportScheduleBuilderTest {

    private static final String DESTINATION_NAME = "test_destination";
    private Path PROCESSING_DIRECTORY;
    private Path IMPORT_DIRECTORY;
    private Path SUCCESS_DIRECTORY;
    private Path FAILURE_DIRECTORY;
    private Path BASE_PATH;

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
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator validator;
    @Mock
    private EventService eventService;
    private FileSystem testFileSystem;

    @Before
    public void setUp() {
        testFileSystem = Jimfs.newFileSystem(Configuration.unix());
        PROCESSING_DIRECTORY = testFileSystem.getPath("./processing");
        IMPORT_DIRECTORY = testFileSystem.getPath("./import");
        SUCCESS_DIRECTORY = testFileSystem.getPath("./success");
        FAILURE_DIRECTORY = testFileSystem.getPath("./failure");
        BASE_PATH = testFileSystem.getPath("/");
        when(fileImportService.getImportFactory(Matchers.any())).thenReturn(Optional.of(fileImporterFactory));
        when(fileImportService.getBasePath()).thenReturn(BASE_PATH);
        when(fileImporterFactory.getDestinationName()).thenReturn("DEST_1");
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(validator.validate(any(), anyVararg())).thenReturn(Collections.<ConstraintViolation<Object>>emptySet());
        when(dataModel.getInstance(ImportScheduleImpl.class)).thenReturn(
                new ImportScheduleImpl(dataModel, fileImportService, messageService, eventService, scheduleExpressionParser, nameResolver, fileUtils, thesaurus, testFileSystem, Clock
                        .systemDefaultZone()));
    }

    @Test
    public void testCronExpression() {
        ImportSchedule schedule = new DefaultImportScheduleBuilder(dataModel, fileImportService, thesaurus)
                .setDestination(DESTINATION_NAME)
                .setScheduleExpression(scheduleExpression)
                .setProcessingDirectory(PROCESSING_DIRECTORY)
                .setImportDirectory(IMPORT_DIRECTORY)
                .setSuccessDirectory(SUCCESS_DIRECTORY)
                .setFailureDirectory(FAILURE_DIRECTORY)
                .create();

        assertThat(schedule.getScheduleExpression()).isEqualTo(scheduleExpression);
    }

    @Test
    public void testProcessingDirectory() {
        ImportSchedule schedule = new DefaultImportScheduleBuilder(dataModel, fileImportService, thesaurus)
                .setDestination(DESTINATION_NAME)
                .setScheduleExpression(scheduleExpression)
                .setProcessingDirectory(PROCESSING_DIRECTORY)
                .setImportDirectory(IMPORT_DIRECTORY)
                .setSuccessDirectory(SUCCESS_DIRECTORY)
                .setFailureDirectory(FAILURE_DIRECTORY)
                .create();

        assertThat(schedule.getInProcessDirectory()).isEqualTo(PROCESSING_DIRECTORY);
    }

    @Test
    public void testImportDirectory() {
        ImportSchedule schedule = new DefaultImportScheduleBuilder(dataModel, fileImportService, thesaurus)
                .setDestination(DESTINATION_NAME)
                .setScheduleExpression(scheduleExpression)
                .setProcessingDirectory(PROCESSING_DIRECTORY)
                .setImportDirectory(IMPORT_DIRECTORY)
                .setSuccessDirectory(SUCCESS_DIRECTORY)
                .setFailureDirectory(FAILURE_DIRECTORY)
                .create();

        assertThat(schedule.getImportDirectory()).isEqualTo(IMPORT_DIRECTORY);
    }

    @Test
    public void testSuccessDirectory() {
        ImportSchedule schedule = new DefaultImportScheduleBuilder(dataModel, fileImportService, thesaurus)
                .setDestination(DESTINATION_NAME)
                .setScheduleExpression(scheduleExpression)
                .setProcessingDirectory(PROCESSING_DIRECTORY)
                .setImportDirectory(IMPORT_DIRECTORY)
                .setSuccessDirectory(SUCCESS_DIRECTORY)
                .setFailureDirectory(FAILURE_DIRECTORY)
                .create();

        assertThat(schedule.getSuccessDirectory()).isEqualTo(SUCCESS_DIRECTORY);
    }

    @Test
    public void testFailureDirectory() {
        ImportSchedule schedule = new DefaultImportScheduleBuilder(dataModel, fileImportService, thesaurus)
                .setDestination(DESTINATION_NAME)
                .setScheduleExpression(scheduleExpression)
                .setProcessingDirectory(PROCESSING_DIRECTORY)
                .setImportDirectory(IMPORT_DIRECTORY)
                .setSuccessDirectory(SUCCESS_DIRECTORY)
                .setFailureDirectory(FAILURE_DIRECTORY)
                .create();

        assertThat(schedule.getFailureDirectory()).isEqualTo(FAILURE_DIRECTORY);
    }

}
