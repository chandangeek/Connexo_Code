package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImport;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.util.cron.CronExpression;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImportScheduleImplTest {

    private ImportScheduleImpl importSchedule;

    @Mock
    private DestinationSpec destination;
    @Mock
    private CronExpression cronExpression;
    @Mock
    File importDir, inProcessDir, failureDir, successDir;
    @Mock
    private File file;
    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private OrmClient ormClient;
    @Mock
    private DataMapper<ImportSchedule> importScheduleFactory;

    @Before
    public void setUp() {
        when(serviceLocator.getOrmClient()).thenReturn(ormClient);
        when(ormClient.getImportScheduleFactory()).thenReturn(importScheduleFactory);

        Bus.setServiceLocator(serviceLocator);

        importSchedule = new ImportScheduleImpl(cronExpression, destination, importDir, inProcessDir, failureDir, successDir);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testGetDestination() {
        assertThat(importSchedule.getDestination()).isEqualTo(destination);
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
        assertThat(importSchedule.getScheduleExpression()).isEqualTo(cronExpression);
    }

    @Test
    public void testCreateFileImport() {
        when(file.exists()).thenReturn(true);

        FileImport fileImport = importSchedule.createFileImport(file);

        assertThat(fileImport.getImportSchedule()).isEqualTo(importSchedule);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCannotCreateFileImportIfFileDoesNotExist() {
        when(file.exists()).thenReturn(false);

        importSchedule.createFileImport(file);
    }

    @Test
    public void testSave() {
        importSchedule.save();

        verify(importScheduleFactory).persist(importSchedule);
    }

}
