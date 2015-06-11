package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.ScheduleExpression;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    private ScheduleExpression scheduleExpression;
    @Mock
    private Path importDir;
    @Mock
    private FileUtils fileSystem;
    @Mock
    private DirectoryStream<Path> directoryStream;
    @Mock
    private Path path;
    @Mock
    private TransactionService transactionService;

    @Mock
    private CronExpressionParser cronExpressionParser;

    @Mock
    private FileImportOccurrence fileImportOccurrence;
    @Mock
    private JsonService jsonService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DestinationSpec destination;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private FileImportService fileImportService;
    @Mock
    private Clock clock;
    @Mock
    private Path basePath;

    @Before
    public void setUp() {
        when(importSchedule.getScheduleExpression()).thenReturn(scheduleExpression);
        when(importSchedule.getImportDirectory()).thenReturn(importDir);
        when(importSchedule.getId()).thenReturn(1L);
        when(fileImportService.getImportSchedule(1L)).thenReturn(Optional.of(importSchedule));
        when(fileImportService.getBasePath()).thenReturn(basePath);
        when(basePath.resolve(importDir)).thenReturn(importDir);
//        when(serviceLocator.getFileSystem()).thenReturn(fileSystem);
//        when(serviceLocator.getPredicates()).thenReturn(predicates);
//        when(serviceLocator.getJsonService()).thenReturn(jsonService);
//        when(serviceLocator.getTransactionService()).thenReturn(transactionService);
                when(fileSystem.newDirectoryStream(importDir, null)).thenReturn(directoryStream);
        when(fileImportOccurrence.getId()).thenReturn(ID);


        when(transactionService.execute(any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return ((Transaction<?>) invocationOnMock.getArguments()[0]).perform();
            }
        });

        importScheduleJob = new ImportScheduleJob(path -> true, fileSystem, jsonService, fileImportService, importSchedule.getId(), transactionService, thesaurus, cronExpressionParser,clock);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testKeepsScheduleExpression() {
        assertThat(importScheduleJob.getSchedule()).isEqualTo(scheduleExpression);
    }

    @Test
    public void testRun() {
        when(directoryStream.spliterator()).thenReturn(Arrays.asList(path).spliterator());
        when(importSchedule.createFileImportOccurrence(path, clock)).thenReturn(fileImportOccurrence);
        when(importSchedule.isActive()).thenReturn(true);
        when(jsonService.serialize(any())).thenReturn(SERIALIZED);
        when(importSchedule.getDestination()).thenReturn(destination);

        importScheduleJob.run();

        verify(destination).message(SERIALIZED);

    }

}
