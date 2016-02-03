package com.elster.partners.jbpm.extension;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJacksonProvider;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jbpm.kie.services.api.RuntimeDataService;
import org.jbpm.services.task.impl.model.UserImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskData;
import org.kie.internal.task.api.InternalTaskService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by dragos on 1/29/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class JbpmTaskResourceTest {
    private static int port;
    private static String baseUri;
    private static TJWSEmbeddedJaxrsServer server;

    @Mock
    @PersistenceUnit(unitName = "org.jbpm.domain")
    EntityManagerFactory emf;

    @Mock
    InternalTaskService internalTaskService;

    @Mock
    RuntimeDataService runtimeDataService;

    @InjectMocks
    private final static JbpmTaskResource taskResource = new JbpmTaskResource();

    @BeforeClass
    public static void beforeClass() throws Exception {
        port = RemoteUtil.findFreePort();

        server = new TJWSEmbeddedJaxrsServer();
        server.setPort(port);
        server.getDeployment().setResources((List) Arrays.asList(taskResource));
        server.start();

        baseUri = "http://localhost:" + port + "/tasks";

        ResteasyProviderFactory instance= ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(instance); instance.registerProvider(ResteasyJacksonProvider.class);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        server.stop();
    }

    // testGetTasks

    @Test
    public void testGetCompletedTask() throws Exception {
        // Given
        Calendar calendar = new GregorianCalendar(2016, 1, 1, 10, 30, 0);

        EntityManager entityManager = mock(EntityManager.class);
        when(emf.createEntityManager()).thenReturn(entityManager);
        Query query = mock(Query.class);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter("taskId", 1)).thenReturn(query);
        Object[] obj = new Object[6];
        obj[0] = new BigDecimal(1);
        obj[1] = "TestUser";
        obj[2] = "TestProcessId";
        obj[3] = new java.sql.Timestamp(calendar.getTime().getTime());
        obj[4] = "InProgress";
        obj[5] = "TestTask";

        List<Object[]> records = new ArrayList<>();
        records.add(obj);
        when(query.getResultList()).thenReturn(records);

        ClientRequest request = new ClientRequest(baseUri + "/1");
        when(internalTaskService.getTaskById(1)).thenReturn(null);

        // When
        ClientResponse<TaskSummary> response = request.get(TaskSummary.class);

        // Then
        assertEquals(1L, response.getEntity().getId());
        assertEquals("TestTask", response.getEntity().getName());
        assertEquals("TestProcessId", response.getEntity().getProcessName());
        assertEquals(calendar.getTime(), response.getEntity().getCreatedOn());
        assertEquals(Status.InProgress, response.getEntity().getStatus());
        assertEquals("TestUser", response.getEntity().getActualOwner());

        // No priority
        // No due date
        // No deployment id
        // No process instance id
    }

    @Test
    public void testGetExistingTask() throws Exception {
        // Given
        Task task = mock(Task.class);
        when(task.getId()).thenReturn(1L);
        when(task.getName()).thenReturn("TestTask");
        when(task.getPriority()).thenReturn(5);

        Calendar calendar = new GregorianCalendar(2016, 1, 1, 10, 30, 0);

        TaskData taskData = mock(TaskData.class);
        when(task.getTaskData()).thenReturn(taskData);
        when(taskData.getProcessId()).thenReturn("TestProcessId");
        when(taskData.getDeploymentId()).thenReturn("TestDeploymentId");
        when(taskData.getProcessInstanceId()).thenReturn(100L);
        when(taskData.getCreatedOn()).thenReturn(calendar.getTime());
        when(taskData.getActualOwner()).thenReturn(new UserImpl("TestUser"));
        when(taskData.getStatus()).thenReturn(Status.InProgress);

        calendar.set(Calendar.DAY_OF_MONTH, 15);
        when(taskData.getExpirationTime()).thenReturn(calendar.getTime());

        ClientRequest request = new ClientRequest(baseUri + "/1");
        when(internalTaskService.getTaskById(1)).thenReturn(task);

        // When
        ClientResponse<TaskSummary> response = request.get(TaskSummary.class);

        // Then
        assertEquals(1L, response.getEntity().getId());
        assertEquals("TestTask", response.getEntity().getName());
        assertEquals("TestProcessId", response.getEntity().getProcessName());
        assertEquals("TestDeploymentId", response.getEntity().getDeploymentId());
        assertEquals(calendar.getTime(), response.getEntity().getDueDate());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        assertEquals(calendar.getTime(), response.getEntity().getCreatedOn());
        assertEquals(5, response.getEntity().getPriority());
        assertEquals(Status.InProgress, response.getEntity().getStatus());
        assertEquals("TestUser", response.getEntity().getActualOwner());
        assertEquals(100L, response.getEntity().getProcessInstanceId());
    }

    // testAssignTask
    // testSetDueDateAndPriority
    // testGetProc
    // testGetRunningProcesses
    // testGetProcessHistory
    // testGetTaskContent
    // testGetProcessForm
    // testGetTaskContents
    // testStartTaskContent
    // testCompleteTaskContent
    // testSaveTaskContent
    // testManageTasks
    //
}
