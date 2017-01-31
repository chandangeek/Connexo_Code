/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
import org.jbpm.services.cdi.Selectable;
import org.jbpm.services.cdi.producer.UserGroupInfoProducer;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.kie.api.runtime.query.QueryContext;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.remote.services.cdi.ProcessRequestBean;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Inject
    @Selectable
    private Instance<UserGroupInfoProducer> userGroupInfoProducers;


    @POST
    @Produces("application/json")
    public TaskSummaryList getTasks(ProcessDefinitionInfos processDefinitionInfos, @Context UriInfo uriInfo){
        Map<String, JsonNode> filterProperties = getFilterProperties(getQueryValue(uriInfo,"filter"),"value");
        Map<String, JsonNode> sortProperties = getFilterProperties(getQueryValue(uriInfo,"sort"),"direction");
        List<String> deploymentIds = processDefinitionInfos.processes.stream().map(proc -> proc.deploymentId).collect(Collectors.toList());
        List<String> processIds = processDefinitionInfos.processes.stream().map(proc -> proc.processId).collect(Collectors.toList());

        if(deploymentIds != null && processIds != null && !deploymentIds.isEmpty() && !processIds.isEmpty()) {
            EntityManager em = emf.createEntityManager();
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

            final CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(TaskMinimal.class);
            final Root taskRoot = criteriaQuery.from(TaskImpl.class);


            List<Predicate> predicatesDeploymentId = new ArrayList<>();
            List<Predicate> predicatesProcessId = new ArrayList<>();
            List<Predicate> predicatesStatus = new ArrayList<>();
            List<Predicate> predicateList = new ArrayList<>();
            for (String each : deploymentIds) {
                predicatesDeploymentId.add(criteriaBuilder.equal(taskRoot.get("taskData").get("deploymentId"), each));
            }
            for (String each : processIds) {
                predicatesProcessId.add(criteriaBuilder.equal(taskRoot.get("taskData").get("processId"), each));
            }

            if (!predicatesDeploymentId.isEmpty()) {
                Predicate p1 = criteriaBuilder.or(predicatesDeploymentId.toArray(new Predicate[predicatesDeploymentId.size()]));
                predicateList.add(p1);
            }

            predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get("status"), Status.InProgress));
            predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get("status"), Status.Created));
            predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get("status"), Status.Ready));
            predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get("status"), Status.Reserved));

            if (!predicatesStatus.isEmpty()) {
                Predicate p1 = criteriaBuilder.or(predicatesStatus.toArray(new Predicate[predicatesStatus.size()]));
                predicateList.add(p1);
            }

            criteriaQuery.where(criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()])));

            criteriaQuery.select(criteriaBuilder.construct(TaskMinimal.class,
                    taskRoot.get("id")
            ));

            final TypedQuery query = em.createQuery(criteriaQuery);
            List<TaskMinimal> taskMinimals = query.getResultList();
            List<Task> tasks = taskMinimals.stream()
                    .map(minimal -> taskService.getTaskById(minimal.getId()))
                    .collect(Collectors.toList());


            List<java.util.function.Predicate<Task>> statusP = new ArrayList<>();
            List<java.util.function.Predicate<Task>> userP = new ArrayList<>();
            List<java.util.function.Predicate<Task>> dueDatePredicate = new ArrayList<>();
            List<java.util.function.Predicate<Task>> processPredicate = new ArrayList<>();
            List<java.util.function.Predicate<Task>> deploymentPredicate = new ArrayList<>();
            List<java.util.function.Predicate<Task>> workGroupPredicate = new ArrayList<>();

            Iterator<String> it = filterProperties.keySet().iterator();
            while (it.hasNext()) {
                String theKey = (String) it.next();
                if (theKey.equals("status")) {
                    for (int i = 0; i < filterProperties.get("status").size(); i++) {
                        if (filterProperties.get("status").get(i).toString().contains("CREATED")) {
                            statusP.add((task) -> task.getTaskData().getStatus().equals(Status.Created));
                            statusP.add((task) -> task.getTaskData().getStatus().equals(Status.Ready));
                            statusP.add((task) -> task.getTaskData().getStatus().equals(Status.Suspended));
                        }
                        if (filterProperties.get("status").get(i).toString().contains("ASSIGNED")) {
                            statusP.add((task) -> task.getTaskData().getStatus().equals(Status.Reserved));
                        }
                        if (filterProperties.get("status").get(i).toString().contains("ONGOING")) {
                            statusP.add((task) -> task.getTaskData().getStatus().equals(Status.InProgress));
                        }
                    }
                }
                if (theKey.equals("user")) {
                    for (int i = 0; i < filterProperties.get("user").size(); i++) {
                        final String userName = filterProperties.get("user").get(i).toString().replace("\"", "");
                        if (filterProperties.get("user").get(i).toString().replace("\"", "").equals("Unassigned")) {
                            userP.add((task -> task.getTaskData().getActualOwner() == null));
                        } else {
                            userP.add((task) -> task.getTaskData().getActualOwner() != null && task.getTaskData()
                                    .getActualOwner()
                                    .getId()
                                    .equals(userName));
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
                            dueDatePredicate.add((task) -> task.getTaskData()
                                    .getExpirationTime() != null && task.getTaskData()
                                    .getExpirationTime()
                                    .before(cal.getTime()));
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
                            dueDatePredicate.add((task) -> task.getTaskData()
                                    .getExpirationTime() != null && task.getTaskData()
                                    .getExpirationTime()
                                    .after(cal.getTime()) && task.getTaskData()
                                    .getExpirationTime()
                                    .before(cal2.getTime()));
                        }
                        if (filterProperties.get("dueDate").get(i).toString().replace("\"", "").equals("UPCOMING")) {
                            Calendar cal = Calendar.getInstance();
                            cal.add(Calendar.DATE, 0);
                            cal.set(Calendar.HOUR, 11);
                            cal.set(Calendar.MINUTE, 59);
                            cal.set(Calendar.SECOND, 59);
                            cal.set(Calendar.AM_PM, Calendar.PM);
                            dueDatePredicate.add((task) -> task.getTaskData()
                                    .getExpirationTime() != null && task.getTaskData()
                                    .getExpirationTime()
                                    .after(cal.getTime()));
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
                            processPredicate.add((task) -> task.getTaskData().getProcessId() != null && task.getTaskData().getProcessId().equals(processId));
                            deploymentPredicate.add((task) -> task.getTaskData().getDeploymentId() != null && task.getTaskData().getDeploymentId().equals(deploymentId));
                        }
                    }
                }
                if (theKey.equals("workgroup")) {
                    for (int i = 0; i < filterProperties.get("workgroup").size(); i++) {
                        final String workGroup = filterProperties.get("workgroup").get(i).toString().replace("\"", "");
                        if (filterProperties.get("workgroup").get(i).toString().replace("\"", "").equals("Unassigned")) {
                            workGroupPredicate.add((task -> task.getPeopleAssignments().getPotentialOwners().stream()
                                    .filter(potO -> potO instanceof Group)
                                    .collect(Collectors.toList())
                                    .isEmpty()));
                        } else {
                            workGroupPredicate.add((task) -> task.getPeopleAssignments().getPotentialOwners().stream()
                                    .filter(potO -> potO instanceof Group)
                                    .map(OrganizationalEntity::getId).anyMatch(groupName -> groupName.equals(workGroup)));
                        }
                    }
                }
            }

            tasks = tasks.stream()
                    .filter(statusP.stream().reduce(java.util.function.Predicate::or).orElse(t -> true))
                    .filter(userP.stream().reduce(java.util.function.Predicate::or).orElse(t -> true))
                    .filter(dueDatePredicate.stream().reduce(java.util.function.Predicate::or).orElse(t -> true))
                    .filter(processPredicate.stream().reduce(java.util.function.Predicate::or).orElse(t -> true))
                    .filter(deploymentPredicate.stream().reduce(java.util.function.Predicate::or).orElse(t -> true))
                    .filter(workGroupPredicate.stream().reduce(java.util.function.Predicate::or).orElse(t -> true))
                    .collect(Collectors.toList());

            List<Comparator<Task>> sort = new ArrayList<>();
            if (!sortProperties.isEmpty()) {
                Iterator<String> sortIterator = sortProperties.keySet().iterator();
                while (sortIterator.hasNext()) {
                    String theKey = (String) sortIterator.next();
                    if (theKey.equals("dueDate")) {
                        if (sortProperties.get("dueDate").toString().replace("\"", "").equals("asc")) {
                            sort.add(Comparator.comparing((task) -> task.getTaskData().getExpirationTime(), Comparator.nullsFirst(Date::compareTo)));
                        } else {
                            sort.add(Comparator.comparing((task) -> task.getTaskData().getExpirationTime(), Comparator.nullsFirst(Date::compareTo).reversed()));
                        }
                    }
                    if (theKey.equals("creationDate")) {
                        if (sortProperties.get("creationDate").toString().replace("\"", "").equals("asc")) {
                            sort.add((task1, task2) -> task1.getTaskData().getCreatedOn().compareTo(task2.getTaskData().getCreatedOn()));
                        } else {
                            sort.add((task1, task2) -> task2.getTaskData().getCreatedOn().compareTo(task1.getTaskData().getCreatedOn()));
                        }
                    }
                    if (theKey.equals("priority")) {
                        if (sortProperties.get("priority").toString().replace("\"", "").equals("asc")) {
                            sort.add((task1, task2) -> Integer.compare(task1.getPriority(), task2.getPriority()));
                        } else {
                            sort.add((task1, task2) -> Integer.compare(task2.getPriority(), task1.getPriority()));
                        }
                    }
                }
            } else {
                sort.add((task1, task2) -> task1.getName().compareTo(task2.getName()));
            }

            Collections.reverse(sort);
            for (Comparator<Task> aSort : sort) {
                tasks = tasks.stream().sorted(aSort).collect(Collectors.toList());
            }

            TaskSummaryList taskSummaryList = new TaskSummaryList(tasks);
            taskSummaryList.getTasks().stream().forEach(taskSummary -> {
                ProcessDefinition process = runtimeDataService.getProcessById(taskSummary.getProcessName());
                if(process != null){
                    taskSummary.setProcessName(process.getName());
                }
            });
            taskSummaryList.setTotal(tasks.size());
            return taskSummaryList;

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
    @Path("/toptasks")
    @Produces("application/json")
    public TopTasksInfo getTopTasks(TopTasksPayload topTasksPayload, @Context UriInfo uriInfo){
        TopTasksInfo topTasksInfo = new TopTasksInfo();
        Comparator<TaskSummary> priorityComparator = (task1, task2) -> Integer.compare(task1.getPriority(), task2.getPriority());
        Comparator<TaskSummary> dueDateComparator = Comparator.comparing(TaskSummary::getDueDate, Comparator.nullsLast(Date::compareTo));
        Comparator<TaskSummary> nameComparator = (task1, task2) -> task1.getName().toLowerCase().compareTo(task2.getName().toLowerCase());
        TaskSummaryList tasks = getTasks(topTasksPayload.processDefinitionInfos, uriInfo);
        List<TaskSummary> filteredTasks = tasks.getTasks()
                .stream()
                .filter(taskSummary -> taskSummary.getActualOwner()
                        .equals(topTasksPayload.userName) ||
                        ((topTasksPayload.workGroups.contains(taskSummary.getWorkGroup()) && taskSummary.getActualOwner().equals(""))
                                || topTasksPayload.workGroups.contains(taskSummary.getWorkGroup()) && taskSummary.getActualOwner().equals(topTasksPayload.userName)))
                .collect(Collectors.toList());
        topTasksInfo.totalUserAssigned = filteredTasks.stream().filter(taskSummary -> taskSummary.getActualOwner().equals(topTasksPayload.userName)).count();
        topTasksInfo.workGroupAssigned = filteredTasks.stream().filter(taskSummary -> topTasksPayload.workGroups.contains(taskSummary.getWorkGroup()) && taskSummary.getActualOwner().equals("")).count();
        topTasksInfo.tasks = filteredTasks.stream().sorted(dueDateComparator.thenComparing(priorityComparator).thenComparing(nameComparator)).limit(5).collect(Collectors.toList());
        return topTasksInfo;
    }


    @POST
    @Path("/release/{taskId: [0-9-]+}")
    public Response releaseTask(@Context UriInfo uriInfo, @PathParam("taskId") long taskId){
        String currentuser = getQueryValue(uriInfo, "currentuser");
        Task task = taskService.getTaskById(taskId);
        if(task != null) {
            taskService.release(task.getId(), currentuser);
        }
        return Response.ok().build();
    }

    @POST
    @Path("/assigntome/{taskId: [0-9-]+}")
    public Response assignToMeTask(@Context UriInfo uriInfo, @PathParam("taskId") long taskId){
        String currentuser = getQueryValue(uriInfo, "currentuser");
        Task task = taskService.getTaskById(taskId);
        if(task != null) {
            if(currentuser != null) {
                boolean check = assignTaskToUser(currentuser, currentuser, taskId);
                if(!check){
                    return Response.status(403).build();
                }
            }
        }
        return Response.ok().build();
    }

    @POST
    @Path("/{taskId: [0-9-]+}/{optLock: [0-9-]+}/assign")
    public Response assignTask(@Context UriInfo uriInfo, @PathParam("taskId") long taskId, @PathParam("optLock") long optLock){
        String userName = getQueryValue(uriInfo, "username");
        String currentuser = getQueryValue(uriInfo, "currentuser");
        String priority = getQueryValue(uriInfo, "priority");
        String date = getQueryValue(uriInfo, "duedate");
        String workGroupName = getQueryValue(uriInfo, "workgroupname");
        Task task = taskService.getTaskById(taskId);
        if(task != null) {
            if(((TaskImpl) task).getVersion() == optLock) {
                if(userName != null && currentuser != null) {
                    if(!userName.equals("Unassigned")) {
                        boolean check = assignTaskToUser(userName, currentuser, taskId);
                        if (!check) {
                            return Response.status(403).build();
                        }
                    }else {
                        if(task.getTaskData().getActualOwner() != null){
                            taskService.release(task.getId(), task.getTaskData().getActualOwner().getId());
                        }
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
                if(workGroupName != null){
                    taskService.execute(new ComplexAssigneeForwardTaskCommand(taskId, workGroupName));
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
    public Response getProcessForm(@PathParam("processId") String processId, @PathParam("deploymentId") String deploymentId) {
        if(runtimeDataService.getProcessesById(processId).size() == 0){
            return Response.ok().entity("Undeployed").build();
        }
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

                    return Response.ok().entity(form).build();
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
    @Path("/{taskId: [0-9-]+}/contentstart/{username}")
    public Response startTaskContent(@PathParam("taskId") long taskId, @PathParam("username") String username, @Context SecurityContext context, @HeaderParam("Authorization") String auth) {
        UserTaskInstanceDesc task = runtimeDataService.getTaskById(taskId);
        if(task != null){
            if(task.getStatus().equals("Created") || task.getStatus().equals("Ready") || task.getStatus().equals("Reserved")) {
                if(auth.contains("Basic") && username.equals(task.getActualOwner())){
                    taskService.start(taskId, task.getActualOwner());
                    return Response.ok().build();
                } else {
                    if (auth.contains("Bearer") && task.getActualOwner().equals(context.getUserPrincipal().getName())) {
                        taskService.start(taskId, context.getUserPrincipal().getName());
                        return Response.ok().build();
                    }
                }
            }
            return Response.status(409).entity(task.getName()).build();
        }
        return Response.status(400).build();
    }

    @POST
    @Produces("application/json")
    @Path("/{taskId: [0-9-]+}/contentcomplete/{username}")
    public Response completeTaskContent(TaskOutputContentInfo taskOutputContentInfo, @PathParam("taskId") long taskId, @PathParam("username") String username, @Context SecurityContext context, @HeaderParam("Authorization") String auth) {
        UserTaskInstanceDesc task = runtimeDataService.getTaskById(taskId);
        if(task != null) {
            if (!task.getStatus().equals("Completed")) {
                if(auth.contains("Basic") && username.equals(task.getActualOwner())){
                    TaskCommand<?> cmd = new CompleteTaskCommand(taskId, task.getActualOwner(), taskOutputContentInfo.outputTaskContent);
                    processRequestBean.doRestTaskOperation(taskId, null, null, null, cmd);
                    return Response.ok().build();
                } else {
                    if (auth.contains("Bearer") && task.getActualOwner().equals(context.getUserPrincipal().getName())) {
                        TaskCommand<?> cmd = new CompleteTaskCommand(taskId, context.getUserPrincipal()
                                .getName(), taskOutputContentInfo.outputTaskContent);
                        processRequestBean.doRestTaskOperation(taskId, null, null, null, cmd);
                        return Response.ok().build();
                    }
                }
            }
            return Response.status(409).entity(task.getName()).build();
        }
        return Response.status(400).build();
    }

    @POST
    @Produces("application/json")
    @Path("/{taskId: [0-9-]+}/contentsave/{username}")
    public Response saveTaskContent(TaskOutputContentInfo taskOutputContentInfo, @PathParam("taskId") long taskId, @PathParam("username") String username, @Context SecurityContext context, @HeaderParam("Authorization") String auth){
        UserTaskInstanceDesc task = runtimeDataService.getTaskById(taskId);
        if(task != null) {
            if (!task.getStatus().equals("Completed")) {
                if(auth.contains("Basic") && username.equals(task.getActualOwner())){
                    ((InternalTaskService) taskService).addContent(taskId, taskOutputContentInfo.outputTaskContent);
                    return Response.ok().build();
                } else {
                    if (auth.contains("Bearer") && task.getActualOwner().equals(context.getUserPrincipal().getName())) {
                        ((InternalTaskService) taskService).addContent(taskId, taskOutputContentInfo.outputTaskContent);
                        return Response.ok().build();
                    }
                }
            }
            return Response.status(409).entity(task.getName()).build();
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
                        if(!getQueryValue(uriInfo, "assign").equals("Unassigned")) {
                            if (!assignTaskToUser(getQueryValue(uriInfo, "assign"), getQueryValue(uriInfo, "currentuser"), taskId)) {
                                failed++;
                            }
                        }else{
                            if(taskService.getTaskById(taskId).getTaskData().getActualOwner() != null){
                                taskService.release(taskId, taskService.getTaskById(taskId).getTaskData().getActualOwner().getId());
                            }
                        }
                    }
                }
            }
        }
        if(getQueryValue(uriInfo, "workgroup") != null){
            for(TaskGroupsInfo taskGroup : taskGroupsInfos.taskGroups){
                for(Long taskId: taskGroup.taskIds){
                    taskService.execute(new ComplexAssigneeForwardTaskCommand(taskId, getQueryValue(uriInfo, "workgroup")));
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
        if(getQueryValue(uriInfo, "setDueDate") == null && getQueryValue(uriInfo, "setPriority") == null && getQueryValue(uriInfo, "assign") == null && getQueryValue(uriInfo, "workgroup") == null){
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
                taskService.execute(new AddUserToPeopleAssigmentCommand(taskId, currentuser));
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
        if(taskService.getTaskById(taskId) != null) {
            taskService.setExpirationDate(taskId, dueDate);
        }
    }

    private void setPriority(int priority, long taskId){
        if(taskService.getTaskById(taskId) != null) {
            ((InternalTaskService) taskService).setPriority(taskId, priority);
        }
    }

    private List<TaskSummary> getTaskForProceessInstance(long processInstanceId){
        if(emf != null) {
            EntityManager em = emf.createEntityManager();
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

            final CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(TaskMinimal.class);
            final Root taskRoot = criteriaQuery.from(TaskImpl.class);

            criteriaQuery.select(criteriaBuilder.construct(TaskMinimal.class,
                    taskRoot.get("id")
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
            List<TaskMinimal> taskMinimals = query.getResultList();
            List<Task> tasks = taskMinimals.stream()
                    .map(minimal -> taskService.getTaskById(minimal.getId()))
                    .collect(Collectors.toList());

            TaskSummaryList taskSummaryList = new TaskSummaryList(tasks);
            taskSummaryList.getTasks().stream().forEach(taskSummary -> {
                ProcessDefinition process = runtimeDataService.getProcessById(taskSummary.getProcessName());
                if(process != null){
                    taskSummary.setProcessName(process.getName());
                }
            });

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
