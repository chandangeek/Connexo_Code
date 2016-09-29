package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportOccurrenceFinder;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.StandardDataSelector;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativeField;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.util.time.Never;

import javax.ws.rs.core.Application;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.Before;
import org.mockito.Answers;
import org.mockito.Mock;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class DataExportApplicationJerseyTest extends FelixRestApplicationJerseyTest {
    @Mock
    protected ExportTask exportTask;
    @Mock
    protected StandardDataSelector standardDataSelector;
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
    protected Query<? extends ExportTask> query;
    @Mock
    protected RestQuery<? extends ExportTask> restQuery;
    @Mock
    protected EndDeviceGroup endDeviceGroup;
    @Mock
    protected RelativePeriod exportPeriod;
    @Mock
    protected DataExportStrategy strategy;
    @Mock
    protected QueryExecutor<DataExportOccurrence> queryExecutor;
    @Mock
    protected PropertyValueInfoService propertyValueInfoService;
    @Mock
    protected AppService appService;
    protected DataExportTaskBuilder builder = initBuilderStub();

    private DataExportTaskBuilder initBuilderStub() {
        final Object proxyInstance = Proxy.newProxyInstance(DataExportTaskBuilder.class.getClassLoader(), new Class<?>[]{DataExportTaskBuilder.class, DataExportTaskBuilder.PropertyBuilder.class, DataExportTaskBuilder.CustomSelectorBuilder.class, DataExportTaskBuilder.EventSelectorBuilder.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (hasReturnType(method, asList(DataExportTaskBuilder.class, DataExportTaskBuilder.PropertyBuilder.class, DataExportTaskBuilder.CustomSelectorBuilder.class, DataExportTaskBuilder.EventSelectorBuilder.class))) {
                    return builderGetter.get();
                }
                return taskGetter.get();
            }

            boolean hasReturnType(Method method, List<Class<?>> classes) {
                return classes.stream().anyMatch(clazz -> clazz.isAssignableFrom(method.getReturnType()));
            }

            private Supplier<ExportTask> taskGetter = () -> exportTask;
            private Supplier<DataExportTaskBuilder> builderGetter = () -> builder;
        });
        return (DataExportTaskBuilder) proxyInstance;
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
        application.setPropertyValueInfoService(propertyValueInfoService);
        return application;
    }

    @Before
    public void setUpMocks() {
        when(transactionService.execute(any())).thenAnswer(invocation -> ((Transaction<?>) invocation.getArguments()[0]).perform());
        doReturn(query).when(dataExportService).getReadingTypeDataExportTaskQuery();
        doReturn(restQuery).when(restQueryService).wrap(query);
        doReturn(asList(exportTask)).when(restQuery).select(any(), any());
        doReturn(Optional.of(standardDataSelector)).when(exportTask).getReadingTypeDataSelector();
        when(standardDataSelector.getEndDeviceGroup()).thenReturn(endDeviceGroup);
        when(standardDataSelector.getExportPeriod()).thenReturn(exportPeriod);
        when(exportPeriod.getRelativeDateFrom()).thenReturn(new RelativeDate(RelativeField.DAY.minus(1)));
        when(exportPeriod.getRelativeDateTo()).thenReturn(new RelativeDate());
        when(standardDataSelector.getStrategy()).thenReturn(strategy);
        when(strategy.getUpdateWindow()).thenReturn(Optional.empty());
        when(standardDataSelector.getStrategy().getUpdatePeriod()).thenReturn(Optional.of(exportPeriod));
        when(exportTask.getNextExecution()).thenReturn(DataExportTaskResourceTest.NEXT_EXECUTION.toInstant());
        when(meteringGroupsService.findEndDeviceGroup(5)).thenReturn(Optional.of(endDeviceGroup));
        when(exportTask.getScheduleExpression()).thenReturn(Never.NEVER);
        when(dataExportService.newBuilder()).thenReturn(builder);
        when(exportTask.getOccurrencesFinder()).thenReturn(finder);
        when(exportTask.getName()).thenReturn("Name");
        when(exportTask.getLastOccurrence()).thenReturn(Optional.empty());
        when(exportTask.getLastRun()).thenReturn(Optional.empty());
        when(exportTask.getVersion()).thenReturn(DataExportTaskResourceTest.OK_VERSION);

        doReturn(Optional.of(exportTask)).when(dataExportService).findExportTask(DataExportTaskResourceTest.TASK_ID);
        doReturn(Optional.of(exportTask)).when(dataExportService).findAndLockExportTask(DataExportTaskResourceTest.TASK_ID, DataExportTaskResourceTest.OK_VERSION);
        doReturn(Optional.empty()).when(dataExportService).findAndLockExportTask(DataExportTaskResourceTest.TASK_ID, DataExportTaskResourceTest.BAD_VERSION);
        setUpStubs();
    }

    protected void setUpStubs() {
        // for child
    }
}
