package com.elster.partners.jbpm.extension;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJacksonProvider;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jbpm.kie.services.api.RuntimeDataService;
import org.jbpm.kie.services.impl.model.ProcessAssetDesc;
import org.jbpm.kie.services.impl.model.ProcessInstanceDesc;
import org.jbpm.services.task.impl.model.UserImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.task.model.*;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.api.model.InternalTask;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JbpmTaskResourceTest {
    @InjectMocks
    private final static JbpmTaskResource taskResource = new JbpmTaskResource();
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

    @Test
    public  void testGetTaskContent() throws Exception{
        Map<String, Object> content = new HashMap<>();
        content.put("TestContent", "TestContentValue");
        when(internalTaskService.getTaskContent(anyLong())).thenReturn(content);
        ClientRequest request = new ClientRequest(baseUri + "/1/taskcontent");

        ClientResponse<ConnexoForm> response = request.get(ConnexoForm.class);

        assertEquals("TestContentValue", response.getEntity().content.get("TestContent"));
    }

    @Test
    public  void testGetTaskFormContent() throws Exception{
        ConnexoForm form = mock(ConnexoForm.class);
        String template = "\n" +
                "<form id=\"1634463930\">\n" +
                "<property name=\"subject\" value=\"\"/>\n" +
                "<property name=\"name\" value=\"testForm-taskform.form\"/>\n" +
                "<property name=\"displayMode\" value=\"default\"/>\n" +
                "<property name=\"status\" value=\"0\"/>\n" +
                "<field position=\"0\" name=\"542195483\" type=\"InputText\" id=\"542195483\">\n" +
                "<property name=\"readonly\" value=\"true\"/>\n" +
                "</field>\n" +
                "</form>";
        InternalTask task = mock(InternalTask.class);
        TaskData taskData = mock(TaskData.class);
        ProcessAssetDesc processAssetDesc = mock(ProcessAssetDesc.class);
        Collection<ProcessAssetDesc> processesList = new HashSet<>();
        Map<String, String> forms = new HashMap<>();
        forms.put("FormName",template);
        processesList.add(processAssetDesc);

        when(task.getTaskData()).thenReturn(taskData);
        when(taskData.getDeploymentId()).thenReturn("TestDeploymentID");
        when(internalTaskService.getTaskById(anyLong())).thenReturn(task);
        when(runtimeDataService.getProcessesByDeploymentId(anyString())).thenReturn(processesList);
        when(processAssetDesc.getDeploymentId()).thenReturn("TestDeploymentID");
        when(task.getFormName()).thenReturn("FormName");
        when(internalTaskService.getTaskById(anyLong()).getTaskData().getOutputContentId()).thenReturn(-1L);
        when(processAssetDesc.getForms()).thenReturn(forms);

        ClientRequest request = new ClientRequest(baseUri + "/1/content");

        ClientResponse<ConnexoForm> response = request.get(ConnexoForm.class);

        assertEquals("1634463930", response.getEntity().id);
        assertEquals(true, response.getEntity().properties.stream().anyMatch(s->s.value.equals("testForm-taskform.form")));
        assertEquals(true, response.getEntity().fields.stream().anyMatch(s-> s.type.equals("InputText") && s.properties.stream().anyMatch(p->p.name.equals("readonly") && p.value.equals("true"))));
    }

    @Test
    public  void testGetProcessForm() throws Exception{
        ProcessAssetDesc process = mock(ProcessAssetDesc.class);
        String template = "\n" +
                "<form id=\"1634463930\">\n" +
                "<property name=\"subject\" value=\"\"/>\n" +
                "<property name=\"name\" value=\"testForm-taskform.form\"/>\n" +
                "<property name=\"displayMode\" value=\"default\"/>\n" +
                "<property name=\"status\" value=\"0\"/>\n" +
                "<field position=\"0\" name=\"542195483\" type=\"InputText\" id=\"542195483\">\n" +
                "<property name=\"readonly\" value=\"true\"/>\n" +
                "</field>\n" +
                "</form>";
        Collection<ProcessAssetDesc> processesList = new HashSet<>();
        processesList.add(process);
        Map<String, String> forms = new HashMap<>();
        forms.put("processID",template);

        when(process.getDeploymentId()).thenReturn("deploymentID");
        when(runtimeDataService.getProcessById(anyString())).thenReturn(process);
        when(runtimeDataService.getProcessesByDeploymentId(anyString())).thenReturn(processesList);
        when(process.getId()).thenReturn("processID");
        when(process.getForms()).thenReturn(forms);

        ClientRequest request = new ClientRequest(baseUri + "/process/deploymentID/content/processID");

        ClientResponse<ConnexoForm> response = request.get(ConnexoForm.class);
        assertEquals("1634463930", response.getEntity().id);
        assertEquals(true, response.getEntity().properties.stream().anyMatch(s->s.value.equals("testForm-taskform.form")));
        assertEquals(true, response.getEntity().fields.stream().anyMatch(s-> s.type.equals("InputText") && s.properties.stream().anyMatch(p->p.name.equals("readonly") && p.value.equals("true"))));
    }

    @Test
    public  void testGetMandatoryTasks() throws Exception{
        ConnexoForm form = new ConnexoForm();
        List<TaskGroupsInfo> taskGroups = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        ids.add(1L);
        TaskGroupsInfo taskGroupsInfo = new TaskGroupsInfo("name", "processName", "1.0", ids, true, form);
        taskGroups.add(taskGroupsInfo);
        TaskGroupsInfos taskGroupInfos = new TaskGroupsInfos(taskGroups);
        TaskData taskData = mock(TaskData.class);
        InternalTask task = mock(InternalTask.class);
        Collection<ProcessAssetDesc> processesList = new HashSet<>();
        ProcessAssetDesc process = mock(ProcessAssetDesc.class);
        processesList.add(process);
        String template = "\n" +
                "<form id=\"1634463930\">\n" +
                "<property name=\"subject\" value=\"\"/>\n" +
                "<property name=\"name\" value=\"testForm-taskform.form\"/>\n" +
                "<property name=\"displayMode\" value=\"default\"/>\n" +
                "<property name=\"status\" value=\"0\"/>\n" +
                "<field position=\"0\" name=\"542195483\" type=\"InputText\" id=\"542195483\">\n" +
                "<property name=\"readonly\" value=\"true\"/>\n" +
                "<property name=\"fieldRequired\" value=\"true\"/>\n" +
                "</field>\n" +
                "</form>";
        ProcessAssetDesc processAssetDesc = mock(ProcessAssetDesc.class);
        Map<String, String> forms = new HashMap<>();
        forms.put("FormName",template);
        processesList.add(processAssetDesc);

        when(internalTaskService.getTaskById(anyLong())).thenReturn(task);
        when(task.getTaskData()).thenReturn(taskData);
        when(taskData.getDeploymentId()).thenReturn("deploymentID");
        when(runtimeDataService.getProcessesByDeploymentId(anyString())).thenReturn(processesList);
        when(process.getDeploymentId()).thenReturn("deploymentID");
        when(task.getId()).thenReturn(1L);
        when(task.getTaskData()).thenReturn(taskData);
        when(taskData.getDeploymentId()).thenReturn("TestDeploymentID");
        when(taskData.getStatus()).thenReturn(Status.InProgress);
        when(internalTaskService.getTaskById(anyLong())).thenReturn(task);
        when(runtimeDataService.getProcessesByDeploymentId(anyString())).thenReturn(processesList);
        when(processAssetDesc.getDeploymentId()).thenReturn("TestDeploymentID");
        when(task.getFormName()).thenReturn("FormName");
        when(internalTaskService.getTaskById(anyLong()).getTaskData().getOutputContentId()).thenReturn(-1L);
        when(processAssetDesc.getForms()).thenReturn(forms);
        when(task.getName()).thenReturn("TestName");

        ClientRequest request = new ClientRequest(baseUri + "/mandatory");
        request.body(MediaType.APPLICATION_JSON_TYPE, taskGroupInfos);

        ClientResponse<TaskGroupsInfos> response = request.post(TaskGroupsInfos.class);
        assertEquals(true, response.getEntity().taskGroups.get(0).hasMandatory);
        assertEquals(1L, response.getEntity().taskGroups.get(0).count);
        assertEquals(true, response.getEntity().taskGroups.get(0).tasksForm.properties.stream().anyMatch(s->s.value.equals("testForm-taskform.form")));
        assertEquals(true, response.getEntity().taskGroups.get(0).tasksForm.fields.stream().anyMatch(s-> s.type.equals("InputText") && s.properties.stream().anyMatch(p->p.name.equals("readonly") && p.value.equals("true"))));
        assertEquals(true, response.getEntity().taskGroups.get(0).tasksForm.fields.stream().anyMatch(s-> s.type.equals("InputText") && s.properties.stream().anyMatch(p->p.name.equals("fieldRequired") && p.value.equals("true"))));
    }

    @Test
    public  void testAssignTask() throws Exception{
        Task task = mock(Task.class);
        TaskData taskData = mock(TaskData.class);
        PeopleAssignments peopleAssignments = mock(PeopleAssignments.class);
        List<OrganizationalEntity> businessAdministrators = new ArrayList<>();
        OrganizationalEntity org = mock(OrganizationalEntity.class);
        businessAdministrators.add(org);

        when(internalTaskService.getTaskById(anyLong())).thenReturn(task);
        when(task.getTaskData()).thenReturn(taskData);
        when(taskData.getStatus())
                .thenReturn(Status.Created)
                .thenReturn(Status.Ready);
        when(task.getPeopleAssignments()).thenReturn(peopleAssignments);
        when(peopleAssignments.getBusinessAdministrators()).thenReturn(businessAdministrators);
        when(org.getId()).thenReturn("userName");

        ClientRequest request = new ClientRequest(baseUri + "/1/assign");
        request.queryParameter("username", "userName");
        request.queryParameter("currentuser", "currentUser");

        ClientResponse<Response> response = request.post(Response.class);
        assertEquals(200, response.getResponseStatus().getStatusCode());
    }

    @Test
    public  void testSetDueDate() throws Exception{
        Calendar calendar = new GregorianCalendar(2016, 1, 1, 10, 30, 0);

        ClientRequest request = new ClientRequest(baseUri + "/1/set");
        request.queryParameter("priority", "1");
        request.queryParameter("duedate", calendar.getTimeInMillis());

        ClientResponse<Response> response = request.post(Response.class);
        assertEquals(200, response.getResponseStatus().getStatusCode());
    }
}
