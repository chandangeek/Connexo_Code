package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.estimation.EstimationTaskBuilder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativeField;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.time.Never;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.*;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EstimationTaskResourceTest extends EstimationApplicationJerseyTest {

    private EstimationTaskBuilder builder = initBuilderStub();
    @Mock
    private EstimationTask estimationTask;
    @Mock
    protected RelativePeriod period;
    @Mock
    private EndDeviceGroup endDeviceGroup;
    @Mock
    private Query<EstimationTask> query;
    @Mock
    private RestQuery<EstimationTask> restQuery;

    private EstimationTaskBuilder initBuilderStub() {
        final Object proxyInstance = Proxy.newProxyInstance(EstimationTaskBuilder.class.getClassLoader(), new Class<?>[]{EstimationTaskBuilder.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (EstimationTaskBuilder.class.isAssignableFrom(method.getReturnType())) {
                    return builderGetter.get();
                }
                return taskGetter.get();
            }

            private Supplier<EstimationTask> taskGetter = () -> estimationTask;
            private Supplier<EstimationTaskBuilder> builderGetter = () -> builder;
        });
        return (EstimationTaskBuilder) proxyInstance;
    }

    public static final ZonedDateTime NEXT_EXECUTION = ZonedDateTime.of(2015, 1, 13, 0, 0, 0, 0, ZoneId.systemDefault());
    public static final int TASK_ID = 750;

    @Before
    public void setUpMocks() {
        doReturn(query).when(estimationService).getEstimationTaskQuery();
        doReturn(restQuery).when(restQueryService).wrap(query);
        doReturn(Collections.singletonList(estimationTask)).when(restQuery).select(any(), anyVararg());
        when(estimationTask.getEndDeviceGroup()).thenReturn(endDeviceGroup);
        when(estimationTask.getPeriod()).thenReturn(Optional.of(period));
        when(period.getRelativeDateFrom()).thenReturn(new RelativeDate(RelativeField.DAY.minus(1)));
        when(period.getRelativeDateTo()).thenReturn(new RelativeDate());
        when(estimationTask.getNextExecution()).thenReturn(NEXT_EXECUTION.toInstant());
        when(meteringGroupsService.findEndDeviceGroup(5)).thenReturn(Optional.of(endDeviceGroup));
        when(estimationTask.getScheduleExpression()).thenReturn(Never.NEVER);
        when(estimationService.newBuilder()).thenReturn(builder);
        when(estimationTask.getName()).thenReturn("Name");
        when(estimationTask.getLastOccurrence()).thenReturn(Optional.empty());
        when(estimationTask.getLastRun()).thenReturn(Optional.<Instant>empty());

        doReturn(Optional.of(estimationTask)).when(estimationService).findEstimationTask(TASK_ID);
        doReturn(Optional.of(estimationTask)).when(estimationService).findAndLockEstimationTask(TASK_ID, 1L);
        doReturn(Optional.empty()).when(estimationService).findAndLockEstimationTask(TASK_ID, 2L);
    }

    @After
    public void after(){
        Mockito.validateMockitoUsage();
    }

    @Test
    public void getTasksTest() {
        Response response1 = target("/estimation/tasks").request().get();
        assertThat(response1.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        EstimationTaskInfos infos = response1.readEntity(EstimationTaskInfos.class);
        assertThat(infos.total).isEqualTo(1);
        assertThat(infos.estimationTasks).hasSize(1);
    }

    @Test
    public void triggerTaskTest() {
        EstimationTaskInfo info = new EstimationTaskInfo();
        info.id = TASK_ID;
        info.deviceGroup = new MeterGroupInfo();
        info.deviceGroup.id = 5;
        info.version = 1L;
        Entity<EstimationTaskInfo> json = Entity.json(info);

        Response response1 = target("/estimation/tasks/"+TASK_ID+"/trigger").request().put(json);
        assertThat(response1.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(estimationTask).triggerNow();
    }


    @Test
    public void getCreateTasksTest() {
        EstimationTaskInfo info = new EstimationTaskInfo();
        info.name = "newName";
        info.nextRun = 250L;
        info.deviceGroup = new MeterGroupInfo();
        info.deviceGroup.id = 5;
        Entity<EstimationTaskInfo> json = Entity.json(info);

        Response response = target("/estimation/tasks").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void updateTasksTest() {
        EstimationTaskInfo info = new EstimationTaskInfo();
        info.id = TASK_ID;
        info.deviceGroup = new MeterGroupInfo();
        info.deviceGroup.id = 5;
        info.version = 1L;
        Entity<EstimationTaskInfo> json = Entity.json(info);

        Response response = target("/estimation/tasks/" + TASK_ID).request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testUpdateTaskConcurrentModification() {
        EstimationTaskInfo info = new EstimationTaskInfo();
        info.id = TASK_ID;
        info.deviceGroup = new MeterGroupInfo();
        info.deviceGroup.id = 5;
        info.version = 2L;
        Entity<EstimationTaskInfo> json = Entity.json(info);

        Response response = target("/estimation/tasks/" + TASK_ID).request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

}