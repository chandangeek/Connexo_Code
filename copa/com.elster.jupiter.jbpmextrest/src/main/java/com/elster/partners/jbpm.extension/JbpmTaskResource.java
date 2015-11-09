package com.elster.partners.jbpm.extension;


import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.jbpm.bpmn2.xml.TaskHandler;
import org.jbpm.services.task.audit.impl.model.AuditTaskImpl;
import org.jbpm.services.task.impl.model.OrganizationalEntityImpl;
import org.jbpm.services.task.impl.model.TaskImpl;

import javax.inject.Inject;

import org.jbpm.services.task.impl.model.UserImpl;
import org.kie.api.definition.process.*;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.api.model.InternalTaskData;

import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.util.*;

@Path("/tasks")
public class JbpmTaskResource {

    private static final String PROPERTY = "property";

    @Inject
    @PersistenceUnit(unitName = "org.jbpm.domain")
    private EntityManagerFactory emf;

    @Inject
    InternalTaskService internalTaskService;

    @GET
    @Produces("application/json")
    public TaskSummaryList getTasks(@Context UriInfo uriInfo){
        Map<String, JsonNode> filterProperties;
        Map<String, JsonNode> sortProperties;
        filterProperties = getFilterProperties(getQueryValue(uriInfo,"filter"),"value");
        sortProperties = getFilterProperties(getQueryValue(uriInfo,"sort"),"direction");
        int startIndex = 0;
        int endIndex = Integer.MAX_VALUE;
        try {
            startIndex = Integer.valueOf(getQueryValue(uriInfo, "start"));
            endIndex = Integer.valueOf(getQueryValue(uriInfo, "limit"));
            endIndex++;
        }catch (NumberFormatException e){
        }
        if(emf != null) {
            EntityManager em = emf.createEntityManager();
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

            final CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(TaskSummary.class);
            final Root taskRoot = criteriaQuery.from(TaskImpl.class);
            if(!filterProperties.isEmpty()){
                List<Predicate> predicatesUser = new ArrayList<Predicate>();
                List<Predicate> predicatesDueDate = new ArrayList<Predicate>();
                List<Predicate> predicatesProcess = new ArrayList<Predicate>();
                List<Predicate> predicatesStatus = new ArrayList<Predicate>();
                Iterator<String> it = filterProperties.keySet().iterator();
                while(it.hasNext()){
                    String theKey = (String)it.next();
                    if(theKey.equals("status")) {
                        for(int i=0;i<filterProperties.get("status").size();i++) {
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
                                predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get(theKey), Status.Completed));
                            }
                            if (filterProperties.get("status").get(i).toString().contains("FAILED")) {
                                predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get(theKey), Status.Failed));
                                predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get(theKey), Status.Error));
                                predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get(theKey), Status.Exited));
                                predicatesStatus.add(criteriaBuilder.equal(taskRoot.get("taskData").get(theKey), Status.Obsolete));
                            }
                        }
                    }
                    if(theKey.equals("user")) {
                        for (int i = 0; i < filterProperties.get("user").size(); i++) {
                            if(filterProperties.get("user").get(i).toString().replace("\"", "").equals("Unassigned")){
                                predicatesUser.add(criteriaBuilder.and(taskRoot.get("taskData").get("actualOwner").isNull()));
                            }else {
                                predicatesUser.add(criteriaBuilder.equal(taskRoot.get("taskData").get("actualOwner").get("id"), filterProperties.get("user").get(i).toString().replace("\"", "")));
                            }
                        }
                    }
                    if(theKey.equals("dueDate")) {
                        for (int i = 0; i < filterProperties.get("dueDate").size(); i++) {
                            if(filterProperties.get("dueDate").get(i).toString().replace("\"", "").equals("OVERDUE")){
                                Calendar cal = Calendar.getInstance();
                                cal.add(Calendar.DATE, -1);
                                cal.set(Calendar.HOUR, 11);
                                cal.set(Calendar.MINUTE, 59);
                                cal.set(Calendar.SECOND, 59);
                                cal.set(Calendar.AM_PM, Calendar.PM);
                                predicatesDueDate.add(criteriaBuilder.lessThanOrEqualTo(taskRoot.<Date>get("taskData").get("expirationTime") , cal.getTime()));
                            }
                            if(filterProperties.get("dueDate").get(i).toString().replace("\"", "").equals("TODAY")){
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
                            if(filterProperties.get("dueDate").get(i).toString().replace("\"", "").equals("UPCOMING")){
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
                    if(theKey.equals("process")) {
                        for (int i = 0; i < filterProperties.get("process").size(); i++) {
                            predicatesProcess.add(criteriaBuilder.equal(taskRoot.get("taskData").get("processId"), filterProperties.get("process").get(i).toString().replace("\"", "")));
                        }
                    }
                }

                List<Predicate> predicateList = new ArrayList<Predicate>();
                Predicate p1 = criteriaBuilder.disjunction();
                if(!predicatesStatus.isEmpty()) {
                    p1 = criteriaBuilder.or(predicatesStatus.toArray(new Predicate[predicatesStatus.size()]));
                    predicateList.add(p1);
                }

                Predicate p2 = criteriaBuilder.disjunction();
                if(!predicatesUser.isEmpty()) {
                    p2 = criteriaBuilder.or(predicatesUser.toArray(new Predicate[predicatesUser.size()]));
                    predicateList.add(p2);
                }

                Predicate p3 = criteriaBuilder.disjunction();
                if(!predicatesDueDate.isEmpty()) {
                    p3 = criteriaBuilder.or(predicatesDueDate.toArray(new Predicate[predicatesDueDate.size()]));
                    predicateList.add(p3);
                }

                Predicate p4 = criteriaBuilder.disjunction();
                if(!predicatesProcess.isEmpty()) {
                    p4 = criteriaBuilder.or(predicatesProcess.toArray(new Predicate[predicatesProcess.size()]));
                    predicateList.add(p4);

                }
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

            if(!sortProperties.isEmpty()) {
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
            }else{
                criteriaQuery.orderBy(criteriaBuilder.asc(taskRoot.get("name")));
            }

            final TypedQuery query = em.createQuery(criteriaQuery);

            query.setFirstResult(startIndex);
            query.setMaxResults(endIndex);

            TaskSummaryList taskSummaryList = new TaskSummaryList(query.getResultList());
            if(taskSummaryList.getTotal() == endIndex){
                int total = startIndex+ endIndex;
                taskSummaryList.removeLast(total);
            }else{
                int total = startIndex + taskSummaryList.getTotal();
                taskSummaryList.setTotal(total);
            }
            return taskSummaryList;
        }
        throw new WebApplicationException(null, Response.serverError().entity("Cannot inject entity manager factory!").build());
    }

    @GET
    @Path("/{taskId: [0-9-]+}/")
    @Produces("application/json")
    public TaskSummary getTask(@PathParam("taskId") long taskid){
        Task task = internalTaskService.getTaskById(taskid);
        TaskSummary taskSummary = new TaskSummary(task);
        return taskSummary;
    }

    @POST
    @Path("/{taskId: [0-9-]+}/assign")
    public Response assignTask(@Context UriInfo uriInfo,@PathParam("taskId") long taskId){
        String userName = getQueryValue(uriInfo, "username");
        String currentuser = getQueryValue(uriInfo, "currentuser");
        Task task = internalTaskService.getTaskById(taskId);
        if(task.getTaskData().getStatus().equals(Status.Created)) {
            internalTaskService.activate(taskId, userName);
            List<OrganizationalEntity> list = new ArrayList<OrganizationalEntity>(1);
            list.add(new UserImpl("admin"));
            internalTaskService.nominate(taskId, "admin", list);
        }
        if(task.getTaskData().getStatus().equals(Status.Ready)) {
            internalTaskService.claim(taskId, currentuser);
            internalTaskService.delegate(taskId, currentuser, userName);
        }
        if(task.getTaskData().getStatus().equals(Status.Reserved)) {
            if(task.getTaskData().getActualOwner() != null) {
                if(!userName.equals("")) {
                    internalTaskService.delegate(taskId, task.getTaskData().getActualOwner().getId(), userName);
                }
            }
        }
        if(task.getTaskData().getStatus().equals(Status.InProgress)) {
            if(task.getTaskData().getActualOwner() != null) {
                if(!userName.equals("")) {
                    internalTaskService.stop(taskId, task.getTaskData().getActualOwner().getId());
                    internalTaskService.delegate(taskId, task.getTaskData().getActualOwner().getId(), userName);
                }
            }
        }
        return Response.ok().build();
    }

    @POST
    @Path("/{taskId: [0-9-]+}/set")
    public Response setDue(@Context UriInfo uriInfo,@PathParam("taskId") long taskId){
        String priority = getQueryValue(uriInfo, "priority");
        String date = getQueryValue(uriInfo, "duedate");
        if(priority != null || date != null){
            if(priority != null && !priority.equals("")){
                internalTaskService.setPriority(taskId, Integer.valueOf(priority));
            }
            if(date != null && !date.equals("")){
                Date millis = new Date();
                millis.setTime(Long.valueOf(date));
                internalTaskService.setExpirationDate(taskId, millis);
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
            Predicate p1 = criteriaBuilder.equal(taskRoot.get("taskData").get("processInstanceId"), processInstanceId);
            criteriaQuery.where(criteriaBuilder.and(p1));
            final TypedQuery query = em.createQuery(criteriaQuery);
            TaskSummaryList taskSummaryList = new TaskSummaryList(query.getResultList());
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
                        process += "p.PROCESSID = " + filterProperties.get("process").get(i).toString().replace("\"","'");
                    }else{
                        process += " OR p.PROCESSID = " + filterProperties.get("process").get(i).toString().replace("\"", "'");;
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
}
