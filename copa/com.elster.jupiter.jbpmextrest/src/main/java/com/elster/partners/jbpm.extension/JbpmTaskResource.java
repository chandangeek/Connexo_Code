package com.elster.partners.jbpm.extension;


import com.google.common.base.Strings;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.model.UserTaskInstanceDesc;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.kie.api.runtime.query.QueryContext;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.remote.services.cdi.ProcessRequestBean;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/tasks")
public class JbpmTaskResource {

    private static final String PROPERTY = "property";

    @Inject
    @PersistenceUnit(unitName = "org.jbpm.domain")
    private EntityManagerFactory emf;

    @Inject
    TaskService taskService;

    @Inject
    protected ProcessRequestBean processRequestBean;

    @Inject
    RuntimeDataService runtimeDataService;

    @Inject
    DeploymentService deploymentService;

    @Inject
    FormManagerService formManagerService;


    @POST
    @Produces("application/json")
    public TaskSummaryList getTasks(ProcessDefinitionInfos processDefinitionInfos, @Context UriInfo uriInfo){
        Map<String, JsonNode> filterProperties;
        Map<String, JsonNode> sortProperties;
        filterProperties = getFilterProperties(getQueryValue(uriInfo,"filter"),"value");
        sortProperties = getFilterProperties(getQueryValue(uriInfo,"sort"),"direction");
        List<String> deploymentIds = new ArrayList<>();
        List<String> processIds = new ArrayList<>();
        for(ProcessDefinitionInfo processDefinitionInfo : processDefinitionInfos.processes){
            deploymentIds.add(processDefinitionInfo.deploymentId);
            processIds.add(processDefinitionInfo.processId);
        }
        int startIndex = 0;
        int endIndex = Integer.MAX_VALUE;
        try {
            startIndex = Integer.valueOf(getQueryValue(uriInfo, "start"));
            endIndex = Integer.valueOf(getQueryValue(uriInfo, "limit"));
            endIndex++;
        }catch (NumberFormatException e){
        }
        if(deploymentIds != null && processIds != null && !deploymentIds.isEmpty() && !processIds.isEmpty()) {
            if (emf != null) {
                EntityManager em = emf.createEntityManager();
                CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

                final CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(TaskSummary.class);
                final Root taskRoot = criteriaQuery.from(TaskImpl.class);
                if (!filterProperties.isEmpty()) {
                    List<Predicate> predicatesUser = new ArrayList<Predicate>();
                    List<Predicate> predicatesDueDate = new ArrayList<Predicate>();
                    List<Predicate> predicatesProcess = new ArrayList<Predicate>();
                    List<Predicate> predicatesStatus = new ArrayList<Predicate>();
                    List<Predicate> predicatesDeploymentId = new ArrayList<Predicate>();
                    Iterator<String> it = filterProperties.keySet().iterator();
                    while (it.hasNext()) {
                        String theKey = (String) it.next();
                        if (theKey.equals("status")) {
                            for (int i = 0; i < filterProperties.get("status").size(); i++) {
                                if (filterProperties.get("status").get(i).toString().contains("OPEN")) {
                                    predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get(theKey), Status.Created));
                                    predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get(theKey), Status.Ready));
                                    predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get(theKey), Status.Reserved));
                                    predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get(theKey), Status.Suspended));
                                }
                                if (filterProperties.get("status").get(i).toString().contains("INPROGRESS")) {
                                    predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get(theKey), Status.InProgress));
                                }
                                if (filterProperties.get("status").get(i).toString().contains("COMPLETED")) {
                                    predicatesStatus.add(criteriaBuilder.notEqual(taskRoot.get("taskData").get("status"), Status.Completed));
                                }
                                if (filterProperties.get("status").get(i).toString().contains("FAILED")) {
                                    predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get(theKey), Status.Failed));
                                    predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get(theKey), Status.Exited));
                                    predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get(theKey), Status.Obsolete));
                                }
                            }
                        }
                        if (theKey.equals("user")) {
                            for (int i = 0; i < filterProperties.get("user").size(); i++) {
                                if (filterProperties.get("user").get(i).toString().replace("\"", "").equals("Unassigned")) {
                                    predicatesUser.add(criteriaBuilder.and(taskRoot.get("taskData").get("actualOwner").isNull()));
                                } else {
                                    predicatesUser.add(criteriaBuilder.equal(taskRoot.get("taskData").get("actualOwner").get("id"), filterProperties.get("user").get(i).toString().replace("\"", "")));
                                }
                            }
                        }
                        if (theKey.equals("dueDate")) {
                            for (int i = 0; i < filterProperties.get("dueDate").size(); i++) {
                                if (filterProperties.get("dueDate").get(i).toString().replace("\"", "").equals("OVERDUE")) {
                                    Calendar cal = Calendar.getInstance();
                                    cal.add(Calendar.DATE, -1);
                                    cal.set(Calendar.HOUR, 11);
                                    cal.set(Calendar.MINUTE, 59);
                                    cal.set(Calendar.SECOND, 59);
                                    cal.set(Calendar.AM_PM, Calendar.PM);
                                    predicatesDueDate.add(criteriaBuilder.lessThanOrEqualTo(taskRoot.<Date>get("taskData").get("expirationTime"), cal.getTime()));
                                }
                                if (filterProperties.get("dueDate").get(i).toString().replace("\"", "").equals("TODAY")) {
                                    Calendar cal = Calendar.getInstance();
                                    cal.add(Calendar.DATE, -1);
                                    cal.set(Calendar.HOUR, 11);
                                    cal.set(Calendar.MINUTE, 59);
                                    cal.set(Calendar.SECOND, 59);
                                    cal.set(Calendar.AM_PM, Calendar.PM);
                                    Calendar cal2 = Calendar.getInstance();
                                    cal2.add(Calendar.DATE, 0);
                                    cal2.set(Calendar.HOUR, 11);
                                    cal2.set(Calendar.MINUTE, 59);
                                    cal2.set(Calendar.SECOND, 59);
                                    cal2.set(Calendar.AM_PM, Calendar.PM);
                                    predicatesDueDate.add(criteriaBuilder.between(taskRoot.<Date>get("taskData").get("expirationTime"), cal.getTime(), cal2.getTime()));
                                }
                                if (filterProperties.get("dueDate").get(i).toString().replace("\"", "").equals("UPCOMING")) {
                                    Calendar cal = Calendar.getInstance();
                                    cal.add(Calendar.DATE, 0);
                                    cal.set(Calendar.HOUR, 11);
                                    cal.set(Calendar.MINUTE, 59);
                                    cal.set(Calendar.SECOND, 59);
                                    cal.set(Calendar.AM_PM, Calendar.PM);
                                    predicatesDueDate.add(criteriaBuilder.greaterThanOrEqualTo(taskRoot.<Date>get("taskData").get("expirationTime"), cal.getTime()));
                                }
                            }
                        }
                        if (theKey.equals("process")) {
                            for (int i = 0; i < filterProperties.get("process").size(); i++) {
                                String[] processItems = filterProperties.get("process")
                                        .get(i)
                                        .toString()
                                        .replace("\"", "")
                                        .split(" \\(");
                                if (processItems.length == 3) {
                                    String processId = processItems[0];
                                    String deploymentId = processItems[2].replace(")", "");
                                    predicatesProcess.add(criteriaBuilder.equal(taskRoot.get("taskData").get("processId"), processId));
                                    predicatesDeploymentId.add(criteriaBuilder.equal(taskRoot.get("taskData").get("deploymentId"), deploymentId));
                                }
                            }
                        }else{
                            for (String each : deploymentIds) {
                                predicatesDeploymentId.add(criteriaBuilder.equal(taskRoot.get("taskData").get("deploymentId"), each));
                            }
                        }
                    }

                    List<Predicate> predicateList = new ArrayList<Predicate>();
                    Predicate p1 = criteriaBuilder.disjunction();
                    if (!predicatesStatus.isEmpty()) {
                        p1 = criteriaBuilder.or(predicatesStatus.toArray(new Predicate[predicatesStatus.size()]));
                        predicateList.add(p1);
                    } else {
                        predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get("status"), Status.InProgress));
                        predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get("status"), Status.Created));
                        predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get("status"), Status.Ready));
                        predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get("status"), Status.Reserved));
                    }

                    Predicate p2 = criteriaBuilder.disjunction();
                    if (!predicatesUser.isEmpty()) {
                        p2 = criteriaBuilder.or(predicatesUser.toArray(new Predicate[predicatesUser.size()]));
                        predicateList.add(p2);
                    }

                    Predicate p3 = criteriaBuilder.disjunction();
                    if (!predicatesDueDate.isEmpty()) {
                        p3 = criteriaBuilder.or(predicatesDueDate.toArray(new Predicate[predicatesDueDate.size()]));
                        predicateList.add(p3);
                    }

                    Predicate p4 = criteriaBuilder.disjunction();
                    if (!predicatesProcess.isEmpty()) {
                        p4 = criteriaBuilder.or(predicatesProcess.toArray(new Predicate[predicatesProcess.size()]));
                        predicateList.add(p4);

                    }
                    Predicate p5 = criteriaBuilder.disjunction();
                    if (!predicatesDeploymentId.isEmpty()) {
                        p5 = criteriaBuilder.or(predicatesDeploymentId.toArray(new Predicate[predicatesDeploymentId.size()]));
                        predicateList.add(p5);
                    }
                    criteriaQuery.where(criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()])));
                } else {
                    List<Predicate> predicateList = new ArrayList<Predicate>();
                    List<Predicate> predicatesStatus = new ArrayList<>();
                    List<Predicate> predicatesDeploymentId = new ArrayList<>();
                    List<Predicate> predicatesProcessId = new ArrayList<>();
                    for (String each : deploymentIds) {
                        predicatesDeploymentId.add(criteriaBuilder.equal(taskRoot.get("taskData").get("deploymentId"), each));
                    }
                    for (String each : processIds) {
                        predicatesProcessId.add(criteriaBuilder.equal(taskRoot.get("taskData").get("processId"), each));
                    }
                    predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get("status"), Status.InProgress));
                    predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get("status"), Status.Created));
                    predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get("status"), Status.Ready));
                    predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get("status"), Status.Reserved));
                    Predicate p1 = criteriaBuilder.disjunction();
                    if (!predicatesDeploymentId.isEmpty()) {
                        p1 = criteriaBuilder.or(predicatesDeploymentId.toArray(new Predicate[predicatesDeploymentId.size()]));
                        predicateList.add(p1);
                    }
                    Predicate p2 = criteriaBuilder.disjunction();
                    if(!predicatesProcessId.isEmpty()){
                        p2 = criteriaBuilder.or(predicatesProcessId.toArray(new Predicate[predicatesProcessId.size()]));
                    }
                    p1 = criteriaBuilder.or(predicatesStatus.toArray(new Predicate[predicatesStatus.size()]));
                    predicateList.add(p1);
                    predicateList.add(p2);
                    criteriaQuery.where(criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()])));
                }
                criteriaQuery.select(criteriaBuilder.construct(TaskSummary.class,
                        taskRoot.get("id"),
                        taskRoot.get("name"),
                        taskRoot.get("taskData").get("processId"),
                        taskRoot.get("taskData").get("deploymentId"),
                        taskRoot.get("taskData").get("expirationTime"),
                        taskRoot.get("taskData").get("createdOn"),
                        taskRoot.get("priority"),
                        taskRoot.get("taskData").get("status"),
                        taskRoot.get("taskData").get("actualOwner").get("id"),
                        taskRoot.get("taskData").get("processInstanceId")
                ));

                if (!sortProperties.isEmpty()) {
                    List<Order> orders = new ArrayList<Order>();
                    Iterator<String> it = sortProperties.keySet().iterator();
                    while (it.hasNext()) {
                        String theKey = (String) it.next();
                        if (theKey.equals("dueDate")) {
                            if (sortProperties.get("dueDate").toString().replace("\"", "").equals("asc")) {
                                orders.add(criteriaBuilder.asc(taskRoot.get("taskData").get("expirationTime")));
                            } else {
                                orders.add(criteriaBuilder.desc(taskRoot.get("taskData").get("expirationTime")));
                            }
                        }
                        if (theKey.equals("creationDate")) {
                            if (sortProperties.get("creationDate").toString().replace("\"", "").equals("asc")) {
                                orders.add(criteriaBuilder.asc(taskRoot.get("taskData").get("createdOn")));
                            } else {
                                orders.add(criteriaBuilder.desc(taskRoot.get("taskData").get("createdOn")));
                            }
                        }
                        if (theKey.equals("priority")) {
                            if (sortProperties.get("priority").toString().replace("\"", "").equals("asc")) {
                                orders.add(criteriaBuilder.desc(taskRoot.get("priority")));
                            } else {
                                orders.add(criteriaBuilder.asc(taskRoot.get("priority")));
                            }
                        }
                    }
                    criteriaQuery.orderBy(orders);
                } else {
                    criteriaQuery.orderBy(criteriaBuilder.asc(taskRoot.get("name")));
                }

                final TypedQuery query = em.createQuery(criteriaQuery);

                query.setFirstResult(startIndex);
                query.setMaxResults(endIndex);

                TaskSummaryList taskSummaryList = new TaskSummaryList(runtimeDataService, query.getResultList());
                if (taskSummaryList.getTotal() == endIndex) {
                    int total = startIndex + endIndex;
                    taskSummaryList.removeLast(total);
                } else {
                    int total = startIndex + taskSummaryList.getTotal();
                    taskSummaryList.setTotal(total);
                }
                return taskSummaryList;
            }
        }

        return new TaskSummaryList(runtimeDataService, new ArrayList<>());
    }

    @POST
    @Path("/{taskId: [0-9-]+}/")
    @Produces("application/json")
    public TaskSummary getTask(ProcessDefinitionInfos processDefinitionInfos, @PathParam("taskId") long taskid){
        Task task = taskService.getTaskById(taskid);
        if(task == null){
            return new TaskSummary(getAuditTask(taskid));
        }else {
            for(ProcessDefinitionInfo processDefinitionInfo : processDefinitionInfos.processes){
                if(processDefinitionInfo.deploymentId.equals(task.getTaskData().getDeploymentId()) && processDefinitionInfo.processId.equals(task.getTaskData().getProcessId())){
                    return new TaskSummary(task);
                }
            }
            return new TaskSummary();
        }
    }

    @POST
    @Path("/{taskId: [0-9-]+}/{optLock: [0-9-]+}/assign")
    public Response assignTask(@Context UriInfo uriInfo, @PathParam("taskId") long taskId, @PathParam("optLock") long optLock){
        String userName = getQueryValue(uriInfo, "username");
        String currentuser = getQueryValue(uriInfo, "currentuser");
        String priority = getQueryValue(uriInfo, "priority");
        String date = getQueryValue(uriInfo, "duedate");
        Task task = taskService.getTaskById(taskId);
        if(task != null) {
            if(((TaskImpl) task).getVersion() == optLock) {
                if(userName != null && currentuser != null) {
                    boolean check = assignTaskToUser(userName, currentuser, taskId);
                    if (!check) {
                        return Response.status(403).build();
                    }
                }
                if(priority != null || date != null){
                    if (priority != null && !priority.equals("")) {
                        setPriority(Integer.valueOf(priority), taskId);
                    }
                    if (date != null && !date.equals("")) {
                        Date millis = new Date();
                        millis.setTime(Long.valueOf(date));
                        setDueDate(millis, taskId);
                    }
                }
            } else {
                return Response.status(409).entity(task.getName()).build();
            }
        }
        return Response.ok().build();
    }

    @GET
    @Path("/proc")
    @Produces("application/json")
    public ProcessInstanceInfos getProc(@Context UriInfo uriInfo){
        String variableId = getQueryValue(uriInfo, "variableid");
        String variableValue = getQueryValue(uriInfo, "variablevalue");
        if(variableId != null && variableValue != null) {
            EntityManager em = emf.createEntityManager();
            String queryString = "select p.STATUS, p.PROCESSINSTANCEID as processLogid, p.PROCESSID, p.PROCESSNAME, p.EXTERNALID, p.PROCESSVERSION, " +
                    "p.USER_IDENTITY, p.START_DATE, p.END_DATE, p.DURATION, " +
                    "p.PARENTPROCESSINSTANCEID, v.PROCESSINSTANCEID as variableProcessId, v.LOG_DATE, v.VARIABLEID, v.OLDVALUE " +
                    "from processinstancelog p " +
                    "LEFT JOIN VARIABLEINSTANCELOG v ON p.PROCESSINSTANCEID = v.PROCESSINSTANCEID " +
                    "where v.VARIABLEID = :variableid and v.VALUE = :variablevalue " +
                    "order by upper(p.PROCESSNAME)";
            Query query = em.createNativeQuery(queryString);
            query.setParameter("variableid", variableId);
            query.setParameter("variablevalue", variableValue);
            List<Object[]> list = query.getResultList();
            return new ProcessInstanceInfos(list);
        }
        return null;
    }

    @GET
    @Path("/runningprocesses")
    @Produces("application/json")
    public RunningProcessInfos getRunningProcesses(@Context UriInfo uriInfo){
        String variableId = getQueryValue(uriInfo, "variableid");
        String variableValue = getQueryValue(uriInfo, "variablevalue");
        int startIndex = 0;
        int endIndex = Integer.MAX_VALUE;
        try {
            startIndex = Integer.valueOf(getQueryValue(uriInfo, "start"));
            endIndex = Integer.valueOf(getQueryValue(uriInfo, "limit"));
            endIndex++;
        }catch (NumberFormatException e){
        }
        if(variableId != null && variableValue != null) {
            EntityManager em = emf.createEntityManager();
            String queryString = "select p.STATUS, p.PROCESSID, p.PROCESSNAME, p.PROCESSVERSION, " +
                    "p.USER_IDENTITY, p.START_DATE, p.PROCESSINSTANCEID as processLogid " +
                    "from processinstancelog p " +
                    "LEFT JOIN VARIABLEINSTANCELOG v ON p.PROCESSINSTANCEID = v.PROCESSINSTANCEID " +
                    "where UPPER (v.VARIABLEID) = UPPER (:variableid) and UPPER (v.VALUE) = UPPER (:variablevalue) " +
                    "and p.STATUS = 1 OR p.STATUS = 0" +
                    "order by p.START_DATE";
            Query query = em.createNativeQuery(queryString);
            query.setParameter("variableid", variableId);
            query.setParameter("variablevalue", variableValue);
            query.setFirstResult(startIndex);
            query.setMaxResults(endIndex);
            List<Object[]> list = query.getResultList();
            RunningProcessInfos runningProcessInfos = new RunningProcessInfos(list);
            for(RunningProcessInfo info : runningProcessInfos.processInstances){
                info.tasks = info.processInstanceId == -1 ? null : getTaskForProceessInstance(info.processInstanceId);
            }
            if(runningProcessInfos.total == endIndex){
                int total = startIndex + endIndex;
                runningProcessInfos.removeLast(total);
            }else{
                int total = startIndex + runningProcessInfos.total;
                runningProcessInfos.setTotal(total);
            }
            return runningProcessInfos;
        }
        return null;
    }

    @GET
    @Path("/allprocesses")
    @Produces("application/json")
    public RunningProcessInfos getAllProcesses(@Context UriInfo uriInfo){
        String variableId = getQueryValue(uriInfo, "variableid");
        String variableValue = getQueryValue(uriInfo, "variablevalue");
        int startIndex = 0;
        int endIndex = Integer.MAX_VALUE;
        try {
            startIndex = Integer.valueOf(getQueryValue(uriInfo, "start"));
            endIndex = Integer.valueOf(getQueryValue(uriInfo, "limit"));
            endIndex++;
        }catch (NumberFormatException e){
        }
        if(variableId != null && variableValue != null) {
            EntityManager em = emf.createEntityManager();
            String queryString = "select p.STATUS, p.PROCESSID, p.PROCESSNAME, p.PROCESSVERSION, " +
                    "p.USER_IDENTITY, p.START_DATE, p.PROCESSINSTANCEID as processLogid " +
                    "from processinstancelog p " +
                    "LEFT JOIN VARIABLEINSTANCELOG v ON p.PROCESSINSTANCEID = v.PROCESSINSTANCEID " +
                    "where UPPER (v.VARIABLEID) = UPPER (:variableid) and UPPER (v.VALUE) = UPPER (:variablevalue) " +
                    "order by p.START_DATE";
            Query query = em.createNativeQuery(queryString);
            query.setParameter("variableid", variableId);
            query.setParameter("variablevalue", variableValue);
            query.setFirstResult(startIndex);
            query.setMaxResults(endIndex);
            List<Object[]> list = query.getResultList();
            RunningProcessInfos runningProcessInfos = new RunningProcessInfos(list);
            for(RunningProcessInfo info : runningProcessInfos.processInstances){
                info.tasks = info.processInstanceId == -1 ? null : getTaskForProceessInstance(info.processInstanceId);
            }
            if(runningProcessInfos.total == endIndex){
                int total = startIndex + endIndex;
                runningProcessInfos.removeLast(total);
            }else{
                int total = startIndex + runningProcessInfos.total;
                runningProcessInfos.setTotal(total);
            }
            return runningProcessInfos;
        }
        return null;
    }

    @GET
    @Path("/process/instance/{processInstanceId: [0-9-]+}/node")
    @Produces("application/json")
    public ProcessInstanceNodeInfos getProcessInstanceNode(@Context UriInfo uriInfo,@PathParam("processInstanceId") long processInstanceId){
        String processInstanceState = "";
        ProcessInstanceDesc processDesc = runtimeDataService.getProcessInstanceById(processInstanceId);
        if (processDesc != null) {
            if (processDesc.getState() == 1) {
                processInstanceState = "Active";
            } else if (runtimeDataService.getProcessInstanceById(processInstanceId).getState() == 2) {
                processInstanceState = "Completed";
            } else {
                processInstanceState = "Aborted";
            }
        }else {
            processInstanceState = "Undeployed";
        }
        EntityManager em = emf.createEntityManager();
        String queryString = "select * from(select n.NODENAME, n.NODETYPE, n.log_date,n.NODEINSTANCEID, n.NODEID, n.ID from NODEINSTANCELOG n " +
                "where n.PROCESSINSTANCEID = :processInstanceId and type= (select min(type) from NODEINSTANCELOG where processinstanceid = :processInstanceId and nodeid = n.nodeid))t1 " +
                "join(select p.NODEID as NODEIDNOTUSED, max(p.TYPE) from NODEINSTANCELOG p where processinstanceid = :processInstanceId group by p.NODEID)t2 on t1.nodeid = t2.NODEIDNOTUSED order by t1.ID desc";
        Query query = em.createNativeQuery(queryString);
        query.setParameter("processInstanceId", processInstanceId);
        List<Object[]> nodes = query.getResultList();
        queryString = "select * from variableinstancelog v WHERE v.PROCESSINSTANCEID = :processInstanceId ORDER BY v.VARIABLEID ASC";
        query = em.createNativeQuery(queryString);
        query.setParameter("processInstanceId", processInstanceId);
        List<Object[]> processVariables = query.getResultList();
        return new ProcessInstanceNodeInfos(nodes, processInstanceState, processVariables);
    }

    @GET
    @Path("/process/history")
    @Produces("application/json")
    public ProcessHistoryInfos getProcessHistory(@Context UriInfo uriInfo){
        String variableId = getQueryValue(uriInfo, "variableid");
        String variableValue = getQueryValue(uriInfo, "variablevalue");
        Map<String, JsonNode> filterProperties;
        filterProperties = getFilterProperties(getQueryValue(uriInfo,"filter"),"value");
        int startIndex = 0;
        int endIndex = Integer.MAX_VALUE;
        try {
            startIndex = Integer.valueOf(getQueryValue(uriInfo, "start"));
            endIndex = Integer.valueOf(getQueryValue(uriInfo, "limit"));
            endIndex++;
        }catch (NumberFormatException e){
        }
        if(variableId != null && variableValue != null) {
            EntityManager em = emf.createEntityManager();
            String queryString = "select p.STATUS, p.PROCESSINSTANCEID as processLogid, p.PROCESSNAME, p.PROCESSVERSION, p.USER_IDENTITY, p.START_DATE, p.END_DATE, p.DURATION " +
                    "from processinstancelog p " +
                    "LEFT JOIN VARIABLEINSTANCELOG v ON p.PROCESSINSTANCEID = v.PROCESSINSTANCEID " +
                    "where UPPER (v.VARIABLEID) = UPPER (:variableid) and UPPER (v.VALUE) = UPPER (:variablevalue) ";
            queryString += addFilterToQuery(filterProperties);
            Query query = em.createNativeQuery(queryString);
            query.setParameter("variableid", variableId);
            query.setParameter("variablevalue", variableValue);
            query.setFirstResult(startIndex);
            query.setMaxResults(endIndex);
            List<Object[]> list = query.getResultList();
            ProcessHistoryInfos processHistoryInfos = new ProcessHistoryInfos(list);
            if(processHistoryInfos.total == endIndex){
                int total = startIndex + endIndex;
                processHistoryInfos.removeLast(total);
            }else{
                int total = startIndex + processHistoryInfos.total;
                processHistoryInfos.setTotal(total);
            }
            return processHistoryInfos;
        }
        return null;
    }

    @GET
    @Produces("application/json")
    @Path("/{taskId: [0-9-]+}/content")
    public ConnexoForm getTaskContent(@PathParam("taskId") long taskId) {
        ConnexoForm form = new ConnexoForm();
        if (taskService != null && formManagerService != null) {
            Task task = taskService.getTaskById(taskId);
            if (task != null) {
                String template = formManagerService.getFormByKey(task.getTaskData()
                        .getDeploymentId(), getTaskFormName(task));
                if (Strings.isNullOrEmpty(template)) {
                    template = formManagerService.getFormByKey(task.getTaskData()
                            .getDeploymentId(), getTaskFormName(task) + "-taskform");
                }
                if (Strings.isNullOrEmpty(template)) {
                    template = formManagerService.getFormByKey(task.getTaskData()
                            .getDeploymentId(), getTaskFormName(task) + "-taskform.form");
                }

                if (!Strings.isNullOrEmpty(template)) {
                    try {
                        JAXBContext jc = JAXBContext.newInstance(ConnexoForm.class, ConnexoFormField.class, ConnexoProperty.class);
                        Unmarshaller unmarshaller = jc.createUnmarshaller();

                        StringReader reader = new StringReader(template);
                        form = (ConnexoForm) unmarshaller.unmarshal(reader);

                    } catch (JAXBException e) {
                        e.printStackTrace();
                    }
                }

                form.content = taskService.getTaskContent(taskId);
                long contentId = taskService.getTaskById(taskId).getTaskData().getOutputContentId();
                if (contentId != -1) {
                    byte[] outContent = taskService.getContentById(contentId).getContent();
                    form.outContent = (Map<String, Object>) ContentMarshallerHelper.unmarshall(outContent, null);
                }
                form.taskStatus = taskService.getTaskById(taskId).getTaskData().getStatus();
            }
        }
        // TODO throw new WebApplicationException(null, Response.serverError().entity("Cannot inject entity manager factory!").build());
        return form;
    }

    @GET
    @Produces("application/json")
    @Path("/process/{deploymentId}/content/{processId}")
    public ConnexoForm getProcessForm(@PathParam("processId") String processId, @PathParam("deploymentId") String deploymentId) {
        if (formManagerService != null) {
            String template = formManagerService.getFormByKey(deploymentId, processId);
            if (Strings.isNullOrEmpty(template)) {
                template = formManagerService.getFormByKey(deploymentId, processId + "-taskform");
            }
            if (Strings.isNullOrEmpty(template)) {
                template = formManagerService.getFormByKey(deploymentId, processId + "-taskform.form");
            }

            if (!Strings.isNullOrEmpty(template)) {
                try {
                    JAXBContext jc = JAXBContext.newInstance(ConnexoForm.class, ConnexoFormField.class, ConnexoProperty.class);
                    Unmarshaller unmarshaller = jc.createUnmarshaller();

                    StringReader reader = new StringReader(template);
                    ConnexoForm form = (ConnexoForm) unmarshaller.unmarshal(reader);

                    return form;
                } catch (JAXBException e) {
                    e.printStackTrace();
                }
            }
        }
        // TODO throw new WebApplicationException(null, Response.serverError().entity("Cannot inject entity manager factory!").build());
        return null;
    }

    @GET
    @Produces("application/json")
    @Path("/{taskId: [0-9-]+}/taskcontent")
    public ConnexoForm getTaskContents(@PathParam("taskId") long taskId) {
        ConnexoForm form = new ConnexoForm();
        form.content = taskService.getTaskContent(taskId);
        return form;
    }

    @POST
    @Produces("application/json")
    @Path("/{taskId: [0-9-]+}/contentstart")
    public Response startTaskContent(@PathParam("taskId") long taskId, @Context SecurityContext context) {
        UserTaskInstanceDesc task = runtimeDataService.getTaskById(taskId);
        if(task != null){
            if(task.getStatus().equals("Created") || task.getStatus().equals("Ready") || task.getStatus().equals("Reserved")) {
                taskService.start(taskId, context.getUserPrincipal().getName());
                return Response.ok().build();
            }
            return Response.status(409).entity(task.getName()).build();
        }
        return Response.status(400).build();
    }

    @POST
    @Produces("application/json")
    @Path("/{taskId: [0-9-]+}/contentcomplete")
    public Response completeTaskContent(TaskOutputContentInfo taskOutputContentInfo, @PathParam("taskId") long taskId, @Context SecurityContext context) {
        UserTaskInstanceDesc task = runtimeDataService.getTaskById(taskId);
        if(task != null) {
            if (!task.getStatus().equals("Completed")) {
                TaskCommand<?> cmd = new CompleteTaskCommand(taskId, context.getUserPrincipal()
                        .getName(), taskOutputContentInfo.outputTaskContent);
                processRequestBean.doRestTaskOperation(taskId, null, null, null, cmd);
                return Response.ok().build();
            } else {
                return Response.status(409).entity(task.getName()).build();
            }
        }
        return Response.status(400).build();
    }

    @POST
    @Produces("application/json")
    @Path("/{taskId: [0-9-]+}/contentsave/")
    public Response saveTaskContent(TaskOutputContentInfo taskOutputContentInfo, @PathParam("taskId") long taskId){
        UserTaskInstanceDesc task = runtimeDataService.getTaskById(taskId);
        if(task != null) {
            if (!task.getStatus().equals("Completed")) {
                ((InternalTaskService) taskService).addContent(taskId, taskOutputContentInfo.outputTaskContent);
                return Response.ok().build();
            } else {
                return Response.status(409).entity(task.getName()).build();
            }
        }
        return Response.status(400).build();
    }

    private List<Long> taskIdList(String source){
        List<Long> taskIdList = new ArrayList<>();
        try {
            if (source != null) {
                JsonNode node = new ObjectMapper().readValue(new ByteArrayInputStream(source.getBytes()), JsonNode.class);
                if (node != null && node.isArray()) {
                    for (JsonNode singleFilter : node) {
                        JsonNode property = singleFilter.get("id");
                        if (property != null && property.getTextValue() != null)
                            taskIdList.add(Long.parseLong(property.getTextValue()));
                    }
                }
            }
        }catch (Exception e){

        }
        return taskIdList;
    }

    @POST
    @Produces("application/json")
    @Path("/mandatory")
    public TaskGroupsInfos checkMandatoryTask(TaskGroupsInfos taskGroupsInfos, @Context UriInfo uriInfo){
        List<TaskGroupsInfo> taskGroups = new ArrayList<>();
        List<Long> taskIds = taskGroupsInfos.taskGroups.get(0).taskIds;
        Map<Map<ProcessDefinition, String>, List<Task>> groupedTasks = new HashMap<>();
        for(Long id: taskIds){
            Task task = taskService.getTaskById(id);
                if (task != null) {
                    if(!task.getTaskData().getStatus().equals(Status.Completed)) {
                        ProcessDefinition process = null;
                        Collection<ProcessDefinition> processesList = runtimeDataService.getProcessesByDeploymentId(task
                                .getTaskData()
                                .getDeploymentId(), new QueryContext());
                        for (ProcessDefinition each : processesList) {
                        if (each.getDeploymentId().equals(task.getTaskData().getDeploymentId())) {
                            process = each;
                        }
                    }
                        Map<ProcessDefinition, String> proc = new HashMap<>();
                    proc.put(process, ((InternalTask) task).getFormName());
                    if (groupedTasks.containsKey(proc)) {
                        List<Task> listOfTasks = new ArrayList<>(groupedTasks.get(proc));
                        listOfTasks.add(task);
                        groupedTasks.put(proc, listOfTasks);
                    } else {
                        groupedTasks.put(proc, Collections.singletonList(task));
                    }
                }
            }
        }
        for (Map.Entry<Map<ProcessDefinition, String>, List<Task>> entry : groupedTasks.entrySet()) {
            ConnexoForm form = getTaskContent(entry.getValue().get(0).getId());
            List<Long> ids = new ArrayList<>();
            for(Task each : entry.getValue()){
                ids.add(each.getId());
            }
            form.taskStatus = Status.InProgress;
            Map.Entry<ProcessDefinition, String> entryKey = entry.getKey().entrySet().iterator().next();
            taskGroups.add(new TaskGroupsInfo(entry.getValue().get(0).getName(), entryKey.getKey().getName(), entryKey.getKey().getVersion(), ids, hasFormMandatoryFields(form), form));
        }
        return new TaskGroupsInfos(taskGroups);
    }

    private boolean hasFormMandatoryFields(ConnexoForm form){
        if(form.fields != null) {
            for (ConnexoFormField field : form.fields) {
                if(field.properties != null) {
                    for (ConnexoProperty property : field.properties) {
                        if (property.name.equals("fieldRequired")) {
                            if (property.value.equals("true")) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @POST
    @Produces("application/json")
    @Path("/managetasks")
    public TaskBulkReportInfo manageTasks(TaskGroupsInfos taskGroupsInfos, @Context UriInfo uriInfo){
        long failed = 0;
        long total = 0;
        for(TaskGroupsInfo taskGroup : taskGroupsInfos.taskGroups){
            total +=taskGroup.taskIds.size();
        }
        if (getQueryValue(uriInfo, "assign") != null) {
            if (getQueryValue(uriInfo, "currentuser") != null) {
                for(TaskGroupsInfo taskGroup : taskGroupsInfos.taskGroups){
                    for(Long taskId: taskGroup.taskIds){
                        if(!assignTaskToUser(getQueryValue(uriInfo, "assign"), getQueryValue(uriInfo, "currentuser"), taskId)){
                            failed++;
                        }
                    }
                }
            }
        }
        if(getQueryValue(uriInfo, "setPriority") != null){
            for(TaskGroupsInfo taskGroup : taskGroupsInfos.taskGroups){
                for(Long taskId: taskGroup.taskIds){
                    setPriority(Integer.valueOf(getQueryValue(uriInfo, "setPriority")), taskId);
                }
            }
        }
        if(getQueryValue(uriInfo, "setDueDate") != null){
            Date millis = new Date();
            millis.setTime(Long.valueOf(getQueryValue(uriInfo, "setDueDate")));
            for(TaskGroupsInfo taskGroup : taskGroupsInfos.taskGroups){
                for(Long taskId: taskGroup.taskIds){
                    setDueDate(millis , taskId);
                }
            }
        }
        if(getQueryValue(uriInfo, "setDueDate") == null && getQueryValue(uriInfo, "setPriority") == null && getQueryValue(uriInfo, "assign") == null){
            if (getQueryValue(uriInfo, "currentuser") != null) {
                for(TaskGroupsInfo taskGroup : taskGroupsInfos.taskGroups){
                    for(Long taskId: taskGroup.taskIds){
                        if(taskService.getTaskById(taskId) != null) {
                            if (!taskService.getTaskById(taskId).getTaskData().getStatus().equals(Status.Completed)) {
                                boolean check = true;
                                if (taskService.getTaskById(taskId).getTaskData().getStatus().equals(Status.Ready)) {
                                    assignTaskToUser(getQueryValue(uriInfo, "currentuser"), getQueryValue(uriInfo, "currentuser"), taskId);
                                }
                                if (taskService.getTaskById(taskId).getTaskData().getStatus().equals(Status.Created)) {
                                    if (assignTaskToUser(getQueryValue(uriInfo, "currentuser"), getQueryValue(uriInfo, "currentuser"), taskId)) {
                                        taskService.start(taskId, getQueryValue(uriInfo, "currentuser"));
                                    } else {
                                        check = false;
                                    }
                                }
                                if (taskService.getTaskById(taskId).getTaskData().getStatus().equals(Status.Reserved)) {
                                    if (!taskService.getTaskById(taskId)
                                            .getTaskData()
                                            .getActualOwner()
                                            .getId()
                                            .equals(getQueryValue(uriInfo, "currentuser"))) {
                                        if (assignTaskToUser(getQueryValue(uriInfo, "currentuser"), taskService.getTaskById(taskId)
                                                .getTaskData()
                                                .getActualOwner()
                                                .getId(), taskId)) {
                                            taskService.start(taskId, getQueryValue(uriInfo, "currentuser"));
                                        } else {
                                            check = false;
                                        }
                                    } else {
                                        taskService.start(taskId, getQueryValue(uriInfo, "currentuser"));
                                    }
                                }
                                if (taskService.getTaskById(taskId)
                                        .getTaskData()
                                        .getStatus()
                                        .equals(Status.InProgress)) {
                                    if (!taskService.getTaskById(taskId)
                                            .getTaskData()
                                            .getActualOwner()
                                            .getId()
                                            .equals(getQueryValue(uriInfo, "currentuser"))) {
                                        if (assignTaskToUser(getQueryValue(uriInfo, "currentuser"), getQueryValue(uriInfo, "currentuser"), taskId)) {
                                            taskService.start(taskId, getQueryValue(uriInfo, "currentuser"));
                                        } else {
                                            check = false;
                                        }
                                    }
                                }
                                if (check) {
                                    TaskCommand<?> cmd = new CompleteTaskCommand(taskId, getQueryValue(uriInfo, "currentuser"), taskGroup.outputBindingContents);
                                    processRequestBean.doRestTaskOperation(taskId, null, null, null, cmd);
                                } else {
                                    failed++;
                                }
                            }
                        }
                    }
                }
            }
        }
        return new TaskBulkReportInfo(total,failed);
    }

    private boolean assignTaskToUser(String userName, String currentuser, long taskId){
        Task task = taskService.getTaskById(taskId);
            if (task != null) {
                if (task.getTaskData().getStatus().equals(Status.Created)) {
                    List<OrganizationalEntity> businessAdministrators = task.getPeopleAssignments().getBusinessAdministrators();
                    boolean check = false;
                    for(int i = 0;i<businessAdministrators.size();i++){
                        if(businessAdministrators.get(i).getId().equals(userName)){
                            check = true;
                        }
                    }
                    if(check) {
                        taskService.activate(taskId, userName);
                        assignTaskToUser(userName, currentuser, taskId);
                    }
                    return check;
                }
                if (task.getTaskData().getStatus().equals(Status.Ready)) {
                    taskService.claim(taskId, currentuser);
                    taskService.delegate(taskId, currentuser, userName);
                }
                if (task.getTaskData().getStatus().equals(Status.Reserved)) {
                    if (task.getTaskData().getActualOwner() != null) {
                        if (!userName.equals("")) {
                            taskService.delegate(taskId, task.getTaskData().getActualOwner().getId(), userName);
                        }
                    }
                }
                if (task.getTaskData().getStatus().equals(Status.InProgress)) {
                    if(!task.getTaskData().getActualOwner().getId().equals(userName)) {
                        if (task.getTaskData().getActualOwner() != null) {
                            if (!userName.equals("")) {
                                taskService.stop(taskId, task.getTaskData().getActualOwner().getId());
                                taskService.delegate(taskId, task.getTaskData().getActualOwner().getId(), userName);
                            }
                        }
                    }
                }
            }
        return true;
    }

    private Object[] getAuditTask(long taskid){
        EntityManager em = emf.createEntityManager();
        String queryString = "Select TASKID , ACTUALOWNER, PROCESSID, CREATEDON, STATUS, NAME from AUDITTASKIMPL where TASKID = :taskId";
        Query query = em.createNativeQuery(queryString);
        query.setParameter("taskId", taskid);
        List<Object[]> list = query.getResultList();
        if(!list.isEmpty()){
            return list.get(0);
        }
        return null;
    }

    private void setDueDate(Date dueDate, long taskId){
        taskService.setExpirationDate(taskId, dueDate);
    }

    private void setPriority(int priority, long taskId){
        ((InternalTaskService) taskService).setPriority(taskId, priority);
    }

    private List<TaskSummary> getTaskForProceessInstance(long processInstanceId){
        if(emf != null) {
            EntityManager em = emf.createEntityManager();
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

            final CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(TaskSummary.class);
            final Root taskRoot = criteriaQuery.from(TaskImpl.class);

            criteriaQuery.select(criteriaBuilder.construct(TaskSummary.class,
                    taskRoot.get("id"),
                    taskRoot.get("name"),
                    taskRoot.get("taskData").get("processId"),
                    taskRoot.get("taskData").get("deploymentId"),
                    taskRoot.get("taskData").get("expirationTime"),
                    taskRoot.get("taskData").get("createdOn"),
                    taskRoot.get("priority"),
                    taskRoot.get("taskData").get("status"),
                    taskRoot.get("taskData").get("actualOwner").get("id"),
                    taskRoot.get("taskData").get("processInstanceId")
            ));
            List<Predicate> predicatesStatus = new ArrayList<>();
            List<Predicate> predicateList = new ArrayList<>();
            Predicate p1 = criteriaBuilder.disjunction();
            predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get("status"), Status.InProgress));
            predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get("status"), Status.Created));
            predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get("status"), Status.Ready));
            predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get("status"), Status.Reserved));
            Predicate predicateProcessId = criteriaBuilder.equal(taskRoot.get("taskData").get("processInstanceId"), processInstanceId);
            p1 = criteriaBuilder.or(predicatesStatus.toArray(new Predicate[predicatesStatus.size()]));
            predicateList.add(p1);
            predicateList.add(predicateProcessId);
            criteriaQuery.where((criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]))));
            final TypedQuery query = em.createQuery(criteriaQuery);
            TaskSummaryList taskSummaryList = new TaskSummaryList(runtimeDataService, query.getResultList());
            return taskSummaryList.getTasks();
        }
        return null;
    }


    private Map<String, JsonNode> getFilterProperties(String source, String value){
        LinkedHashMap<String, JsonNode> filterProperties = new LinkedHashMap<String, JsonNode>();
        try {
            if (source != null) {
                JsonNode node = new ObjectMapper().readValue(new ByteArrayInputStream(source.getBytes()), JsonNode.class);
                if (node != null && node.isArray()) {
                    for (JsonNode singleFilter : node) {
                        JsonNode property = singleFilter.get(PROPERTY);
                        if (property != null && property.getTextValue() != null)
                            filterProperties.put(property.getTextValue(), singleFilter.get(value));
                    }
                }
            }
        }catch (Exception e){

        }
        return filterProperties;
    }

    private String getQueryValue(UriInfo uriInfo,String key){
        return uriInfo.getQueryParameters().getFirst(key);
    }

    private String addFilterToQuery(Map<String, JsonNode> filterProperties){
        String process = "";
        String startedOnFrom = "";
        String startedOnTo = "";
        String status = "";
        String startedBy = "";
        String order = " order by p.START_DATE";
        String filter = "";
        Iterator<String> it = filterProperties.keySet().iterator();
        while(it.hasNext()) {
            String theKey = (String) it.next();
            if (theKey.equals("process")) {
                for(int i=0;i<filterProperties.get("process").size();i++) {
                    if(process.equals("")) {
                        process += "(p.PROCESSID = " + filterProperties.get("process")
                                .get(i)
                                .toString()
                                .split(" \\(")[0].replace("\"", "'") + "'";
                        process += "AND p.PROCESSVERSION = '" + filterProperties.get("process")
                                .get(i)
                                .toString()
                                .split(" \\(")[1].replace(")", "") + "')";
                    }else{
                        process += " OR (p.PROCESSID = " + filterProperties.get("process")
                                .get(i)
                                .toString()
                                .split(" \\(")[0].replace("\"", "'") + "'";
                        process += "AND p.PROCESSVERSION = '" + filterProperties.get("process")
                                .get(i)
                                .toString()
                                .split(" \\(")[1].replace(")", "") + "')";
                    }
                }
            }
            if (theKey.equals("status")) {
                for(int i=0;i<filterProperties.get("status").size();i++) {
                    if(status.equals("")) {
                        status += "p.STATUS = " + filterProperties.get("status").get(i).toString().replace("\"","'");
                    }else{
                        status += " OR p.STATUS = " + filterProperties.get("status").get(i).toString().replace("\"", "'");;
                    }
                }
            }
            if (theKey.equals("user")) {
                for(int i=0;i<filterProperties.get("user").size();i++) {
                    if(startedBy.equals("")) {
                        startedBy += "p.USER_IDENTITY = " + filterProperties.get("user").get(i).toString().replace("\"","'");
                    }else{
                        startedBy += " OR p.USER_IDENTITY = " + filterProperties.get("user").get(i).toString().replace("\"", "'");;
                    }
                }
            }
            if (theKey.equals("startedOnFrom")) {
                startedOnFrom = "AND (p.START_DATE > FROM_TZ(timestamp '1970-01-01 00:00:00' + numtodsinterval(" + filterProperties.get("startedOnFrom").toString() +"/1000, 'second'),'UTC') AT TIME ZONE SESSIONTIMEZONE)";
            }
            if (theKey.equals("startedOnTo")) {
                startedOnTo = "AND (p.START_DATE < FROM_TZ(timestamp '1970-01-01 00:00:00' + numtodsinterval(" + filterProperties.get("startedOnTo").toString() +"/1000, 'second'),'UTC') AT TIME ZONE SESSIONTIMEZONE)";
            }
        }


        if(!process.equals("")){
            filter += "AND ( " + process + ")";
        }
        if(!status.equals("")){
            filter += "AND ( " + status + ")";
        }else{
            filter += "AND(p.STATUS = 2 OR p.STATUS = 3 )";
        }
        if(!startedBy.equals("")){
            filter += "AND ( " + startedBy + ")";
        }
        if(!startedOnFrom.equals("")){
            filter += startedOnFrom;
        }
        if(!startedOnTo.equals("")){
            filter += startedOnTo;
        }
        filter += order;
        return filter;
    }

    protected String getTaskFormName(Task task) {
        String formName = ((InternalTask) task).getFormName();
        if (formName != null && !formName.equals("")) {
            // if the form name has extension it
            if (formName.endsWith(getFormExtension())) {
                return formName;
            }
            return formName + getFormSuffix();
        } else {
            if (task.getNames() != null && !task.getNames().isEmpty()) {
                formName = task.getNames().get(0).getText();
                if (formName != null) {
                    return formName.replace(" ", "") + getFormSuffix();
                }
            }
        }
        return null;
    }

    protected String getFormSuffix() {
        return "-taskform" + getFormExtension();
    }

    protected String getFormExtension() {
        return "";
    }

}
