package com.elster.jupiter.bpm.rest.impl;

import com.elster.jupiter.bpm.*;
import com.elster.jupiter.bpm.rest.*;
import com.elster.jupiter.bpm.rest.resource.StandardParametersBean;
import com.elster.jupiter.bpm.security.Privileges;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.*;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/runtime")
public class BpmResource {

    public static final String ME = "me";
    public static final String LIKE = "like";

    private final UserService userService;
    private final Thesaurus thesaurus;
    private final BpmService bpmService;
    private final TransactionService transactionService;
    private final PropertyUtils propertyUtils;

    private final String errorNotFoundMessage;
    private final String errorInvalidMessage;

    @Inject
    public BpmResource(BpmService bpmService, UserService userService, TransactionService transactionService, Thesaurus thesaurus, PropertyUtils propertyUtils) {
        this.bpmService = bpmService;
        this.userService = userService;
        this.transactionService = transactionService;
        this.thesaurus = thesaurus;
        this.propertyUtils = propertyUtils;

        this.errorNotFoundMessage = thesaurus.getString("error.flow.unavailable", "Cannot connect to Flow; HTTP error {0}.");
        this.errorInvalidMessage = thesaurus.getString("error.flow.invalid.response", "Invalid response received, please check your Flow version.");
    }

    @GET
    @Path("/deployments")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_BPM)
    public DeploymentInfos getAllDeployments(@Context UriInfo uriInfo, @HeaderParam("Authorization") String auth) {
        return getAllDeployments(auth);
    }

    @GET
    @Path("/startup")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_BPM)
    public StartupInfo getStartup(@Context UriInfo uriInfo) {
        StartupInfo startupInfo = new StartupInfo();
        BpmServer server = bpmService.getBpmServer();
        startupInfo.url = server.getUrl();

        return startupInfo;
    }

    @GET
    @Path("/instances")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_BPM)
    public ProcessInstanceInfos getAllInstances(@Context UriInfo uriInfo, @HeaderParam("Authorization") String auth) {
        String jsonContent;
        JSONArray arr = null;
        DeploymentInfos deploymentInfos = getAllDeployments(auth);
        if (deploymentInfos != null && deploymentInfos.total > 0) {
            // Apparently - although not in line with the documentation - all instances are returned regardless of the deployment id
            // For future versions, we need to revise if this behavior changes
            //for (DeploymentInfo deployment : deploymentInfos.getDeployments()) {
            try {
                DeploymentInfo deployment = deploymentInfos.deployments.get(0);
                jsonContent = bpmService.getBpmServer().doGet("/rest/runtime/" + deployment.identifier + "/history/instances", auth);
                if (!"".equals(jsonContent)) {
                    JSONObject obj = new JSONObject(jsonContent);
                    arr = obj.getJSONArray("result");
                }
            } catch (JSONException e) {
                throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
            } catch (RuntimeException e) {
                throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(String.format(this.errorNotFoundMessage, e.getMessage())).build());
            }
        }
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        return new ProcessInstanceInfos(arr, queryParameters.getLimit(), queryParameters.getStartInt());
    }

    @GET
    @Path("/deployment/{deploymentId}/instance/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_BPM)
    public ProcessInstanceInfo getInstance(@Context UriInfo uriInfo, @PathParam("deploymentId") String deploymentId,
                                           @PathParam("id") long instanceId, @HeaderParam("Authorization") String auth) {
        JSONObject obj = null;
        String jsonContent;
        try {
            jsonContent = bpmService.getBpmServer().doGet("/rest/runtime/" + deploymentId + "/history/instance/" + instanceId, auth);
            if (!"".equals(jsonContent)) {
                obj = (new JSONObject(jsonContent)).getJSONArray("result").getJSONObject(0);
            }
        } catch (JSONException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
        } catch (RuntimeException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(String.format(this.errorNotFoundMessage, e.getMessage())).build());
        }
        return new ProcessInstanceInfo(obj);
    }

    @GET
    @Path("/deployment/{deploymentId}/instance/{id}/nodes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_BPM)
    public NodeInfos getNodes(@Context UriInfo uriInfo, @PathParam("deploymentId") String deploymentId,
                              @PathParam("id") long instanceId, @HeaderParam("Authorization") String auth) {
        JSONArray arr = null;
        String jsonContent;
        try {
            jsonContent = bpmService.getBpmServer().doGet("/rest/runtime/" + deploymentId + "/history/instance/" + instanceId + "/node", auth);
            if (!"".equals(jsonContent)) {
                arr = (new JSONObject(jsonContent)).getJSONArray("result");
            }
        } catch (JSONException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
        } catch (RuntimeException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(String.format(this.errorNotFoundMessage, e.getMessage())).build());
        }
        return new NodeInfos(arr);
    }


    @GET
    @Path("/deployment/{deploymentId}/instance/{id}/variables")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_BPM)
    public VariableInfos getVariables(@Context UriInfo uriInfo, @PathParam("deploymentId") String deploymentId,
                                      @PathParam("id") long instanceId, @HeaderParam("Authorization") String auth) {
        JSONArray arr = null;
        String jsonContent;
        try {
            jsonContent = bpmService.getBpmServer().doGet("/rest/runtime/" + deploymentId + "/history/instance/" + instanceId + "/variable", auth);
            if (!"".equals(jsonContent)) {
                arr = (new JSONObject(jsonContent)).getJSONArray("result");
            }
        } catch (JSONException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
        } catch (RuntimeException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(String.format(this.errorNotFoundMessage, e.getMessage())).build());
        }
        return new VariableInfos(arr);
    }

    private DeploymentInfos getAllDeployments(String auth) {
        String jsonContent;
        JSONArray arr = null;
        try {
            jsonContent = bpmService.getBpmServer().doGet("/rest/deployment", auth);
            if (!"".equals(jsonContent)) {
                arr = new JSONArray(jsonContent);
            }
        } catch (JSONException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
        } catch (RuntimeException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(String.format(this.errorNotFoundMessage, e.getMessage())).build());
        }
        return new DeploymentInfos(arr);
    }

    @GET
    @Path("/tasks")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK, Privileges.Constants.ASSIGN_TASK, Privileges.Constants.EXECUTE_TASK})
    public TaskInfos getTask(@Context UriInfo uriInfo, @BeanParam JsonQueryFilter filterX, @HeaderParam("Authorization") String auth) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters(false));
        String jsonContent;
        int total = -1;
        JSONArray arr = null;
        try {
            String rest = "/rest/tasks";
            String req = getQueryParam(queryParameters);
            if (!req.equals("")) {
                rest += req;
            }
            List<String> deployemntIds = getProcesses(uriInfo, auth).processes.stream()
                    .map(s -> s.deploymentId)
                    .collect(Collectors.toList());
            for(String each : deployemntIds){
                rest += "&deploymentid=" + each;
            }
            jsonContent = bpmService.getBpmServer().doGet(rest, auth);
            if (!"".equals(jsonContent)) {
                JSONObject obj = new JSONObject(jsonContent);
                total = Integer.valueOf(obj.get("total").toString());
                arr = obj.getJSONArray("tasks");
            }
        } catch (JSONException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
        } catch (RuntimeException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(String.format(this.errorNotFoundMessage, e.getMessage())).build());
        }
        TaskInfos infos = new TaskInfos(arr);
        if(total > 0){
            infos.total = total;
        }
        return infos;
    }

    @GET
    @Path("/tasks/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK, Privileges.Constants.ASSIGN_TASK, Privileges.Constants.EXECUTE_TASK})
    public TaskInfo getTask(@PathParam("id") long id, @HeaderParam("Authorization") String auth) {
        String jsonContent;
        TaskInfo taskInfo = new TaskInfo();
        try {
            String rest = "/rest/tasks/";
            rest += String.valueOf(id);
            jsonContent = bpmService.getBpmServer().doGet(rest, auth);
            if (!"".equals(jsonContent)) {
                JSONObject obj = new JSONObject(jsonContent);
                taskInfo = new TaskInfo(obj);
            }

        } catch (JSONException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
        } catch (RuntimeException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(String.format(this.errorNotFoundMessage, e.getMessage())).build());
        }
        return taskInfo;
    }

    @GET
    @Path("/process/instance/{processInstanceId}/nodes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK, Privileges.Constants.ASSIGN_TASK, Privileges.Constants.EXECUTE_TASK})
    public ProcessInstanceNodeInfos getProcessInstanceNode(@Context UriInfo uriInfo,
                                       @HeaderParam("Authorization") String auth,
                                       @PathParam("processInstanceId") long processInstanceId) {
        String jsonContent;
        JSONObject jsnobject = null;
        try {
            jsonContent = bpmService.getBpmServer().doGet("/rest/tasks/process/instance/"+processInstanceId+"/node", auth);
            if (!"".equals(jsonContent)) {
                jsnobject = new JSONObject(jsonContent);
            }
        } catch (JSONException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
        } catch (RuntimeException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(String.format(this.errorNotFoundMessage, e.getMessage())).build());
        }
        return new ProcessInstanceNodeInfos(jsnobject, thesaurus);


    }

    @GET
    @Path("/processes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK, Privileges.Constants.ASSIGN_TASK, Privileges.Constants.EXECUTE_TASK})
    public ProcessDefinitionInfos getProcesses(@Context UriInfo uriInfo, @HeaderParam("Authorization") String auth) {
        MultivaluedMap<String, String> filterProperties = uriInfo.getQueryParameters();
        if(filterProperties.get("type") != null) {
            List<BpmProcessDefinition> activeProcesses = bpmService.getAllBpmProcessDefinitions();
            ProcessDefinitionInfos processDefinitionInfos = getBpmProcessDefinitions(auth);
            processDefinitionInfos.processes = processDefinitionInfos.processes.stream()
                    .filter(s -> activeProcesses.stream()
                            .anyMatch(a -> a.getProcessName().equals(s.name) && a.getVersion()
                                    .equals(s.version) && a.getAssociation().isPresent() && a.getAssociation()
                                    .get()
                                    .getType()
                                    .toLowerCase()
                                    .equals(filterProperties.get("type").get(0).toLowerCase())))
                    .collect(Collectors.toList());
            processDefinitionInfos.processes.stream()
                    .forEach(s -> s.id = s.id + " (" + s.deploymentId + ") ");
            processDefinitionInfos.total = processDefinitionInfos.processes.size();
            return processDefinitionInfos;
        }else{
            List<BpmProcessDefinition> activeProcesses = bpmService.getAllBpmProcessDefinitions();
            ProcessDefinitionInfos processDefinitionInfos = getBpmProcessDefinitions(auth);
            processDefinitionInfos.processes = processDefinitionInfos.processes.stream()
                    .filter(s -> activeProcesses.stream().anyMatch(a -> a.getProcessName().equals(s.name) && a.getVersion().equals(s.version)))
                    .collect(Collectors.toList());
            processDefinitionInfos.processes.stream()
                    .forEach(s -> s.id = s.id + " (" + s.deploymentId + ") ");
            processDefinitionInfos.total = processDefinitionInfos.processes.size();
            return processDefinitionInfos;
        }
    }

    @GET
    @Path("/assignees/users")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK, Privileges.Constants.ASSIGN_TASK, Privileges.Constants.EXECUTE_TASK})
    public Response getUsers(@BeanParam StandardParametersBean params) {
        String searchText = params.getFirst(LIKE);
        Condition condition = Condition.TRUE;
        if (searchText != null && !searchText.isEmpty()) {
            String dbSearchText = "*" + searchText + "*";
            condition = condition.and(where("authenticationName").likeIgnoreCase(dbSearchText));
        }
        Query<User> query = userService.getUserQuery();
        List<User> list = query.select(condition, Order.ascending("authenticationName"));
        return Response.ok(new AssigneeFilterListInfo(list)).build();
    }


    @POST
    @Path("tasks/{id}/assign")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ASSIGN_TASK)
    public Response assignUser(@Context UriInfo uriInfo, @PathParam("id") long id, @Context SecurityContext securityContext, @HeaderParam("Authorization") String auth) {
        String response;
        String userName = getQueryValue(uriInfo, "username");
        String rest = "/rest/tasks/";
        rest += String.valueOf(id);
        if (userName != null && !userName.isEmpty()) {
            rest += "/assign?username=" + userName;
            rest += "&currentuser=" + securityContext.getUserPrincipal().getName();
            try {
                response = bpmService.getBpmServer().doPost(rest, null, auth, 0);
            } catch (RuntimeException e) {
                throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(String.format(this.errorNotFoundMessage, e.getMessage())).build());
            }
            if (response == null) {
                throw new BpmResourceAssignUserException(thesaurus);
            }
            return Response.ok().build();
        }

        throw new LocalizedFieldValidationException(MessageSeeds.FIELD_CAN_NOT_BE_EMPTY, "assignee");
    }

    @GET
    @Path("/assignees")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK, Privileges.Constants.ASSIGN_TASK, Privileges.Constants.EXECUTE_TASK})
    public PagedInfoListCustomized getAllAssignees(@BeanParam StandardParametersBean params, @BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext) {
        if (Boolean.parseBoolean(params.getFirst(ME))) {
            AssigneeFilterListInfo assigneeFilterListInfo = AssigneeFilterListInfo.defaults((User) securityContext.getUserPrincipal(), thesaurus, true);
            return PagedInfoListCustomized.fromPagedList("data", assigneeFilterListInfo.getData(), queryParameters, 0);
        }
        String searchText = params.getFirst(LIKE);
        String dbSearchText = (searchText != null && !searchText.isEmpty()) ? ("*" + searchText + "*") : "*";
        Condition conditionUser = where("authenticationName").likeIgnoreCase(dbSearchText);
        Query<User> queryUser = userService.getUserQuery();
        AssigneeFilterListInfo assigneeFilterListInfo;
        if (params.getStart() == 0 && (searchText == null || searchText.isEmpty())) {
            assigneeFilterListInfo = AssigneeFilterListInfo.defaults((User) securityContext.getUserPrincipal(), thesaurus, false);
            List<User> listUsers = queryUser.select(conditionUser, params.getFrom(), params.getTo(), Order.ascending("authname"));
            assigneeFilterListInfo.addData(listUsers);
        } else {
            List<User> listUsers = queryUser.select(conditionUser, Order.ascending("authname"));
            assigneeFilterListInfo = new AssigneeFilterListInfo(listUsers);
        }
        return PagedInfoListCustomized.fromPagedList("data", assigneeFilterListInfo.getData(), queryParameters, params.getStart() == 0 ? 1 : 0);
    }

    @POST
    @Path("tasks/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ASSIGN_TASK)
    public Response assignUser(@Context UriInfo uriInfo, @PathParam("id") long id, @HeaderParam("Authorization") String auth) {
        String priority = getQueryValue(uriInfo, "priority");
        String date = getQueryValue(uriInfo, "duedate");
        String rest = "/rest/tasks/";
        rest += String.valueOf(id) + "/set";
        if (priority != null || date != null) {
            if (priority != null) {
                rest += "?priority=" + priority;
                if (date != null) {
                    rest += "&duedate=" + date;
                }
            } else {
                rest += "?duedate=" + date;
            }
            try {
                bpmService.getBpmServer().doPost(rest, null, auth);
            } catch (RuntimeException e) {
                throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(String.format(this.errorNotFoundMessage, e.getMessage())).build());
            }
            return Response.ok().build();
        }
        return Response.notModified().build();
    }

    @PUT
    @Transactional
    @Path("/process/activate/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_BPM)
    public Response activateProcess(ProcessDefinitionInfo info) {
        List<Errors> err = new ArrayList<>();

        BpmProcessDefinition process;
        Optional<BpmProcessDefinition> foundProcess = bpmService.getBpmProcessDefinition(info.name, info.version);
        if (!foundProcess.isPresent()) {
            BpmProcessDefinitionBuilder processBuilder = bpmService.newProcessBuilder()
                    .setId(info.id).setProcessName(info.name)
                    .setAssociation(info.associatedTo)
                    .setVersion(info.version)
                    .setStatus(info.active)
                    .setProperties(info.properties);
            process = processBuilder.create();
        } else {
            process = foundProcess.get();
            process.setAssociation(info.associatedTo);
            process.setStatus(info.active);
            process.setProperties(info.properties);
        }
        /*if (provider.isPresent()) {
            //TO DO: rename properties to associationData
            //provider.get().update(bpmProcessDefinition.get(),info.associationData);
            try {
                provider.get().checkPresentAssociationData(info.properties);
            } catch (IllegalStateException e) {
                err.add(new Errors(e.getMessage(), MessageSeeds.FIELD_CAN_NOT_BE_EMPTY.getDefaultFormat()));
            }
            provider.get().update(bpmProcessDefinition.get(), info.properties);
        }
        if (info.privileges.isEmpty()) {
            err.add(new Errors("noPrivileges", MessageSeeds.FIELD_CAN_NOT_BE_EMPTY.getDefaultFormat()));
        }

        if (!err.isEmpty()) {
            return Response.status(400).entity(new LocalizedFieldException(err)).build();
        }*/

        doUpdatePrivileges(process, info);
        process.update();
        return Response.ok().build();
    }


    @PUT
    @Transactional
    @Path("/process/deactivate/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_BPM)
    public ProcessDefinitionInfo deactivateProcess(@PathParam("id") String id, ProcessDefinitionInfo info) {
        Optional<BpmProcessDefinition> bpmProcessDefinition = bpmService.getBpmProcessDefinition(info.name, info.version);
        if (bpmProcessDefinition.isPresent()) {
            bpmProcessDefinition.get().setStatus(info.active);
            bpmProcessDefinition.get().update();
            return new ProcessDefinitionInfo(bpmProcessDefinition.get());
        }

        throw new BpmProcessNotAvailable(thesaurus, id + ":" + info.version);
    }

    @GET
    @Path("/process/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.ADMINISTRATE_BPM})
    public ProcessDefinitionInfo getBpmProcessDefinition(@PathParam("id") String id, @Context UriInfo uriInfo, @HeaderParam("Authorization") String auth) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        if (queryParameters.get("version") != null) {
            String version = queryParameters.get("version").get(0);
            Optional<BpmProcessDefinition> bpmProcessDefinition = bpmService.getBpmProcessDefinition(id, version);

            if (bpmProcessDefinition.isPresent()) {
                List<Group> groups = this.userService.getGroups();
                ProcessDefinitionInfo processDefinitionInfo = new ProcessDefinitionInfo(bpmProcessDefinition.get(), groups);
                bpmProcessDefinition.get().getAssociation()
                        .ifPresent(provider -> processDefinitionInfo.setPropertySpecs(propertyUtils.convertPropertySpecsToPropertyInfos(provider
                                .getPropertySpecs())));

                return processDefinitionInfo;
            }else{
                ProcessDefinitionInfo processDefinitionInfo = getBpmProcessDefinitions(auth).processes.stream()
                        .filter(s -> s.id.equals(id) && s.version.equals(version)).findFirst()
                        .orElseThrow(() -> new BpmProcessNotAvailable(thesaurus, id + ":" + version));

                if (queryParameters.get("association") != null) {
                    String association = queryParameters.get("association").get(0);
                    bpmService.getProcessAssociationProvider(association)
                            .ifPresent(provider -> processDefinitionInfo.setPropertySpecs(propertyUtils.convertPropertySpecsToPropertyInfos(provider
                                    .getPropertySpecs())));
                }

                return processDefinitionInfo;
            }
        }
        return null;
    }


    // TODO: GENERIC
    @GET
    @Path("/activeprocesses")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.ADMINISTRATE_BPM})
    public PagedInfoList getActiveBpmProcessesDefinitions(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters, @HeaderParam("Authorization") String auth) {
        MultivaluedMap<String, String> filterProperties = uriInfo.getQueryParameters();
        if(filterProperties.get("type") != null) {
            if (filterProperties.get("type").get(0).toLowerCase().equals("device")) {
                if (filterProperties.get("devicestateid") != null && filterProperties.get("privileges") != null) {
                    List<String> privileges = getPropertyList(filterProperties.get("privileges").get(0), "privilege");
                    ProcessDefinitionInfos bpmProcessDefinition = getBpmProcessDefinitions(auth);
                    long deviceStateId = Long.valueOf(filterProperties.get("devicestateid").get(0));
                    List<String> privilegeNames = privileges.stream().collect(Collectors.toList());
                    List<BpmProcessDefinition> connexoProcesses = bpmService.getActiveBpmProcessDefinitions();
                    List<BpmProcessDefinition> filtredConnexoProcesses = connexoProcesses.stream()
                            //.filter(p -> p.getAssociationData().stream().anyMatch(s -> Long.parseLong(s.get("deviceStateId")) == deviceStateId))
                            .filter(p -> p.getPrivileges().stream().anyMatch(s -> privilegeNames.stream().anyMatch(z -> z.equals(s.getPrivilegeName()))))
                            .filter(p -> p.getAssociation().isPresent() && p.getAssociation()
                                    .get()
                                    .getType()
                                    .toLowerCase()
                                    .equals(filterProperties.get("type").get(0).toLowerCase()))
                            .collect(Collectors.toList());

                    List<ProcessDefinitionInfo> bpmProcesses = bpmProcessDefinition.processes.stream()
                            .filter(s -> filtredConnexoProcesses.stream().anyMatch(x -> x.getProcessName().equals(s.name) && x.getVersion().equals(s.version)))
                            .collect(Collectors.toList());
                    bpmProcesses.stream()
                            .forEach(s -> s.id = s.id + s.deploymentId);
                    return PagedInfoList.fromCompleteList("processes", bpmProcesses, queryParameters);
                }
            }else if(filterProperties.get("type").get(0).toLowerCase().equals("usagepoint") || filterProperties.get("type").get(0).toLowerCase().equals("issue")){
                ProcessDefinitionInfos bpmProcessDefinition = getBpmProcessDefinitions(auth);
                List<BpmProcessDefinition> connexoProcesses = bpmService.getActiveBpmProcessDefinitions();
                List<BpmProcessDefinition> filtredConnexoProcesses = connexoProcesses.stream()
                        .filter(p -> p.getAssociation().isPresent() && p.getAssociation()
                                .get()
                                .getType()
                                .toLowerCase()
                                .equals(filterProperties.get("type").get(0).toLowerCase()))
                        .collect(Collectors.toList());
                List<ProcessDefinitionInfo> bpmProcesses = bpmProcessDefinition.processes.stream()
                        .filter(s -> filtredConnexoProcesses.stream().anyMatch(x -> x.getProcessName().equals(s.name) && x.getVersion().equals(s.version)))
                        .collect(Collectors.toList());
                bpmProcesses.stream()
                        .forEach(s -> s.id = s.id + s.deploymentId);
                return PagedInfoList.fromCompleteList("processes", bpmProcesses, queryParameters);
            }
        }
        return null;
    }

    private ProcessDefinitionInfos getBpmProcessDefinitions(String auth) {
        String jsonContent;
        JSONArray arr = null;
        try {
            jsonContent = bpmService.getBpmServer().doGet("/rest/deployment/processes", auth);
            if (!"".equals(jsonContent)) {
                JSONObject jsnobject = new JSONObject(jsonContent);
                arr = jsnobject.getJSONArray("processDefinitionList");
            }
        } catch (JSONException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
        } catch (RuntimeException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(String.format(this.errorNotFoundMessage, e.getMessage())).build());
        }
        return new ProcessDefinitionInfos(arr);
    }

    @GET
    @Path("/allprocesses")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.ADMINISTRATE_BPM})
    public PagedInfoList getBpmProcessesDefinitions(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters, @HeaderParam("Authorization") String auth, @Context HttpHeaders headers) {
        try (TransactionContext context = transactionService.getContext()) {
            List<BpmProcessDefinition> connexoProcesses = bpmService.getBpmProcessDefinitions();
            ProcessDefinitionInfos bpmProcessDefinition = getBpmProcessDefinitions(auth);
            for (BpmProcessDefinition eachConnexo : connexoProcesses) {
                boolean found = false;
                for (ProcessDefinitionInfo eachBpm : bpmProcessDefinition.processes) {
                    if (eachConnexo.getProcessName().equals(eachBpm.name) && eachConnexo.getVersion().equals(eachBpm.version)) {
                        eachBpm.active = eachConnexo.getStatus();
                        eachBpm.associatedTo = eachConnexo.getAssociation().isPresent() ? eachConnexo.getAssociation()
                                .get()
                                .getType() : "";
                        found = true;
                    }
                }
                if (!found && !bpmProcessDefinition.processes.isEmpty()) {
                    eachConnexo.setStatus("UNDEPLOYED");
                    eachConnexo.update();
                }
            }
            List<ProcessDefinitionInfo> list = bpmProcessDefinition.processes.stream()
                    .sorted((s1, s2) -> s1.name.toLowerCase().compareTo(s2.name.toLowerCase()))
                    .collect(Collectors.toList());
            list.stream().forEach(s -> s.id = s.id + s.version);
            context.commit();
            return PagedInfoList.fromCompleteList("processes", list, queryParameters);
        }
    }

    @GET
    @Path("/runningprocesses")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.ADMINISTRATE_BPM})
    public RunningProcessInfos getRunningProcesses(@Context UriInfo uriInfo, @HeaderParam("Authorization") String auth) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters(false));
        String jsonContent;
        int total = -1;
        JSONArray arr = null;
        try {
            String rest = "/rest/tasks/runningprocesses";
            String req = getQueryParam(queryParameters);
            if (!req.equals("")) {
                rest += req;
            }
            jsonContent = bpmService.getBpmServer().doGet(rest, auth);
            if (!"".equals(jsonContent)) {
                JSONObject obj = new JSONObject(jsonContent);
                total = Integer.valueOf(obj.get("total").toString());
                arr = obj.getJSONArray("processInstances");
            }

        } catch (JSONException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
        } catch (RuntimeException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(String.format(this.errorNotFoundMessage, e.getMessage())).build());
        }
        List<BpmProcessDefinition> activeProcesses = bpmService.getActiveBpmProcessDefinitions();
        RunningProcessInfos runningProcessInfos = new RunningProcessInfos(arr);
        List<RunningProcessInfo> runningProcessesList = runningProcessInfos.processes.stream()
                .filter(s -> activeProcesses.stream().anyMatch(a -> s.name.equals(a.getProcessName()) && s.version.equals(a.getVersion())))
                .collect(Collectors.toList());
        runningProcessInfos.processes = runningProcessesList;
        if (total == Integer.valueOf(queryParameters.get("page").get(0)) * runningProcessInfos.total + 1) {
            runningProcessInfos.total = total;
        }else{
            runningProcessInfos.total = Integer.valueOf(queryParameters.get("page").get(0)) * 10 - 10 + runningProcessesList.size();
        }
        return runningProcessInfos;
    }

    @GET
    @Path("/historyprocesses")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.ADMINISTRATE_BPM})
    public ProcessHistoryInfos getProcessHistory(@Context UriInfo uriInfo, @HeaderParam("Authorization") String auth) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters(false));
        String jsonContent;
        int total = -1;
        JSONArray arr = null;
        try {
            String rest = "/rest/tasks/process/history";
            String req = getQueryParam(queryParameters);
            if (!req.equals("")) {
                rest += req;
            }
            jsonContent = bpmService.getBpmServer().doGet(rest, auth);
            if (!"".equals(jsonContent)) {
                JSONObject obj = new JSONObject(jsonContent);
                total = Integer.valueOf(obj.get("total").toString());
                arr = obj.getJSONArray("processHistories");
            }

        } catch (JSONException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
        } catch (RuntimeException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(String.format(this.errorNotFoundMessage, e.getMessage())).build());
        }
        ProcessHistoryInfos processHistoryInfos = new ProcessHistoryInfos(arr);
        if (total > 0) {
            processHistoryInfos.total = total;
        }
        return processHistoryInfos;
    }

    @POST
    @Path("/managetasks")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.EXECUTE_TASK)
    public Response manageTasks(TaskGroupsInfos taskGroupsInfos, @Context UriInfo uriInfo, @Context SecurityContext securityContext, @HeaderParam("Authorization") String auth) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters(false));
        String result;
        JSONObject obj = null;
        try {
            taskGroupsInfos.taskGroups.stream()
                    .forEach(s->{
                        s.outputBindingContents = getOutputContent(s.tasksForm,s.taskIds.get(0), null, auth);
                        s.tasksForm = null;
                    });
            ObjectMapper mapper = new ObjectMapper();
            String stringJson = null;
            try {
                stringJson = mapper.writeValueAsString(taskGroupsInfos);
                String rest = "/rest/tasks/managetasks";
                String req = getQueryParam(queryParameters);
                if (!req.equals("")) {
                    rest += req+"&currentuser=" + securityContext.getUserPrincipal().getName() ;
                }else{
                    rest += req+"?currentuser=" + securityContext.getUserPrincipal().getName() ;
                }
                result = bpmService.getBpmServer().doPost(rest, stringJson, auth, 0);
                if (result != null) {
                    obj = new JSONObject(result);
                }
            } catch (JsonProcessingException e) {
            } catch (JSONException e) {
                throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
            }
        } catch (RuntimeException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(String.format(this.errorNotFoundMessage, e.getMessage())).build());
        }
        if(obj == null){
            return Response.status(400).build();
        }
        TaskBulkReportInfo taskBulkReportInfo = new TaskBulkReportInfo(obj);
        if(taskBulkReportInfo.failed > 0){
            return Response.status(400).entity(taskBulkReportInfo).build();
        }
        return Response.ok().entity(taskBulkReportInfo).build();
    }

    @POST
    @Path("tasks/mandatory")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.EXECUTE_TASK, Privileges.Constants.ASSIGN_TASK})
    public TaskGroupsInfos getTaskContent(TaskGroupsInfos taskGroupsInfos, @HeaderParam("Authorization") String auth, @Context UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters(false));
        String response = null;
        JSONArray arr = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            String stringJson = null;
            try {
                stringJson = mapper.writeValueAsString(taskGroupsInfos);
                String rest = "/rest/tasks/mandatory";
                response = bpmService.getBpmServer().doPost(rest, stringJson, auth, 0);
            } catch (JsonProcessingException e) {
            }
            if (response != null) {
                if (!response.equals("Connection refused: connect")) {
                    arr = (new JSONObject(response)).getJSONArray("taskGroups");
                }else {
                    throw new NoBpmConnectionException(thesaurus);
                }
            }

        } catch (JSONException e) {
        }
        TaskGroupsInfos taskGroups = new TaskGroupsInfos(arr);
        taskGroups.taskGroups.stream()
                .forEach(s-> {
            s.tasksForm.outputContent = null;
            s.tasksForm.properties.stream().forEach(f -> {
                if(f.propertyValueInfo != null) {
                    f.propertyValueInfo.defaultValue = "";
                }
            });
        });
        taskGroups.taskGroups = taskGroups.taskGroups.stream()
                .sorted((s1, s2) -> s1.name.toLowerCase().compareTo(s2.name.toLowerCase()))
                .collect(Collectors.toList());
        return taskGroups;
    }

    @GET
    @Path("/processes/privileges")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.ADMINISTRATE_BPM})
    public PagedInfoList getProcessesPrivileges(@BeanParam JsonQueryParameters queryParameters) {
        List<ProcessesPrivilegesInfo> proc = new ArrayList<>();
        Optional<Resource> resource = userService.getResources()
                .stream()
                .filter(s -> s.getName()
                        .equals(Privileges.PROCESS_EXECUTION_LEVELS.getKey()))
                .findFirst();
        if(resource.isPresent()){
            List<Group> groups = this.userService.getGroups();
            proc = resource.get().getPrivileges().stream()
                    .map(s -> new ProcessesPrivilegesInfo(s.getName(), Privileges.getDescriptionForKey(s.getName()), resource.get().getComponentName(), groups))
                    .collect(Collectors.toList());
        }
        return PagedInfoList.fromCompleteList("privileges", proc, queryParameters);
    }

    @GET
    @Path("taskcontent/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.EXECUTE_TASK, Privileges.Constants.ASSIGN_TASK})
    public TaskContentInfos getTaskContent(@PathParam("id") long id, @HeaderParam("Authorization") String auth) {
        String jsonContent;
        JSONObject obj = null;
        TaskContentInfos taskContentInfos = null;
        try {
            String rest = "/rest/tasks/" + id + "/content";
            jsonContent = bpmService.getBpmServer().doGet(rest, auth);
            if (!"".equals(jsonContent)) {
                if(!jsonContent.equals("Connection refused: connect")){
                    obj = new JSONObject(jsonContent);
                }else {
                    throw new NoBpmConnectionException(thesaurus);
                }
            }

        } catch (JSONException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
        }
        if(obj != null) {
            taskContentInfos = new TaskContentInfos(obj);
        }
        return taskContentInfos;
    }

    @GET
    @Path("/processcontent/{id}/{deploymentId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.ADMINISTRATE_BPM})
    public TaskContentInfos getProcessContent(@PathParam("id") String id,
                                              @PathParam("deploymentId") String deploymentId,
                                              @HeaderParam("Authorization") String auth) {
        String jsonContent;
        String processId = null;
        try {
            processId = id.replace(URLDecoder.decode(deploymentId, "UTF-8"),"");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        JSONObject obj = null;
        TaskContentInfos taskContentInfos = null;
        try {
            String rest = "/rest/tasks/process/" + deploymentId + "/content/"+ processId;
            jsonContent = bpmService.getBpmServer().doGet(rest, auth);
            if (!"".equals(jsonContent)) {
                obj = new JSONObject(jsonContent);
            }

        } catch (JSONException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
        } catch (RuntimeException e) {
        }
        if(obj != null) {
            taskContentInfos = new TaskContentInfos(obj);
        }
        return taskContentInfos;
    }

    @PUT
    @Path("/processcontent/{id}/{deploymentId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.ADMINISTRATE_BPM})
    public Response startProcessContent(TaskContentInfos taskContentInfos, @PathParam("id") String id,
                                        @PathParam("deploymentId") String deploymentId, @HeaderParam("Authorization") String auth) {
        Map<String, Object> expectedParams = getOutputContent(taskContentInfos, -1, id, auth);
        List<Errors> err = new ArrayList<>();
        TaskContentInfos taskContents = getProcessContent(id,deploymentId, auth);
        taskContentInfos.properties.stream()
                .forEach(s -> {
                    if (s.propertyValueInfo.value == null  || s.propertyValueInfo.value.equals("")) {
                        Optional<TaskContentInfo> taskContentInfo = taskContents.properties.stream()
                                .filter(x -> x.key.equals(s.key))
                                .findFirst();
                        if (taskContentInfo.isPresent()) {
                            if (taskContentInfo.get().required) {
                                err.add(new Errors("properties." + s.key, MessageSeeds.FIELD_CAN_NOT_BE_EMPTY.getDefaultFormat()));
                            }
                        }
                    }
                });
        id = id.replace(taskContentInfos.deploymentId,"");
        if(!err.isEmpty()){
            return Response.status(400).entity(new LocalizedFieldException(err)).build();
        }
        if(taskContentInfos.deploymentId != null && taskContentInfos.businessObject.id != null && taskContentInfos.businessObject.value != null) {
            expectedParams.put(taskContentInfos.businessObject.id, taskContentInfos.businessObject.value);
            bpmService.startProcess(taskContentInfos.deploymentId, id, expectedParams, auth);
        }
        return Response.ok().build();
    }

    @PUT
    @Path("taskcontent/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.EXECUTE_TASK)
    public Response postTaskContent(TaskContentInfos taskContentInfos,
                                    @PathParam("id") long id,
                                    @Context SecurityContext securityContext,
                                    @HeaderParam("Authorization") String auth) {
        String postResult = null;
        List<Errors> err = new ArrayList<>();
        String userName = securityContext.getUserPrincipal().getName();
        if(!taskContentInfos.action.equals("startTask")) {
            TaskContentInfos taskContents = getTaskContent(id, auth);
            taskContentInfos.properties.stream()
                    .forEach(s -> {
                        if (s.propertyValueInfo.value == null) {
                            Optional<TaskContentInfo> taskContentInfo = taskContents.properties.stream()
                                    .filter(x -> x.key.equals(s.key))
                                    .findFirst();
                            if (taskContentInfo.isPresent()) {
                                if (taskContentInfo.get().required) {
                                    err.add(new Errors("properties." + s.key, MessageSeeds.FIELD_CAN_NOT_BE_EMPTY.getDefaultFormat()));
                                }
                            }
                        } else if (s.propertyValueInfo.value.equals("")) {
                            Optional<TaskContentInfo> taskContentInfo = taskContents.properties.stream()
                                    .filter(x -> x.key.equals(s.key))
                                    .findFirst();
                            if (taskContentInfo.isPresent()) {
                                if (taskContentInfo.get().required) {
                                    err.add(new Errors("properties." + s.key, MessageSeeds.FIELD_CAN_NOT_BE_EMPTY.getDefaultFormat()));
                                }
                            }
                        }
                    });
        }
        if(!err.isEmpty()){
          return  Response.status(400).entity(new LocalizedFieldException(err)).build();
        }
        JSONObject obj = null;
        if(taskContentInfos.action.equals("startTask")){
            String rest = "/rest/tasks/" + id + "/contentstart/" + userName + "/";
            postResult = bpmService.getBpmServer().doPost(rest, null, auth, 0);
        }
        if(taskContentInfos.action.equals("completeTask")){
            Map<String, Object> outputBindingContents = getOutputContent(taskContentInfos, id, null, auth);
            TaskOutputContentInfo taskOutputContentInfo = new TaskOutputContentInfo(outputBindingContents);
            ObjectMapper mapper = new ObjectMapper();
            String stringJson = null;
            try {
                stringJson = mapper.writeValueAsString(taskOutputContentInfo);
                String rest = "/rest/tasks/" + id + "/contentcomplete/" + userName + "/";
                postResult = bpmService.getBpmServer().doPost(rest, stringJson, auth, 0);
            } catch (JsonProcessingException e) {
            }
        }
        if(taskContentInfos.action.equals("saveTask")){
            Map<String, Object> outputBindingContents = getOutputContent(taskContentInfos, id, null, auth);
            TaskOutputContentInfo taskOutputContentInfo = new TaskOutputContentInfo(outputBindingContents);
            ObjectMapper mapper = new ObjectMapper();
            String stringJson = null;
            try {
                stringJson = mapper.writeValueAsString(taskOutputContentInfo);
                String rest = "/rest/tasks/" + id + "/contentsave";
                postResult = bpmService.getBpmServer().doPost(rest, stringJson, auth, 0);
            } catch (JsonProcessingException e) {
            }
        }
        if (postResult == null) {
            return Response.status(400).build();
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/validateform/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.EXECUTE_TASK)
    public Response validateForm(TaskContentInfos taskContentInfos,
                                    @PathParam("id") long id,
                                    @Context SecurityContext securityContext,
                                    @HeaderParam("Authorization") String auth) {
        List<Errors> err = new ArrayList<>();
        TaskContentInfos taskContents = getTaskContent(id, auth);
        taskContentInfos.properties.stream()
                .forEach(s -> {
                    if (s.propertyValueInfo.value == null || s.propertyValueInfo.value.equals("")) {
                        Optional<TaskContentInfo> taskContentInfo = taskContents.properties.stream()
                                .filter(x -> x.key.equals(s.key))
                                .findFirst();
                        if (taskContentInfo.isPresent()) {
                            if (taskContentInfo.get().required) {
                                err.add(new Errors("properties." + s.key, MessageSeeds.FIELD_CAN_NOT_BE_EMPTY.getDefaultFormat()));
                            }
                        }
                    }
                });
        if(!err.isEmpty()){
            return  Response.status(400).entity(new LocalizedFieldException(err)).build();
        }else{
            return Response.ok().entity(taskContentInfos).build();
        }
    }

    private Map<String, Object> getOutputContent(TaskContentInfos taskContentInfos, long taskId, String processId, String auth){
        TaskContentInfos taskContents;
        if(processId != null) {
            taskContents = getProcessContent(processId, taskContentInfos.deploymentId, auth);
        }else{
            taskContents = getTaskContent(taskId, auth);
        }
        Map<String, Object> outputBindingContents = new HashMap<>();
        try {
            taskContents.properties.stream()
                    .filter(pi -> pi.outputBinding != null)
                    .forEach(s -> {

                        Optional<TaskContentInfo> taskContentInfo = taskContentInfos.properties.stream()
                                .filter(p -> p.key.equals(s.key))
                                .findFirst();
                        if (taskContentInfo.isPresent()) {
                            if (taskContentInfo.get().propertyTypeInfo.simplePropertyType.equals("TIMESTAMP")) {
                                Date date = new Date();
                                if(taskContentInfo.get().propertyValueInfo.value != null) {
                                    date.setTime(Long.valueOf(taskContentInfo.get().propertyValueInfo.value));
                                    outputBindingContents.put(s.outputBinding, date);
                                }
                            } else if (taskContentInfo.get().propertyTypeInfo.simplePropertyType.equals("DATE")) {
                                Date date = new Date();
                                if(taskContentInfo.get().propertyValueInfo.value != null) {
                                    date.setTime(Long.valueOf(taskContentInfo.get().propertyValueInfo.value));
                                    outputBindingContents.put(s.outputBinding, date);
                                }
                            } else if(taskContentInfo.get().propertyTypeInfo.predefinedPropertyValuesInfo != null){
                                if(taskContentInfo.get().propertyTypeInfo.predefinedPropertyValuesInfo.selectionMode.equals("COMBOBOX")) {
                                    Iterator<String> it = s.propertyTypeInfo.predefinedPropertyValuesInfo.comboKeys.keySet().iterator();
                                    while (it.hasNext()) {
                                        String theKey = (String) it.next();
                                        if (s.propertyTypeInfo.predefinedPropertyValuesInfo.comboKeys.get(theKey).equals(taskContentInfo.get().propertyValueInfo.value)) {
                                            outputBindingContents.put(s.outputBinding, theKey);
                                        }
                                    }
                                }
                            }else {
                                outputBindingContents.put(s.outputBinding, taskContentInfo.get().propertyValueInfo.value);
                            }
                        }
                    });
        }catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
        return outputBindingContents;
    }

    private List<String> getPropertyList(String source, String propertyValue){
        List<String> privilegesList = new ArrayList<>();
        try {
            if (source != null) {
                JsonNode node = new ObjectMapper().readValue(new ByteArrayInputStream(source.getBytes()), JsonNode.class);
                if (node != null && node.isArray()) {
                    for (JsonNode singleFilter : node) {
                        JsonNode property = singleFilter.get(propertyValue);
                        if (property != null && property.textValue() != null)
                            privilegesList.add(property.textValue());
                    }
                }
            }
        }catch (Exception e){

        }
        return privilegesList;
    }

    private void doUpdatePrivileges(BpmProcessDefinition bpmProcessDefinition, ProcessDefinitionInfo info) {
        List<BpmProcessPrivilege> currentPrivileges = bpmProcessDefinition.getPrivileges();
        List<BpmProcessPrivilege> targetPrivileges = info.privileges.stream()
                .map(s -> bpmService.createBpmProcessPrivilege(bpmProcessDefinition, s.id, s.applicationName)).collect(Collectors.toList());

        if (!targetPrivileges.equals(currentPrivileges)) {
            bpmProcessDefinition.revokePrivileges(currentPrivileges);
            bpmProcessDefinition.grantPrivileges(targetPrivileges);
        }
    }


    private String getQueryValue(UriInfo uriInfo, String key) {
        return uriInfo.getQueryParameters().getFirst(key);
    }

    private String getQueryParam(QueryParameters queryParam) {
        String req = "";
        int i = 0;
        Iterator<String> it = queryParam.keySet().iterator();
        while (it.hasNext()) {
            String theKey = (String) it.next();
            if (i > 0) {
                req += "&";
            } else {
                req += "?";
            }
            req += theKey + "=" + queryParam.getFirst(theKey);
            i++;
        }
        return req;
    }

}