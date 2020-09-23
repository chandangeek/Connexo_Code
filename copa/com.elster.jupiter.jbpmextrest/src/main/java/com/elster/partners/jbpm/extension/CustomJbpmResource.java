package com.elster.partners.jbpm.extension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.kie.api.KieServices;
import org.kie.api.command.KieCommands;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.server.services.api.KieServerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/tasks")
public class CustomJbpmResource {

    private static final String PROPERTY = "property";
    private static final String DEFAULT_SORTING = " order by p.START_DATE DESC";

    private static final Logger logger = LoggerFactory.getLogger(JbpmTaskResource.class);

    private KieCommands commandsFactory = KieServices.Factory.get().getCommands();

    private KieServerRegistry registry;

    RuntimeDataService runtimeDataService;
    QueryService queryService;
    UserTaskService taskService;

    private EntityManagerFactory emf;

    public CustomJbpmResource() {}

    public CustomJbpmResource(KieServerRegistry registry, UserTaskService taskService, RuntimeDataService runtimeDataService, QueryService queryService) {
        this.registry = registry;
        this.taskService = taskService;
        this.runtimeDataService = runtimeDataService;
        this.queryService=queryService;
    }

    @GET
    @Path("/hello")
    @Produces("application/json")
    public Response test(@Context UriInfo uriInfo){
        String content = "{\"Hello world KIE-SERVER\":[]}";
        return Response.ok().entity(content).build();
    }

    @GET
    @Path("/process/allprocesses")
    @Produces("application/json")
    public ProcessHistoryGenInfos getProcessAll(@Context UriInfo uriInfo){
        if (emf == null) {
            emf = EntityManagerFactoryManager.get().getOrCreate("org.jbpm.domain");
        }

        Map<String, JsonNode> filterProperties;
        filterProperties = getFilterProperties(getQueryValue(uriInfo,"filter"),"value");

        Map<String, JsonNode> sortingProperties;
        sortingProperties = getFilterProperties(getQueryValue(uriInfo,"sort"),"direction");

        int startIndex = 0;
        int endIndex = Integer.MAX_VALUE;
        try {
            startIndex = Integer.valueOf(getQueryValue(uriInfo, "start"));
            endIndex = Integer.valueOf(getQueryValue(uriInfo, "limit"));
            endIndex++;
        }catch (NumberFormatException e){
        }

        final JsonNode value = filterProperties.get("searchInAllProcesses");
        final boolean searchInAllProcesses = Boolean
                .valueOf(String.valueOf(value).replace("\"", "").replaceAll("\'", ""));

        EntityManager em = emf.createEntityManager();
        String queryString = "select p.STATUS, p.PROCESSINSTANCEID as processLogid, p.PROCESSNAME, p.PROCESSVERSION, p.USER_IDENTITY, p.START_DATE, p.END_DATE, p.DURATION , v.VALUE, v.VARIABLEID"
                + " from processinstancelog p"
                + " LEFT JOIN (select count(*) as VARCOUNT, v1.PROCESSINSTANCEID as VPID from VARIABLEINSTANCELOG v1"
                + " where v1.VARIABLEID in ('alarmId', 'issueId', 'deviceId', 'usagePointId')"
                + " group by v1.PROCESSINSTANCEID) ON VPID = p.PROCESSINSTANCEID"
                + " LEFT JOIN VARIABLEINSTANCELOG v ON p.PROCESSINSTANCEID = v.PROCESSINSTANCEID and VARCOUNT = 1 and v.VARIABLEID in ('alarmId', 'issueId', 'deviceId', 'usagePointId')";
        if (searchInAllProcesses) {
            queryString += addProcessInstanceIdFilterToQuery(filterProperties);
            queryString += addSortingToQuery(sortingProperties);
        } else {
            String filterToQuery = addFilterToQuery(filterProperties, false).trim();
            if (filterToQuery.startsWith("AND") || filterToQuery.startsWith("and") || filterToQuery.startsWith("And")) {
                filterToQuery = "WHERE " + filterToQuery.substring(3);
            }
            queryString += filterToQuery;
            queryString += addSortingToQuery(sortingProperties);
        }

        Query query = em.createNativeQuery(queryString);
        query.setFirstResult(startIndex);
        query.setMaxResults(endIndex);
        List<Object[]> list = query.getResultList();
        ProcessHistoryGenInfos processHistoryInfos = new ProcessHistoryGenInfos(list);
        for(ProcessHistoryGenInfo info : processHistoryInfos.processHistories){
            info.tasks = info.processInstanceId == -1 ? null : getTaskForProceessInstance(info.processInstanceId);
        }
        if(processHistoryInfos.total == endIndex){
            int total = startIndex + endIndex;
            processHistoryInfos.removeLast(total);
        }else{
            int total = startIndex + processHistoryInfos.total;
            processHistoryInfos.setTotal(total);
        }

        return processHistoryInfos;
    }

    @GET
    @Path("/process/instance/{processInstanceId: [0-9-]+}/node")
    @Produces("application/json")
    public ProcessInstanceNodeInfos getProcessInstanceNode(@Context UriInfo uriInfo,@PathParam("processInstanceId") long processInstanceId){
        if (emf == null) {
            emf = EntityManagerFactoryManager.get().getOrCreate("org.jbpm.domain");
        }

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
    @Path("/process/instance/{processInstanceId: [0-9-]+}/log")
    @Produces("application/json")
    public ProcessInstanceLogInfo getProcessInstanceLog(@Context UriInfo uriInfo,@PathParam("processInstanceId") long processInstanceId){
        if (emf == null) {
            emf = EntityManagerFactoryManager.get().getOrCreate("org.jbpm.domain");
        }
        EntityManager em = emf.createEntityManager();
        String queryString = "SELECT log.processid, log.processname, log.externalid, log.processversion, log.user_identity, log.outcome, " +
                "log.processinstancedescription, log.duration, log.parentprocessinstanceid, log.status, log.processinstanceid, log.start_date, " +
                "log.end_date FROM processinstancelog log WHERE log.processinstanceid = :processInstanceId";
        Query query = em.createNativeQuery(queryString);
        query.setParameter("processInstanceId", processInstanceId);
        List<Object[]> logs = query.getResultList();
        Object[] log = logs.isEmpty() ? new Object[0] : logs.get(0);
        return new ProcessInstanceLogInfo(log);
    }

    @GET
    @Path("/process/instance/{processInstanceId: [0-9-]+}/log/child")
    @Produces("application/json")
    public ProcessInstanceLogInfos getProcessInstanceChildLog(@Context UriInfo uriInfo,@PathParam("processInstanceId") long processInstanceId){
        if (emf == null) {
            emf = EntityManagerFactoryManager.get().getOrCreate("org.jbpm.domain");
        }
        EntityManager em = emf.createEntityManager();
        String queryString = "SELECT log.processid, log.processname, log.externalid, log.processversion, log.user_identity, log.outcome, " +
                "log.processinstancedescription, log.duration, log.parentprocessinstanceid, log.status, log.processinstanceid, log.start_date, " +
                "log.end_date FROM processinstancelog log WHERE log.parentprocessinstanceid = :processInstanceId";
        Query query = em.createNativeQuery(queryString);
        query.setParameter("processInstanceId", processInstanceId);
        List<Object[]> logs = query.getResultList();

        return new ProcessInstanceLogInfos(logs);
    }


    private Map<String, JsonNode> getFilterProperties(String source, String value){
        LinkedHashMap<String, JsonNode> filterProperties = new LinkedHashMap<String, JsonNode>();
        try {
            if (source != null) {
                JsonNode node = new ObjectMapper().readValue(new ByteArrayInputStream(source.getBytes()), JsonNode.class);
                if (node != null && node.isArray()) {
                    for (JsonNode singleFilter : node) {
                        JsonNode property = singleFilter.get(PROPERTY);
                        if (property != null && property.textValue() != null)
                            filterProperties.put(property.textValue(), singleFilter.get(value));
                    }
                }
            }
        }catch (Exception e){

        }
        return filterProperties;
    }

    private Map<String, JsonNode> getSortingProperties(String source, String value){
        LinkedHashMap<String, JsonNode> filterProperties = new LinkedHashMap<String, JsonNode>();
        try {
            if (source != null) {
                JsonNode node = new ObjectMapper().readValue(new ByteArrayInputStream(source.getBytes()), JsonNode.class);
                if (node != null && node.isArray()) {
                    for (JsonNode singleFilter : node) {
                        JsonNode property = singleFilter.get(PROPERTY);
                        if (property != null && property.textValue() != null)
                            filterProperties.put(property.textValue(), singleFilter.get(value));
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
                    .map(minimal -> taskService.getTask(minimal.getId()))
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

    private String addProcessInstanceIdFilterToQuery(Map<String, JsonNode> filterProperties) {
        String processInstanceId = "";
        for (Map.Entry<String, JsonNode> entry : filterProperties.entrySet()) {
            final String theKey = entry.getKey();
            if ("processInstanceId".equals(theKey)) {
                final JsonNode jsonProcessInstanceId = filterProperties.get(theKey);
                if (jsonProcessInstanceId.size() > 0) {
                    for (int i = 0; i < jsonProcessInstanceId.size(); i++) {
                        if (processInstanceId.trim().isEmpty()) {
                            processInstanceId += "p.PROCESSINSTANCEID = "
                                    + jsonProcessInstanceId.get(i).toString().replace("\"", "'");
                        } else {
                            processInstanceId += " OR p.PROCESSINSTANCEID = "
                                    + jsonProcessInstanceId.get(i).toString().replace("\"", "'");
                        }
                    }
                } else {
                    processInstanceId += "p.PROCESSINSTANCEID = "
                            + jsonProcessInstanceId.textValue().replace("\"", "'");
                }
            }
        }
        if (!processInstanceId.trim().isEmpty()) {
            processInstanceId = "where ( " + processInstanceId + " ) ";
        }
        return processInstanceId;
    }

    private String addFilterToQuery(Map<String, JsonNode> filterProperties, Boolean onlyHistoryProcesses){
        String process = "";
        String startedOnFrom = "";
        String startedOnTo = "";
        String status = "";
        String startedBy = "";
        String variableId = "";
        String variableValue = "";
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
            if (theKey.equals("value")) {
                for(int i=0;i<filterProperties.get("value").size();i++) {
                    if(variableValue.equals("")) {
                        variableValue += "v.VALUE = " + filterProperties.get("value").get(i).toString().replace("\"","'");
                    }else{
                        variableValue+= " OR v.VALUE = " + filterProperties.get("value").get(i).toString().replace("\"", "'");;
                    }
                }
            }
            if (theKey.equals("variableId")) {
                for(int i=0;i<filterProperties.get("variableId").size();i++) {
                    if(variableId.equals("")) {
                        variableId += "v.VARIABLEID = " + filterProperties.get("variableId").get(i).toString().replace("\"","'");
                    }else{
                        variableId += " OR v.VARIABLEID = " + filterProperties.get("variableId").get(i).toString().replace("\"", "'");;
                    }
                }
            }

        }


        if(!process.equals("")){
            filter += "AND ( " + process + ")";
        }
        if(!status.equals("")){
            filter += "AND ( " + status + ")";
        }else if (onlyHistoryProcesses){
            filter += "AND(p.STATUS = 2 OR p.STATUS = 3 )";
        }else{
            filter += "AND(p.STATUS = 1 OR p.STATUS = 2 OR p.STATUS = 3 )";
        }
        if(!startedBy.equals("")){
            filter += "AND ( " + startedBy + ")";
        }
        if(!variableId.equals("")){
            filter += "AND ( " + variableId + ")";
        }
        if(!variableValue.equals("")){
            filter += "AND ( " + variableValue+ ")";
        }
        if(!startedOnFrom.equals("")){
            filter += startedOnFrom;
        }
        if(!startedOnTo.equals("")){
            filter += startedOnTo;
        }
        //filter += order;
        return filter;
    }

    private String addSortingToQuery(Map<String, JsonNode> sortProperties){
        String order = "";
        Iterator<String> it = sortProperties.keySet().iterator();
        ArrayList<String> orders = new ArrayList<>();
        while(it.hasNext()) {
            String theKey = (String) it.next();
            if (theKey.equals("processId")) {
                String sortOrder = sortProperties.get("processId").toString().replace("\"", "");

                if (sortOrder.equals("desc")) {
                    orders.add(" p.PROCESSINSTANCEID DESC");
                } else if (sortOrder.equals("asc")) {
                    orders.add(" p.PROCESSINSTANCEID ");
                }
            }
            if (theKey.equals("startDate")) {
                String sortOrder = sortProperties.get("startDate").toString().replace("\"", "");

                if (sortOrder.equals("desc")) {
                    orders.add(" p.START_DATE DESC");
                } else if (sortOrder.equals("asc")) {
                    orders.add(" p.START_DATE ");
                }
            }
        }

        if(orders.size() != 0)
        {
            order += "order by";
            order += orders.get(0);
            if (orders.size() > 1){
                for (int i = 1; i < orders.size(); i++){
                    order += ", ";
                    order += orders.get(i);
                }
            }
        }else{
            order += "order by p.PROCESSINSTANCEID DESC";
        }
        return order;
    }


//    // Supported HTTP method, path parameters, and data formats:
//    @POST
//    @Path("/server/containers/instances/{containerId}/ksession/{ksessionId}")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response insertFireReturn(@Context HttpHeaders headers,
//                                     @PathParam("containerId") String id,
//                                     @PathParam("ksessionId") String ksessionId,
//                                     String cmdPayload) {
//
////        Variant v = getVariant(headers);
//        String contentType = headers.getMediaType().toString();
//
//        // Marshalling behavior and supported actions:
//        MarshallingFormat format = MarshallingFormat.fromType(contentType);
//        if (format == null) {
//            format = MarshallingFormat.valueOf(contentType);
//        }
//        try {
//            KieContainerInstance kci = registry.getContainer(id);
//
//            Marshaller marshaller = kci.getMarshaller(format);
//
//            List<?> listOfFacts = marshaller.unmarshall(cmdPayload, List.class);
//
//            List<Command<?>> commands = new ArrayList<Command<?>>();
//            BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, ksessionId);
//
//            for (Object fact : listOfFacts) {
//                commands.add(commandsFactory.newInsert(fact, fact.toString()));
//            }
//            commands.add(commandsFactory.newFireAllRules());
//            commands.add(commandsFactory.newGetObjects());
//
//            ExecutionResults results = rulesExecutionService.call(kci, executionCommand);
//
//            String result = marshaller.marshall(results);
//
//
//            logger.debug("Returning OK response with content '{}'", result);
//            return Response.ok().entity(result).build();
//        } catch (Exception e) {
//            // If marshalling fails, return the `call-container` response to maintain backward compatibility:
//            String response = "Execution failed with error : " + e.getMessage();
//            logger.debug("Returning Failure response with content '{}'", response);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
//        }
//    }
}
