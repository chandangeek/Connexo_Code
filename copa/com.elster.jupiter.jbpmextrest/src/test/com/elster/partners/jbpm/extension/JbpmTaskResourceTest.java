package com.elster.partners.jbpm.extension;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJacksonProvider;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jbpm.kie.services.api.RuntimeDataService;
import org.jbpm.kie.services.impl.model.ProcessInstanceDesc;
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
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    public void testGetCompletedTask() throws Exception {
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

        ClientResponse<TaskSummary> response = request.get(TaskSummary.class);

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

        ClientResponse<TaskSummary> response = request.get(TaskSummary.class);

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

    @Test
    public void testGetRunningProcesses() throws Exception{
        Calendar calendar = new GregorianCalendar(2016, 1, 1, 10, 30, 0);
        EntityManager em = mock(EntityManager.class);
        when(emf.createEntityManager()).thenReturn(em);
        Query query = mock(Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(query);
        Object[] obj = new Object[7];
        obj[0] = new BigDecimal(1);
        obj[1] = "TestProcessID";
        obj[2] = "TestProcessName";
        obj[3] = "1.0";
        obj[4] = "TestUser";
        obj[5] = new java.sql.Timestamp(calendar.getTime().getTime());
        obj[6] = new BigDecimal(-1);
        List<Object[]> records = new ArrayList<>();
        records.add(obj);
        when(query.getResultList()).thenReturn(records);
        ClientRequest request = new ClientRequest(baseUri + "/runningprocesses");
        request.queryParameter("variableid", "mrid");
        request.queryParameter("variablevalue", "device01");

        ClientResponse<RunningProcessInfos> response = request.get(RunningProcessInfos.class);

        assertEquals(1L, response.getEntity().processInstances.get(0).status);
        assertEquals("TestProcessID", response.getEntity().processInstances.get(0).processId);
        assertEquals("TestProcessName", response.getEntity().processInstances.get(0).processName);
        assertEquals("1.0", response.getEntity().processInstances.get(0).processVersion);
        assertEquals("TestUser", response.getEntity().processInstances.get(0).userIdentity);
        assertEquals(calendar.getTime(), response.getEntity().processInstances.get(0).startDate);
        assertEquals(-1L, response.getEntity().processInstances.get(0).processInstanceId);

    }

    @Test
    public  void testGetHistoryProcesses() throws Exception{
        Calendar calendar = new GregorianCalendar(2016, 1, 1, 10, 30, 0);
        EntityManager em = mock(EntityManager.class);
        when(emf.createEntityManager()).thenReturn(em);
        Query query = mock(Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(query);
        Object[] obj = new Object[8];
        obj[0] = new BigDecimal(1);
        obj[1] = new BigDecimal(1);
        obj[2] = "TestProcessName";
        obj[3] = "1.0";
        obj[4] = "TestUser";
        obj[5] = new java.sql.Timestamp(calendar.getTime().getTime());
        obj[6] = new java.sql.Timestamp(calendar.getTime().getTime());
        obj[7] = new BigDecimal(0);
        List<Object[]> records = new ArrayList<>();
        records.add(obj);
        when(query.getResultList()).thenReturn(records);
        ClientRequest request = new ClientRequest(baseUri + "/process/history");
        request.queryParameter("variableid", "mrid");
        request.queryParameter("variablevalue", "device01");

        ClientResponse<ProcessHistoryInfos> response = request.get(ProcessHistoryInfos.class);

        assertEquals(1L, response.getEntity().processHistories.get(0).status);
        assertEquals(1L, response.getEntity().processHistories.get(0).processInstanceId);
        assertEquals("TestProcessName", response.getEntity().processHistories.get(0).processName);
        assertEquals("1.0", response.getEntity().processHistories.get(0).processVersion);
        assertEquals("TestUser", response.getEntity().processHistories.get(0).userIdentity);
        assertEquals(calendar.getTime(), response.getEntity().processHistories.get(0).startDate);
        assertEquals(calendar.getTime(), response.getEntity().processHistories.get(0).endDate);
        assertEquals(0L, response.getEntity().processHistories.get(0).duration);

    }

    @Test
    public  void testGetNodesAndVariables() throws Exception{
        Calendar calendar = new GregorianCalendar(2016, 1, 1, 10, 30, 0);
        EntityManager em = mock(EntityManager.class);
        ProcessInstanceDesc processInstanceDesc = mock(ProcessInstanceDesc.class);
        when(runtimeDataService.getProcessInstanceById(anyLong())).thenReturn(processInstanceDesc);
        when(processInstanceDesc.getState()).thenReturn(2);
        when(emf.createEntityManager()).thenReturn(em);
        Query query = mock(Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(query);
        Object[] node = new Object[8];
        node[0] = "TestNodeName";
        node[1] = "TestTask";
        node[2] = new java.sql.Timestamp(calendar.getTime().getTime());
        node[4] = "TestNodeID";
        node[5] = new BigDecimal(1);
        node[7] = new BigDecimal(1);
        Object[] variable = new Object[8];
        variable[1] = new java.sql.Timestamp(calendar.getTime().getTime());
        variable[3] = "TestOldValue";
        variable[6] = "TestValue";
        variable[7] = "TestVariableName";
        List<Object[]> nodesRecords = new ArrayList<>();
        List<Object[]> variableRecords = new ArrayList<>();
        nodesRecords.add(node);
        variableRecords.add(variable);
        when(query.getResultList())
                .thenReturn(nodesRecords)
                .thenReturn(variableRecords);
        ClientRequest request = new ClientRequest(baseUri + "/process/instance/1/node");

        ClientResponse<ProcessInstanceNodeInfos> response = request.get(ProcessInstanceNodeInfos.class);

        assertEquals("TestNodeName", response.getEntity().processInstanceNodes.get(0).nodeName);
        assertEquals("TestTask", response.getEntity().processInstanceNodes.get(0).nodeType);
        assertEquals(calendar.getTime(), response.getEntity().processInstanceNodes.get(0).logDate);
        assertEquals(1L, response.getEntity().processInstanceNodes.get(0).nodeInstanceId);
        assertEquals("COMPLETED", response.getEntity().processInstanceNodes.get(0).type);

        assertEquals(1L, response.getEntity().processInstanceVariables.get(0).nodeInstanceId);
        assertEquals(calendar.getTime(), response.getEntity().processInstanceVariables.get(0).logDate);
        assertEquals("TestValue", response.getEntity().processInstanceVariables.get(0).value);
        assertEquals("TestOldValue", response.getEntity().processInstanceVariables.get(0).oldValue);
        assertEquals("TestVariableName", response.getEntity().processInstanceVariables.get(0).variableName);
    }

    // testAssignTask
    // testSetDueDateAndPriority
    // testGetProc
    // testGetTaskContent
    // testGetProcessForm
    // testGetTaskContents
    // testStartTaskContent
    // testCompleteTaskContent
    // testSaveTaskContent
    // testManageTasks
    //
}
