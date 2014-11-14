package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.export.rest.DataExportTaskInfo;
import com.elster.jupiter.export.rest.DataExportTaskInfos;
import com.elster.jupiter.export.rest.MeterGroupInfo;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativeField;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.time.Never;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataExportTaskResourceTest extends FelixRestApplicationJerseyTest {

    public static final ZonedDateTime NEXT_EXECUTION = ZonedDateTime.of(2015, 1, 13, 0, 0, 0, 0, ZoneId.systemDefault());
    public static final int TASK_ID = 750;
    @Mock
    private RestQueryService restQueryService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataExportService dataExportService;
    private DataExportTaskBuilder builder = initBuilderStub();
    @Mock
    private MeteringService meteringService;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private TimeService timeService;
    @Mock
    private Query<? extends ReadingTypeDataExportTask> query;
    @Mock
    private RestQuery<? extends ReadingTypeDataExportTask> restQuery;
    @Mock
    private ReadingTypeDataExportTask readingTypeDataExportTask;
    @Mock
    private EndDeviceGroup endDeviceGroup;
    @Mock
    private RelativePeriod exportPeriod;
    @Mock
    private DataExportStrategy strategy;

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return MessageSeeds.values();
    }

    @Override
    protected Application getApplication() {
        DataExportApplication application = new DataExportApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setRestQueryService(restQueryService);
        application.setDataExportService(dataExportService);
        application.setMeteringService(meteringService);
        application.setMeteringGroupsService(meteringGroupsService);
        application.setTimeService(timeService);

        return application;
    }

    @Before
    public void setUpMocks() {
        doReturn(query).when(dataExportService).getReadingTypeDataExportTaskQuery();
        doReturn(restQuery).when(restQueryService).wrap(query);
        doReturn(Arrays.asList(readingTypeDataExportTask)).when(restQuery).select(any(), any());
        when(readingTypeDataExportTask.getEndDeviceGroup()).thenReturn(endDeviceGroup);
        when(readingTypeDataExportTask.getExportPeriod()).thenReturn(exportPeriod);
        when(exportPeriod.getRelativeDateFrom()).thenReturn(new RelativeDate(RelativeField.DAY.minus(1)));
        when(exportPeriod.getRelativeDateTo()).thenReturn(new RelativeDate());
        when(readingTypeDataExportTask.getStrategy()).thenReturn(strategy);
        when(readingTypeDataExportTask.getUpdatePeriod()).thenReturn(Optional.of(exportPeriod));
        when(readingTypeDataExportTask.getNextExecution()).thenReturn(NEXT_EXECUTION.toInstant());
        when(meteringGroupsService.findEndDeviceGroup(5)).thenReturn(Optional.of(endDeviceGroup));
        when(readingTypeDataExportTask.getScheduleExpression()).thenReturn(Never.NEVER);
        when(dataExportService.newBuilder()).thenReturn(builder);
        when(readingTypeDataExportTask.getName()).thenReturn("Name");
        when(readingTypeDataExportTask.getLastOccurence()).thenReturn(Optional.empty());

        doReturn(Optional.of(readingTypeDataExportTask)).when(dataExportService).findExportTask(TASK_ID);
    }

    @After
    public void tearDownMocks() {

    }

    @Test
    public void getTasksTest() {
        DataExportTaskInfo info = new DataExportTaskInfo();

        Response response1 = target("/dataexporttask").request().get();
        assertThat(response1.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        DataExportTaskInfos infos = response1.readEntity(DataExportTaskInfos.class);
        assertThat(infos.total).isEqualTo(1);
        assertThat(infos.dataExportTasks).hasSize(1);
    }


    @Test
    public void getCreateTasksTest() {
        DataExportTaskInfo info = new DataExportTaskInfo();
        info.name = "newName";
        info.nextRun = 250L;
        info.deviceGroup = new MeterGroupInfo();
        info.deviceGroup.id = 5;

        Entity<DataExportTaskInfo> json = Entity.json(info);

        Response response = target("/dataexporttask").request().post(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        verify(readingTypeDataExportTask).setName("newName");
        verify(readingTypeDataExportTask).setNextExecution(Instant.ofEpochMilli(250L));
    }

    @Test
    public void updateTasksTest() {
        DataExportTaskInfo info = new DataExportTaskInfo();
        info.id = TASK_ID;
        info.deviceGroup = new MeterGroupInfo();
        info.deviceGroup.id = 5;

        Entity<DataExportTaskInfo> json = Entity.json(info);

        Response response = target("/dataexporttask").request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

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


}
