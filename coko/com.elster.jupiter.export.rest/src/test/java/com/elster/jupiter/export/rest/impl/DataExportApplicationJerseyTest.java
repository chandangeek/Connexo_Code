package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportOccurrenceFinder;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativeField;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.time.Never;
import org.junit.Before;
import org.mockito.Answers;
import org.mockito.Mock;

import javax.ws.rs.core.Application;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class DataExportApplicationJerseyTest extends FelixRestApplicationJerseyTest {
    @Mock
    protected ReadingTypeDataExportTask readingTypeDataExportTask;
    @Mock
    protected RestQueryService restQueryService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    protected DataExportService dataExportService;
    @Mock
    protected DataExportOccurrenceFinder finder;
    @Mock
    protected MeteringService meteringService;
    @Mock
    protected MeteringGroupsService meteringGroupsService;
    @Mock
    protected TimeService timeService;
    @Mock
    protected Query<? extends ReadingTypeDataExportTask> query;
    @Mock
    protected RestQuery<? extends ReadingTypeDataExportTask> restQuery;
    @Mock
    protected EndDeviceGroup endDeviceGroup;
    @Mock
    protected RelativePeriod exportPeriod;
    @Mock
    protected DataExportStrategy strategy;
    @Mock
    protected QueryExecutor<DataExportOccurrence> queryExecutor;
    @Mock
    protected AppService appService;
    protected DataExportTaskBuilder builder = initBuilderStub();

    private DataExportTaskBuilder initBuilderStub() {
        final Object proxyInstance = Proxy.newProxyInstance(DataExportTaskBuilder.class.getClassLoader(), new Class<?>[]{DataExportTaskBuilder.class, DataExportTaskBuilder.PropertyBuilder.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (DataExportTaskBuilder.class.isAssignableFrom(method.getReturnType())) {
                    return builderGetter.get();
                }
                if (DataExportTaskBuilder.PropertyBuilder.class.isAssignableFrom(method.getReturnType())) {
                    return builderGetter.get();
                }
                return taskGetter.get();
            }

            private Supplier<ReadingTypeDataExportTask> taskGetter = () -> readingTypeDataExportTask;
            private Supplier<DataExportTaskBuilder> builderGetter = () -> builder;
        });
        return (DataExportTaskBuilder) proxyInstance;
    }

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return MessageSeeds.values();
    }

    @Override
    protected Application getApplication() {
        when(thesaurus.join(any())).thenReturn(thesaurus);
        DataExportApplication application = new DataExportApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setRestQueryService(restQueryService);
        application.setDataExportService(dataExportService);
        application.setMeteringService(meteringService);
        application.setMeteringGroupsService(meteringGroupsService);
        application.setTimeService(timeService);
        application.setAppService(appService);

        return application;
    }

    @Before
    public void setUpMocks() {
        when(transactionService.execute(any())).thenAnswer(invocation -> ((Transaction<?>) invocation.getArguments()[0]).perform());
        doReturn(query).when(dataExportService).getReadingTypeDataExportTaskQuery();
        doReturn(restQuery).when(restQueryService).wrap(query);
        doReturn(Arrays.asList(readingTypeDataExportTask)).when(restQuery).select(any(), any());
        when(readingTypeDataExportTask.getEndDeviceGroup()).thenReturn(endDeviceGroup);
        when(readingTypeDataExportTask.getExportPeriod()).thenReturn(exportPeriod);
        when(exportPeriod.getRelativeDateFrom()).thenReturn(new RelativeDate(RelativeField.DAY.minus(1)));
        when(exportPeriod.getRelativeDateTo()).thenReturn(new RelativeDate());
        when(readingTypeDataExportTask.getStrategy()).thenReturn(strategy);
        when(readingTypeDataExportTask.getUpdatePeriod()).thenReturn(Optional.of(exportPeriod));
        when(readingTypeDataExportTask.getNextExecution()).thenReturn(DataExportTaskResourceTest.NEXT_EXECUTION.toInstant());
        when(meteringGroupsService.findEndDeviceGroup(5)).thenReturn(Optional.of(endDeviceGroup));
        when(readingTypeDataExportTask.getScheduleExpression()).thenReturn(Never.NEVER);
        when(dataExportService.newBuilder()).thenReturn(builder);
        when(readingTypeDataExportTask.getOccurrencesFinder()).thenReturn(finder);
        when(readingTypeDataExportTask.getName()).thenReturn("Name");
        when(readingTypeDataExportTask.getLastOccurrence()).thenReturn(Optional.empty());
        when(readingTypeDataExportTask.getLastRun()).thenReturn(Optional.<Instant>empty());

        doReturn(Optional.of(readingTypeDataExportTask)).when(dataExportService).findExportTask(DataExportTaskResourceTest.TASK_ID);
        setUpStubs();
    }

    protected void setUpStubs(){
        // for child
    }
}
