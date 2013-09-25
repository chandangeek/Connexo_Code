package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImport;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Predicates;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.json.JsonService;
import com.google.common.base.Predicate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImportScheduleJobTest {

    private static final long ID = 354156L;
    private static final String SERIALIZED = "SERIALIZED";
    private ImportScheduleJob importScheduleJob;

    @Mock
    private ImportSchedule importSchedule;
    @Mock
    private CronExpression cronExpression;
    @Mock
    private File importDir;
    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private FileSystem fileSystem;
    @Mock
    private DirectoryStream<Path> directoryStream;
    @Mock
    private Path path;
    @Mock
    private Predicates predicates;
    @Mock
    private Predicate<Path> onlyFiles;
    @Mock
    private TransactionService transactionService;
    @Mock
    private FileImport fileImport;
    @Mock
    private JsonService jsonService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DestinationSpec destination;

    @Before
    public void setUp() {
        when(importSchedule.getScheduleExpression()).thenReturn(cronExpression);
        when(importSchedule.getImportDirectory()).thenReturn(importDir);
        when(serviceLocator.getFileSystem()).thenReturn(fileSystem);
        when(serviceLocator.getPredicates()).thenReturn(predicates);
        when(serviceLocator.getJsonService()).thenReturn(jsonService);
        when(serviceLocator.getTransactionService()).thenReturn(transactionService);
        when(fileSystem.newDirectoryStream(importDir.toPath())).thenReturn(directoryStream);
        when(fileImport.getId()).thenReturn(ID);


        when(transactionService.execute(any(Transaction.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return ((Transaction<?>) invocationOnMock.getArguments()[0]).perform();
            }
        });

        importScheduleJob = new ImportScheduleJob(importSchedule);

        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testKeepsScheduleExpression() {
        assertThat(importScheduleJob.getSchedule()).isEqualTo(cronExpression);
    }

    @Test
    public void testRun() {
        when(directoryStream.iterator()).thenReturn(Arrays.asList(path).iterator());
        when(predicates.onlyFiles()).thenReturn(onlyFiles);
        when(onlyFiles.apply(path)).thenReturn(true);
        when(importSchedule.createFileImport(path.toFile())).thenReturn(fileImport);
        when(jsonService.serialize(any())).thenReturn(SERIALIZED);
        when(importSchedule.getDestination()).thenReturn(destination);

        importScheduleJob.run();

        verify(destination).message(SERIALIZED);

    }

}
