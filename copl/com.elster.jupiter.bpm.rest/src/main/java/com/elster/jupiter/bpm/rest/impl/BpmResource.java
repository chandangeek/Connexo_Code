/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest.impl;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmProcessDefinitionBuilder;
import com.elster.jupiter.bpm.BpmProcessPrivilege;
import com.elster.jupiter.bpm.BpmServer;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.ProcessAssociationProvider;
import com.elster.jupiter.bpm.ProcessInstanceInfo;
import com.elster.jupiter.bpm.ProcessInstanceInfos;
import com.elster.jupiter.bpm.UserTaskInfo;
import com.elster.jupiter.bpm.UserTaskInfos;
import com.elster.jupiter.bpm.rest.AssigneeFilterListInfo;
import com.elster.jupiter.bpm.rest.BpmProcessNotAvailable;
import com.elster.jupiter.bpm.rest.BpmResourceAssignUserException;
import com.elster.jupiter.bpm.rest.DeploymentInfo;
import com.elster.jupiter.bpm.rest.DeploymentInfos;
import com.elster.jupiter.bpm.rest.Errors;
import com.elster.jupiter.bpm.rest.LocalizedFieldException;
import com.elster.jupiter.bpm.rest.NoBpmConnectionException;
import com.elster.jupiter.bpm.rest.NoTaskWithIdException;
import com.elster.jupiter.bpm.rest.NodeInfos;
import com.elster.jupiter.bpm.rest.PagedInfoListCustomized;
import com.elster.jupiter.bpm.rest.ProcessAssociationInfo;
import com.elster.jupiter.bpm.rest.ProcessAssociationInfos;
import com.elster.jupiter.bpm.rest.ProcessDefinitionInfo;
import com.elster.jupiter.bpm.rest.ProcessDefinitionInfos;
import com.elster.jupiter.bpm.rest.ProcessHistoryInfos;
import com.elster.jupiter.bpm.rest.ProcessInstanceNodeInfos;
import com.elster.jupiter.bpm.rest.ProcessesPrivilegesInfo;
import com.elster.jupiter.bpm.rest.StartupInfo;
import com.elster.jupiter.bpm.rest.TaskBulkReportInfo;
import com.elster.jupiter.bpm.rest.TaskContentInfo;
import com.elster.jupiter.bpm.rest.TaskContentInfos;
import com.elster.jupiter.bpm.rest.TaskGroupsInfos;
import com.elster.jupiter.bpm.rest.TaskOutputContentInfo;
import com.elster.jupiter.bpm.rest.TopTaskInfo;
import com.elster.jupiter.bpm.rest.TopTasksPayload;
import com.elster.jupiter.bpm.rest.VariableInfos;
import com.elster.jupiter.bpm.rest.resource.StandardParametersBean;
import com.elster.jupiter.bpm.security.Privileges;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Resource;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
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
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/runtime")
public class BpmResource {

    private static final String ME = "me";
    private static final String LIKE = "like";

    private final UserService userService;
    private final Thesaurus thesaurus;
    private final BpmService bpmService;

    private final String errorNotFoundMessage;
    private final String errorInvalidMessage;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final PropertyValueInfoService propertyValueInfoService;

    @Inject
    public BpmResource(BpmService bpmService, UserService userService, Thesaurus thesaurus, PropertyValueInfoService propertyValueInfoService, ConcurrentModificationExceptionFactory conflictFactory) {
        this.bpmService = bpmService;
        this.userService = userService;
        this.thesaurus = thesaurus;
        this.propertyValueInfoService = propertyValueInfoService;
        this.conflictFactory = conflictFactory;

        this.errorNotFoundMessage = thesaurus.getString("error.flow.unavailable", "Connexo Flow is not available.");
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
    public com.elster.jupiter.bpm.rest.ProcessInstanceInfos getAllInstances(@Context UriInfo uriInfo, @HeaderParam("Authorization") String auth) {
        String jsonContent;
        JSONArray arr = null;
        DeploymentInfos deploymentInfos = getAllDeployments(auth);
        if (deploymentInfos != null && deploymentInfos.total > 0) {
            // Apparently - although not in line with the documentation - all instances are returned regardless of the deployment processId
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
                throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(this.errorNotFoundMessage)
                        .build());
            }
        }
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        return new com.elster.jupiter.bpm.rest.ProcessInstanceInfos(arr, queryParameters.getLimit(), queryParameters.getStartInt());
    }

    @GET
    @Path("/deployment/{deploymentId}/instance/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_BPM)
    public com.elster.jupiter.bpm.rest.ProcessInstanceInfo getInstance(@Context UriInfo uriInfo, @PathParam("deploymentId") String deploymentId,
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
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(this.errorNotFoundMessage)
                    .build());
        }
        return new com.elster.jupiter.bpm.rest.ProcessInstanceInfo(obj);
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
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(this.errorNotFoundMessage)
                    .build());
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
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(this.errorNotFoundMessage)
                    .build());
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
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(this.errorNotFoundMessage)
                    .build());
        }
        return new DeploymentInfos(arr);
    }

    @GET
    @Path("/toptasks")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK, Privileges.Constants.ASSIGN_TASK, Privileges.Constants.EXECUTE_TASK})
    public TopTaskInfo getTask(@Context UriInfo uriInfo, @HeaderParam("Authorization") String auth, @HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey, @Context SecurityContext securityContext) {
        String jsonContent;
        TopTaskInfo topTaskInfo = new TopTaskInfo();
        JSONArray arr = null;
        ObjectMapper mapper = new ObjectMapper();
        Optional<User> currentUser = userService.findUser(securityContext.getUserPrincipal().getName());
        if(currentUser.isPresent()) {
            String payload;
            try {
                String rest = "/rest/tasks/toptasks";
                payload = mapper.writeValueAsString(
                        new TopTasksPayload(currentUser.get().getName(),
                                currentUser.get().getWorkGroups().stream().map(WorkGroup::getName).collect(Collectors.toList()),
                                getAvailableProcessesByAppKey(uriInfo, auth, appKey)));
                jsonContent = bpmService.getBpmServer().doPost(rest, payload, auth, 0L);
                if (!"".equals(jsonContent)) {
                    JSONObject obj = new JSONObject(jsonContent);
                    topTaskInfo.totalUserAssigned = Long.valueOf(obj.get("totalUserAssigned").toString());
                    topTaskInfo.totalWorkGroupAssigned = Long.valueOf(obj.get("workGroupAssigned").toString());
                    arr = obj.getJSONArray("tasks");
                    UserTaskInfos infos = new UserTaskInfos(arr, "");
                    topTaskInfo.items = infos.getTasks();
                    topTaskInfo.total = infos.total;
                }
            } catch (JSONException e) {
                throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(this.errorInvalidMessage)
                        .build());
            } catch (RuntimeException e) {
                throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(this.errorNotFoundMessage)
                        .build());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        }
        return topTaskInfo;
    }

    @GET
    @Path("/tasks")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK, Privileges.Constants.ASSIGN_TASK, Privileges.Constants.EXECUTE_TASK})
    public PagedInfoList getTask(@Context UriInfo uriInfo, @BeanParam JsonQueryFilter filterX, @HeaderParam("Authorization") String auth, @HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey, @BeanParam JsonQueryParameters queryParam) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters(false));
        String jsonContent;
        int total = -1;
        JSONArray arr = null;
        ObjectMapper mapper = new ObjectMapper();
        String payload;
        try {
            String rest = "/rest/tasks";
            String req = getQueryParam(queryParameters);
            if (!"".equals(req)) {
                rest += req;
            }
            payload = mapper.writeValueAsString(getAvailableProcessesByAppKey(uriInfo, auth, appKey));
            jsonContent = bpmService.getBpmServer().doPost(rest, payload, auth, 0L);
            if (!"".equals(jsonContent)) {
                JSONObject obj = new JSONObject(jsonContent);
                total = Integer.valueOf(obj.get("total").toString());
                arr = obj.getJSONArray("tasks");
            }
        } catch (JSONException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
        } catch (RuntimeException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(this.errorNotFoundMessage)
                    .build());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        UserTaskInfos infos = new UserTaskInfos(arr, "");
        infos.getTasks().stream().forEach(info -> {
            if(!userService.getWorkGroup(info.workgroup).isPresent()){
                info.workgroup = "";
            }
        });
        return PagedInfoList.fromPagedList("tasks", ListPager.of(infos.getTasks()).from(queryParam).find(), queryParam);
    }

    @GET
    @Path("/tasks/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK, Privileges.Constants.ASSIGN_TASK, Privileges.Constants.EXECUTE_TASK})
    public UserTaskInfo getTask(@Context UriInfo uriInfo, @PathParam("id") long id, @HeaderParam("Authorization") String auth, @HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey) {
        String jsonContent;
        UserTaskInfo taskInfo = new UserTaskInfo();
        ObjectMapper mapper = new ObjectMapper();
        String payload;
        try {
            String rest = "/rest/tasks/";
            rest += String.valueOf(id);
            payload = mapper.writeValueAsString(getAvailableProcessesByAppKey(uriInfo, auth, appKey));
            jsonContent = bpmService.getBpmServer().doPost(rest, payload, auth, 0L);
            if (!"".equals(jsonContent)) {
                JSONObject obj = new JSONObject(jsonContent);
                taskInfo = new UserTaskInfo(obj, "");
            }

        } catch (JSONException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
        } catch (RuntimeException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(this.errorNotFoundMessage)
                    .build());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if ("0".equals(taskInfo.id)) {
            throw new NoTaskWithIdException(thesaurus, MessageSeeds.NO_TASK_WITH_ID , id);
        }
        Optional<WorkGroup> workGroup = userService.getWorkGroup(taskInfo.workgroup);
        if(workGroup.isPresent()){
            taskInfo.workgroupId = workGroup.get().getId();
        } else {
            taskInfo.workgroup = "";
        }
        Optional<User> user = userService.findUser(taskInfo.actualOwner);
        if(user.isPresent()){
            taskInfo.userId = user.get().getId();
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
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(this.errorNotFoundMessage)
                    .build());
        }
        return jsnobject != null ? new ProcessInstanceNodeInfos(jsnobject, thesaurus) : new ProcessInstanceNodeInfos();
    }

    @GET
    @Path("/processes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK, Privileges.Constants.ASSIGN_TASK, Privileges.Constants.EXECUTE_TASK})
    public ProcessDefinitionInfos getProcesses(@Context UriInfo uriInfo, @HeaderParam("Authorization") String auth) {
        MultivaluedMap<String, String> filterProperties = uriInfo.getQueryParameters();
        if (filterProperties.get("type") != null) {
            List<BpmProcessDefinition> activeProcesses = bpmService.getAllBpmProcessDefinitions();
            ProcessDefinitionInfos processDefinitionInfos = getBpmProcessDefinitions(auth);
            processDefinitionInfos.processes = processDefinitionInfos.processes.stream()
                    .filter(s -> activeProcesses.stream()
                            .anyMatch(a -> a.getProcessName().equals(s.name) &&
                                    a.getVersion().equals(s.version) &&
                                    a.getAssociationProvider().isPresent() &&
                                    a.getAssociation().equals(filterProperties.get("type").get(0).toLowerCase())))
                    .collect(Collectors.toList());
            processDefinitionInfos.total = processDefinitionInfos.processes.size();
            return processDefinitionInfos;
        } else {
            List<BpmProcessDefinition> activeProcesses = bpmService.getAllBpmProcessDefinitions().stream()
                    .filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus())).collect(Collectors.toList());
            ProcessDefinitionInfos processDefinitionInfos = getBpmProcessDefinitions(auth);
            processDefinitionInfos.processes = processDefinitionInfos.processes.stream()
                    .filter(s -> activeProcesses.stream()
                            .anyMatch(a -> a.getProcessName().equals(s.name) && a.getVersion().equals(s.version)))
                    .collect(Collectors.toList());
            processDefinitionInfos.total = processDefinitionInfos.processes.size();
            return processDefinitionInfos;
        }
    }

    @GET
    @Path("/availableactiveprocesses")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TASK, Privileges.Constants.ASSIGN_TASK, Privileges.Constants.EXECUTE_TASK})
    public ProcessDefinitionInfos getAvailableProcesses(@Context UriInfo uriInfo, @HeaderParam("Authorization") String auth, @HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey) {
        MultivaluedMap<String, String> filterProperties = uriInfo.getQueryParameters();
        List<BpmProcessDefinition> activeProcesses = bpmService.getActiveBpmProcessDefinitions(appKey);
        return (filterProperties.get("type") != null) ? filterProcesses(activeProcesses, filterProperties.get("type").get(0), auth) : filterProcesses(activeProcesses, null, auth);
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
    @Path("/release/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ASSIGN_TASK)
    public Response releaseTask(@PathParam("id") long id, @Context SecurityContext securityContext, @HeaderParam("Authorization") String auth) throws
            UnsupportedEncodingException {
        String restURL = "/rest/tasks/release/" + String.valueOf(id) + "?currentuser=" + URLEncoder.encode(securityContext.getUserPrincipal().getName(), "UTF-8");
        String response = bpmService.getBpmServer().doPost(restURL, null, auth, 0);
        if (response == null) {
            throw new BpmResourceAssignUserException(thesaurus);
        }
        return Response.ok().build();
    }

    @POST
    @Path("/assigntome/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ASSIGN_TASK)
    public Response assignUser(@PathParam("id") long id, @Context SecurityContext securityContext, @HeaderParam("Authorization") String auth) throws
            UnsupportedEncodingException {
        String restURL = "/rest/tasks/assigntome/" + String.valueOf(id) + "?currentuser=" + URLEncoder.encode(securityContext.getUserPrincipal().getName(), "UTF-8");
        String response = bpmService.getBpmServer().doPost(restURL, null, auth, 0);
        if (response == null) {
            throw new BpmResourceAssignUserException(thesaurus);
        }
        return Response.ok().build();
    }

    @POST
    @Path("tasks/{id}/{optLock}/assign")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ASSIGN_TASK)
    public Response assignUser(@Context UriInfo uriInfo, @PathParam("id") long id, @PathParam("optLock") long optLock, @Context SecurityContext securityContext, @HeaderParam("Authorization") String auth) throws
            UnsupportedEncodingException {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters(false));
        String response;
        String userId = getQueryValue(uriInfo, "userId");
        String priority = getQueryValue(uriInfo, "priority");
        String date = getQueryValue(uriInfo, "duedate");
        String workGroupId = getQueryValue(uriInfo, "workgroupId");
        Optional<WorkGroup> workGroup = userService.getWorkGroup(Long.valueOf(workGroupId));
        Optional<User> user = userService.getUser(Long.valueOf(userId));
        String workGroupName;
        String userName;
        if(workGroup.isPresent()){
            workGroupName = workGroup.get().getName();
        }else{
            workGroupName = "Unassigned";
        }
        if(user.isPresent()){
            userName = user.get().getName();
        }else{
            userName = "Unassigned";
        }
        String rest = "/rest/tasks/";
        rest += String.valueOf(id) + "/";
        rest += String.valueOf(optLock);
        String req = getQueryParam(queryParameters);
        if (userName != null || date != null || priority != null || workGroupName != null) {
            rest += "/assign/" + req + "&workgroupname=" + URLEncoder.encode(workGroupName, "UTF-8") + "&username=" + URLEncoder.encode(userName, "UTF-8");
            rest += "&currentuser=" + securityContext.getUserPrincipal().getName();
            try {
                response = bpmService.getBpmServer().doPost(rest, null, auth, 0);
            } catch (RuntimeException e) {
                throw e.getMessage().contains("409")
                        ? conflictFactory.conflict()
                        .withActualVersion(() -> optLock)
                        .withMessageTitle(MessageSeeds.EDIT_TASK_CONCURRENT_TITLE, e.getMessage().replace("409", ""))
                        .withMessageBody(MessageSeeds.EDIT_TASK_CONCURRENT_BODY, e.getMessage().replace("409", ""))
                        .supplier().get()
                        : new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(this.errorNotFoundMessage)
                        .build());
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
                throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(this.errorNotFoundMessage)
                        .build());
            }
            return Response.ok().build();
        }
        return Response.notModified().build();
    }

    @GET
    @Path("/process/associations")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_BPM)
    public ProcessAssociationInfos getProcessAssociations() {
        List<ProcessAssociationInfo> infos = bpmService.getProcessAssociationProviders().stream()
                .map(provider -> new ProcessAssociationInfo(provider.getName(), provider.getType(), propertyValueInfoService.getPropertyInfos(provider.getPropertySpecs())))
                .collect(Collectors.toList());
        return new ProcessAssociationInfos(infos);
    }

    @PUT
    @Transactional
    @Path("/process/activate/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_BPM)
    public Response activateProcess(ProcessDefinitionInfo info) {
        Optional<ProcessAssociationProvider> foundProvider = bpmService.getProcessAssociationProvider(info.type);
        List<PropertySpec> propertySpecs = foundProvider.isPresent() ? foundProvider.get()
                .getPropertySpecs() : Collections.emptyList();

        // This section is only required for 10.1 backward compatibility, as backend validators would break this
        // When enabling backend validators, this section can be removed
        List<Errors> err = new ArrayList<>();
        for (PropertyInfo property : info.properties) {
            if (property.getPropertyValueInfo().value == null && property.required) {
                err.add(new Errors("properties." + property.key, MessageSeeds.FIELD_CAN_NOT_BE_EMPTY.getDefaultFormat()));
            }
        }
        if (info.privileges.isEmpty()) {
            err.add(new Errors("processPrivileges", MessageSeeds.FIELD_CAN_NOT_BE_EMPTY.getDefaultFormat()));
        }
        if (!err.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new LocalizedFieldException(err)).build();
        }

        List<BpmProcessPrivilege> targetPrivileges = info.privileges.stream()
                .map(s -> bpmService.createBpmProcessPrivilege(s.id, s.applicationName))
                .collect(Collectors.toList());

        BpmProcessDefinition process;
        Optional<BpmProcessDefinition> foundProcess = bpmService.getBpmProcessDefinition(info.name, info.version);
        if (!foundProcess.isPresent()) {
            BpmProcessDefinitionBuilder processBuilder = bpmService.newProcessBuilder()
                    .setId(info.processId).setProcessName(info.name)
                    .setAssociation(info.type.toLowerCase())
                    .setVersion(info.version)
                    .setStatus(info.active)
                    .setAppKey(foundProvider.isPresent() ? foundProvider.get().getAppKey() : "")
                    .setProperties(convertPropertyInfosToProperties(propertySpecs, info.properties))
                    .setPrivileges(targetPrivileges);
            process = processBuilder.create();
            targetPrivileges.stream().forEach(privilege -> {
                privilege.setProcessId(process.getId());
                privilege.persist();
            });
        } else {
            process = foundProcess.get();
            if (process.getVersionDB() != info.versionDB) {
                throw conflictFactory.conflict()
                        .withActualVersion(process::getVersionDB)
                        .withMessageTitle(MessageSeeds.EDIT_PROCESS_CONCURRENT_TITLE, info.name)
                        .withMessageBody(MessageSeeds.EDIT_PROCESS_CONCURRENT_BODY, info.name)
                        .supplier().get();
            }
            List<BpmProcessPrivilege> oldPrivileges = new ArrayList<>(process.getPrivileges());

            process.setAssociation(info.type.toLowerCase());
            process.setStatus(info.active);
            process.setAppKey(foundProvider.isPresent() ? foundProvider.get().getAppKey() : "");
            process.setProperties(convertPropertyInfosToProperties(propertySpecs, info.properties));
            process.setPrivileges(targetPrivileges);
            process.save();

            doUpdatePrivileges(process, targetPrivileges, oldPrivileges);
        }

        return Response.ok().build();
    }

    @PUT
    @Transactional
    @Path("/process/deactivate")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_BPM)
    public ProcessDefinitionInfo deactivateProcess(ProcessDefinitionInfo info) {
        Optional<BpmProcessDefinition> bpmProcessDefinition = bpmService.getBpmProcessDefinition(info.name, info.version);
        if (bpmProcessDefinition.isPresent()) {
            if (bpmProcessDefinition.get().getVersionDB() != info.versionDB) {
                throw conflictFactory.conflict()
                        .withActualVersion(bpmProcessDefinition.get()::getVersionDB)
                        .withMessageTitle(MessageSeeds.EDIT_PROCESS_CONCURRENT_TITLE, info.name)
                        .withMessageBody(MessageSeeds.EDIT_PROCESS_CONCURRENT_BODY, info.name)
                        .supplier().get();
            }
            bpmProcessDefinition.get().setStatus(info.active);
            bpmProcessDefinition.get().save();
            return new ProcessDefinitionInfo(bpmProcessDefinition.get());
        }
        throw new BpmProcessNotAvailable(thesaurus, info.name + ":" + info.version);
    }

    @GET
    @Path("/process/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.ADMINISTRATE_BPM})
    public ProcessDefinitionInfo getBpmProcessDefinition(@PathParam("id") String id, @Context UriInfo uriInfo, @HeaderParam("Authorization") String auth) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        ProcessDefinitionInfo processDefinitionInfo = null;
        if (queryParameters.get("version") != null) {
            String version = queryParameters.get("version").get(0);
            Optional<BpmProcessDefinition> bpmProcessDefinition = bpmService.getBpmProcessDefinition(id, version);

            List<Group> groups = this.userService.getGroups();
            if (bpmProcessDefinition.isPresent()) {
                processDefinitionInfo = new ProcessDefinitionInfo(bpmProcessDefinition.get(), groups);
            } else {
                processDefinitionInfo = getBpmProcessDefinitions(auth).processes.stream()
                        .filter(s -> s.name.equals(id) && s.version.equals(version)).findFirst()
                        .orElseThrow(() -> new BpmProcessNotAvailable(thesaurus, id + ":" + version));
            }

            if (queryParameters.get("association") == null && bpmProcessDefinition.isPresent()) {
                Optional<ProcessAssociationProvider> foundProvider = bpmProcessDefinition.get()
                        .getAssociationProvider();
                if (foundProvider.isPresent()) {
                    processDefinitionInfo.setProperties(propertyValueInfoService.getPropertyInfos(foundProvider.get().getPropertySpecs(), bpmProcessDefinition.get().getProperties()));
                    processDefinitionInfo.setAppKey(foundProvider.get().getAppKey());
                }
            } else {
                if (queryParameters.get("association") != null) {
                    String association = queryParameters.get("association").get(0);
                    Optional<ProcessAssociationProvider> foundProvider = bpmService.getProcessAssociationProvider(association);
                    if (foundProvider.isPresent()) {
                        processDefinitionInfo.setProperties(propertyValueInfoService.getPropertyInfos(foundProvider.get()
                                        .getPropertySpecs(),
                                bpmProcessDefinition.isPresent() ? bpmProcessDefinition.get()
                                        .getProperties() : new HashMap<>()));
                        processDefinitionInfo.setAppKey(foundProvider.get().getAppKey());
                    }
                }
            }
        } else {
            processDefinitionInfo = getBpmProcessDefinitions(auth).processes.stream()
                    .filter(s -> s.name.equals(id)).findFirst()
                    .orElseThrow(() -> new BpmProcessNotAvailable(thesaurus, id));
        }
        return processDefinitionInfo;
    }

    @GET
    @Path("/activeprocesses")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.ADMINISTRATE_BPM})
    public PagedInfoList getActiveBpmProcessesDefinitions(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters, @HeaderParam("Authorization") String auth) {
        MultivaluedMap<String, String> filterProperties = uriInfo.getQueryParameters();
        if (filterProperties.get("type") != null) {
            if (filterProperties.get("privileges") != null) {
                List<String> privileges = getPropertyList(filterProperties.get("privileges").get(0), "privilege");
                ProcessDefinitionInfos bpmProcessDefinition = getBpmProcessDefinitions(auth);
                List<BpmProcessDefinition> connexoProcesses = bpmService.getActiveBpmProcessDefinitions();
                List<BpmProcessDefinition> filtredConnexoProcesses = connexoProcesses.stream()
                        .filter(p -> p.getAssociationProvider().isPresent() &&
                                p.getAssociation()
                                        .toLowerCase()
                                        .equals(filterProperties.get("type").get(0).toLowerCase()))
                        .filter(p -> p.getPrivileges().stream()
                                .anyMatch(s -> privileges.stream().anyMatch(z -> z.equals(s.getPrivilegeName()))))
                        .filter(p -> p.getProperties().keySet().stream()
                                .filter(filterProperties::containsKey)
                                .filter(f -> List.class.isInstance(p.getProperties().get(f)))
                                .allMatch(f -> ((List<Object>) p.getProperties().get(f)).stream()
                                        .filter(HasIdAndName.class::isInstance)
                                        .anyMatch(v -> ((HasIdAndName) v).getId()
                                                .toString()
                                                .equals(filterProperties.get(f).get(0)))))
                        .collect(Collectors.toList());

                List<ProcessDefinitionInfo> bpmProcesses = bpmProcessDefinition.processes.stream()
                        .filter(s -> filtredConnexoProcesses.stream()
                                .anyMatch(x -> x.getProcessName().equals(s.name) && x.getVersion()
                                        .equals(s.version)))
                        .collect(Collectors.toList());
                for(ProcessDefinitionInfo bpmDefinition : bpmProcesses) {
                    for(BpmProcessDefinition connexoDefinition : filtredConnexoProcesses) {
                        if (connexoDefinition.getProcessName().equals(bpmDefinition.name) && connexoDefinition.getVersion().equals(bpmDefinition.version)) {
                            bpmDefinition.versionDB = connexoDefinition.getVersionDB();
                        }
                    }
                }
                return PagedInfoList.fromCompleteList("processes", bpmProcesses, queryParameters);
            }
        }

        return PagedInfoList.fromCompleteList("processes", new ArrayList<>(), queryParameters);
    }

    private ProcessDefinitionInfos filterProcesses(List<BpmProcessDefinition> activeProcesses, String filterProperty, String auth) {
        ProcessDefinitionInfos processDefinitionInfos = getBpmProcessDefinitions(auth);
        processDefinitionInfos.processes = processDefinitionInfos.processes.stream()
                .filter(s -> {
                    if (filterProperty != null) {
                        return activeProcesses.stream().anyMatch(a -> a.getProcessName().equals(s.name) && a.getVersion().equals(s.version) && a.getAssociation().toLowerCase().equals(filterProperty.toLowerCase()));
                    } else {
                        return activeProcesses.stream().anyMatch(a -> a.getProcessName().equals(s.name) && a.getVersion().equals(s.version));
                    }
                })
                .collect(Collectors.toList());
        processDefinitionInfos.total = processDefinitionInfos.processes.size();
        return processDefinitionInfos;
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
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(this.errorNotFoundMessage)
                    .build());
        }
        return new ProcessDefinitionInfos(arr);
    }

    @GET
    @Transactional
    @Path("/allprocesses")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.ADMINISTRATE_BPM})
    public PagedInfoList getBpmProcessesDefinitions(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters, @HeaderParam("Authorization") String auth, @Context HttpHeaders headers) {
        List<BpmProcessDefinition> connexoProcesses = bpmService.getBpmProcessDefinitions();
        ProcessDefinitionInfos bpmProcessDefinition = getBpmProcessDefinitions(auth);
        for (BpmProcessDefinition eachConnexo : connexoProcesses) {
            boolean notFound = true;
            for (ProcessDefinitionInfo eachBpm : bpmProcessDefinition.processes) {
                if (eachConnexo.getProcessName().equals(eachBpm.name) && eachConnexo.getVersion()
                        .equals(eachBpm.version)) {
                    eachBpm.active = eachConnexo.getStatus();
                    eachBpm.type = eachConnexo.getAssociationProvider()
                            .isPresent() ? eachConnexo.getAssociationProvider().get().getType() : "";
                    eachBpm.displayType = eachConnexo.getAssociationProvider()
                            .isPresent() ? eachConnexo.getAssociationProvider().get().getName() : "";
                    eachBpm.appKey = eachConnexo.getAssociationProvider()
                            .isPresent() ? eachConnexo.getAssociationProvider().get().getAppKey() : "";
                    eachBpm.versionDB = eachConnexo.getVersionDB();
                    notFound = false;
                }
            }
            if (notFound && !bpmProcessDefinition.processes.isEmpty()) {
                eachConnexo.setStatus("UNDEPLOYED");
                eachConnexo.save();
            }
        }
        List<ProcessDefinitionInfo> list = bpmProcessDefinition.processes.stream()
                .sorted((s1, s2) -> s1.name.toLowerCase().compareTo(s2.name.toLowerCase()))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("processes", list, queryParameters);
    }

    @GET
    @Path("/processinstances")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public ProcessInstanceInfos getProcessInstances(@Context UriInfo uriInfo, @HeaderParam("Authorization") String auth) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters(false));
        String filter = getQueryParam(queryParameters);
        return bpmService.getRunningProcesses(auth, filter);
    }

    @GET
    @Path("/runningprocesses")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.ADMINISTRATE_BPM})
    public ProcessInstanceInfos getRunningProcesses(@Context UriInfo uriInfo, @HeaderParam("Authorization") String auth,  @HeaderParam("X-CONNEXO-APPLICATION-NAME") String appKey) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters(false));
        String jsonContent;
        int total = -1;
        JSONArray arr = null;
        try {
            String rest = "/rest/tasks/runningprocesses";
            String req = getQueryParam(queryParameters);
            if (!"".equals(req)) {
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
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(this.errorNotFoundMessage)
                    .build());
        }
        List<BpmProcessDefinition> activeProcesses = bpmService.getActiveBpmProcessDefinitions(appKey);
        ProcessInstanceInfos runningProcessInfos = new ProcessInstanceInfos(arr, "");
        List<ProcessInstanceInfo> runningProcessesList = runningProcessInfos.processes.stream()
                .filter(s -> activeProcesses.stream().anyMatch(a -> s.name.equals(a.getProcessName()) && s.version.equals(a.getVersion())))
                .collect(Collectors.toList());
        runningProcessInfos.processes = runningProcessesList;
        if (total == Integer.valueOf(queryParameters.get("page").get(0)) * runningProcessInfos.total + 1) {
            runningProcessInfos.total = total;
        } else {
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
            if (!"".equals(req)) {
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
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(this.errorNotFoundMessage)
                    .build());
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
    @RolesAllowed({Privileges.Constants.EXECUTE_TASK, Privileges.Constants.ASSIGN_TASK})
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
            String stringJson;
            try {
                stringJson = mapper.writeValueAsString(taskGroupsInfos);
                String rest = "/rest/tasks/managetasks";
                String req = getQueryParam(queryParameters);
                if (!"".equals(req)) {
                    rest += req+"&currentuser=" + securityContext.getUserPrincipal().getName() ;
                } else {
                    rest += req+"?currentuser=" + securityContext.getUserPrincipal().getName() ;
                }
                result = bpmService.getBpmServer().doPost(rest, stringJson, auth, 0);
                if (result != null) {
                    obj = new JSONObject(result);
                }
            } catch (JsonProcessingException e) {
                throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(this.errorInvalidMessage).build());
            } catch (JSONException e) {
                throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
            }
        } catch (RuntimeException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(this.errorNotFoundMessage)
                    .build());
        }
        if (obj == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        TaskBulkReportInfo taskBulkReportInfo = new TaskBulkReportInfo(obj);
        if (taskBulkReportInfo.failed > 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity(taskBulkReportInfo).build();
        }
        return Response.ok().entity(taskBulkReportInfo).build();
    }

    @POST
    @Path("tasks/mandatory")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.EXECUTE_TASK, Privileges.Constants.ASSIGN_TASK})
    public TaskGroupsInfos getTaskContent(TaskGroupsInfos taskGroupsInfos, @HeaderParam("Authorization") String auth, @Context UriInfo uriInfo) {
        String response = null;
        JSONArray arr = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            try {
                String stringJson = mapper.writeValueAsString(taskGroupsInfos);
                String rest = "/rest/tasks/mandatory";
                response = bpmService.getBpmServer().doPost(rest, stringJson, auth, 0);
            } catch (JsonProcessingException e) {
                throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(this.errorInvalidMessage).build());
            }
            if (response != null) {
                if (!"Connection refused: connect".equals(response)) {
                    arr = (new JSONObject(response)).getJSONArray("taskGroups");
                } else {
                    throw new NoBpmConnectionException(thesaurus);
                }
            }

        } catch (JSONException e) {
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(this.errorInvalidMessage).build());
        }
        TaskGroupsInfos taskGroups = new TaskGroupsInfos(arr);
        taskGroups.taskGroups.stream()
                .forEach(s-> {
            s.tasksForm.outputContent = null;
            s.tasksForm.properties.stream().forEach(f -> {
                if (f.propertyValueInfo != null) {
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
        if (resource.isPresent()) {
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
                if (!"Connection refused: connect".equals(jsonContent)) {
                    obj = new JSONObject(jsonContent);
                } else {
                    throw new NoBpmConnectionException(thesaurus);
                }
            }
            if (obj != null) {
                taskContentInfos = new TaskContentInfos(obj);
            }
            return taskContentInfos;
        } catch (JSONException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
        }
    }

    @GET
    @Path("/processcontent/{deploymentId}/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.ADMINISTRATE_BPM})
    public TaskContentInfos getProcessContent(@PathParam("id") String id,
                                              @PathParam("deploymentId") String deploymentId,
                                              @HeaderParam("Authorization") String auth) {
        String jsonContent;
        JSONObject obj = null;
        try {
            String rest = "/rest/tasks/process/" + deploymentId + "/content/" + id;
            jsonContent = bpmService.getBpmServer().doGet(rest, auth);
            if("Undeployed".equals(jsonContent)){
                throw conflictFactory.conflict()
                        .withActualVersion(() -> 1L) // Not an actual version; process has been undeployed
                        .withMessageTitle(MessageSeeds.START_PROCESS_CONCURRENT_TITLE, id)
                        .withMessageBody(MessageSeeds.START_PROCESS_CONCURRENT_BODY, id)
                        .supplier().get();
            }

            if (!"".equals(jsonContent)) {
                obj = new JSONObject(jsonContent);
            }

            return obj != null ? new TaskContentInfos(obj) : new TaskContentInfos();
        } catch (JSONException e) {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorInvalidMessage).build());
        }
    }

    @PUT
    @Path("/processcontent/{deploymentId}/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_BPM, Privileges.Constants.ADMINISTRATE_BPM})
    public Response startProcessContent(TaskContentInfos taskContentInfos, @PathParam("id") String id,
                                        @PathParam("deploymentId") String deploymentId, @HeaderParam("Authorization") String auth) {
        bpmService.getActiveBpmProcessDefinitions().stream()
                .filter(p -> p.getVersion().equals(taskContentInfos.processVersion) && p.getProcessName().equals(taskContentInfos.processName) && p.getVersionDB() == Long.valueOf(taskContentInfos.versionDB))
                .findAny()
                .orElseThrow(conflictFactory.conflict()
                        .withActualVersion(() -> Long.valueOf(taskContentInfos.versionDB))
                        .withMessageTitle(MessageSeeds.START_PROCESS_CONCURRENT_TITLE, taskContentInfos.processName)
                        .withMessageBody(MessageSeeds.START_PROCESS_CONCURRENT_BODY, taskContentInfos.processName)
                        .supplier());

        Map<String, Object> expectedParams = getOutputContent(taskContentInfos, -1, id, auth);
        List<Errors> err = new ArrayList<>();
        TaskContentInfos taskContents = getProcessContent(id,deploymentId, auth);
        taskContentInfos.properties.stream()
                .forEach(s -> {
                    if (s.propertyValueInfo.value == null  || "".equals(s.propertyValueInfo.value)) {
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
        if (!err.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new LocalizedFieldException(err)).build();
        }
        if (taskContentInfos.deploymentId != null && taskContentInfos.businessObject.id != null && taskContentInfos.businessObject.value != null) {
            expectedParams.put(taskContentInfos.businessObject.id, taskContentInfos.businessObject.value);
            bpmService.startProcess(taskContentInfos.deploymentId, id, expectedParams, auth);
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/taskcontent/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.EXECUTE_TASK)
    public Response postTaskContent(TaskContentInfos taskContentInfos,
                                    @PathParam("id") long id,
                                    @Context SecurityContext securityContext,
                                    @HeaderParam("Authorization") String auth) {
        String postResult = null;
        List<Errors> err = new ArrayList<>();
        String userName = securityContext.getUserPrincipal().getName();
        if (!"startTask".equals(taskContentInfos.action)) {
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
                        } else if ("".equals(s.propertyValueInfo.value)) {
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
        if (!err.isEmpty()) {
          return  Response.status(Response.Status.BAD_REQUEST).entity(new LocalizedFieldException(err)).build();
        }
        if ("startTask".equals(taskContentInfos.action)) {
            String rest = "/rest/tasks/" + id + "/contentstart/" + securityContext.getUserPrincipal().getName();
            try {
                postResult = bpmService.getBpmServer().doPost(rest, null, auth, 0);
            } catch (RuntimeException e) {
                throw e.getMessage().contains("409")
                        ? conflictFactory.conflict()
                        .withActualVersion(() -> 1L)
                        .withMessageTitle(MessageSeeds.START_TASK_CONCURRENT_TITLE, e.getMessage().replace("409", ""))
                        .withMessageBody(MessageSeeds.START_TASK_CONCURRENT_BODY, e.getMessage().replace("409", ""))
                        .supplier().get()
                        : new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(this.errorNotFoundMessage)
                        .build());
            }
        }
        if ("completeTask".equals(taskContentInfos.action)) {
            Map<String, Object> outputBindingContents = getOutputContent(taskContentInfos, id, null, auth);
            TaskOutputContentInfo taskOutputContentInfo = new TaskOutputContentInfo(outputBindingContents);
            ObjectMapper mapper = new ObjectMapper();
            try {
                String stringJson = mapper.writeValueAsString(taskOutputContentInfo);
                String rest = "/rest/tasks/" + id + "/contentcomplete/" + securityContext.getUserPrincipal().getName();
                try {
                    postResult = bpmService.getBpmServer().doPost(rest, stringJson, auth, 0);
                }catch (RuntimeException e) {
                    throw e.getMessage().contains("409")
                            ? conflictFactory.conflict()
                            .withActualVersion(() -> 1L)
                            .withMessageTitle(MessageSeeds.COMPLETE_TASK_CONCURRENT_TITLE, e.getMessage().replace("409", ""))
                            .withMessageBody(MessageSeeds.COMPLETE_TASK_CONCURRENT_BODY, e.getMessage().replace("409", ""))
                            .supplier().get()
                            : new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                            .entity(this.errorNotFoundMessage)
                            .build());
                }

            } catch (JsonProcessingException e) {
                throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(this.errorInvalidMessage).build());
            }
        }
        if ("saveTask".equals(taskContentInfos.action)) {
            Map<String, Object> outputBindingContents = getOutputContent(taskContentInfos, id, null, auth);
            TaskOutputContentInfo taskOutputContentInfo = new TaskOutputContentInfo(outputBindingContents);
            ObjectMapper mapper = new ObjectMapper();
            try {
                String stringJson = mapper.writeValueAsString(taskOutputContentInfo);
                String rest = "/rest/tasks/" + id + "/contentsave/" + securityContext.getUserPrincipal().getName();
                try{
                    postResult = bpmService.getBpmServer().doPost(rest, stringJson, auth, 0);
                }catch (RuntimeException e) {
                    throw e.getMessage().contains("409")
                            ? conflictFactory.conflict()
                            .withActualVersion(() -> 1L)
                            .withMessageTitle(MessageSeeds.SAVE_TASK_CONCURRENT_TITLE, e.getMessage().replace("409", ""))
                            .withMessageBody(MessageSeeds.SAVE_TASK_CONCURRENT_BODY, e.getMessage().replace("409", ""))
                            .supplier().get()
                            : new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                            .entity(this.errorNotFoundMessage)
                            .build());
                }
            } catch (JsonProcessingException e) {
                throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(this.errorInvalidMessage).build());
            }
        }
        if (postResult == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
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
                    if (s.propertyValueInfo.value == null || "".equals(s.propertyValueInfo.value)) {
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
        if (!err.isEmpty()) {
            return  Response.status(Response.Status.BAD_REQUEST).entity(new LocalizedFieldException(err)).build();
        } else {
            return Response.ok().entity(taskContentInfos).build();
        }
    }

    private Map<String, Object> convertPropertyInfosToProperties(List<PropertySpec> propertySpecs, List<PropertyInfo> properties) {
        Map<String, Object> propertyValues = new LinkedHashMap<>();
        for (PropertySpec propertySpec : propertySpecs) {
            Object value = propertyValueInfoService.findPropertyValue(propertySpec, properties);
            if (value != null) {
                propertyValues.put(propertySpec.getName(), value);
            }
        }
        return propertyValues;
    }

    private Map<String, Object> getOutputContent(TaskContentInfos taskContentInfos, long taskId, String processId, String auth) {
        TaskContentInfos taskContents;
        if (processId != null) {
            taskContents = getProcessContent(processId, taskContentInfos.deploymentId, auth);
        } else {
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
                            if ("TIMESTAMP".equals(taskContentInfo.get().propertyTypeInfo.simplePropertyType)) {
                                Date date = new Date();
                                if (taskContentInfo.get().propertyValueInfo != null && taskContentInfo.get().propertyValueInfo.value != null) {
                                    date.setTime(Long.valueOf(taskContentInfo.get().propertyValueInfo.value));
                                    outputBindingContents.put(s.outputBinding, date);
                                }
                            } else if ("DATE".equals(taskContentInfo.get().propertyTypeInfo.simplePropertyType)) {
                                Date date = new Date();
                                if (taskContentInfo.get().propertyValueInfo != null && taskContentInfo.get().propertyValueInfo.value != null) {
                                    date.setTime(Long.valueOf(taskContentInfo.get().propertyValueInfo.value));
                                    outputBindingContents.put(s.outputBinding, date);
                                }
                            } else if (taskContentInfo.get().propertyTypeInfo.predefinedPropertyValuesInfo != null) {
                                if ("COMBOBOX".equals(taskContentInfo.get().propertyTypeInfo.predefinedPropertyValuesInfo.selectionMode)) {
                                    Iterator<String> it = s.propertyTypeInfo.predefinedPropertyValuesInfo.comboKeys.keySet().iterator();
                                    while (it.hasNext()) {
                                        String theKey = it.next();
                                        if (s.propertyTypeInfo.predefinedPropertyValuesInfo.comboKeys.get(theKey).equals(taskContentInfo.get().propertyValueInfo.value)) {
                                            outputBindingContents.put(s.outputBinding, theKey);
                                        }
                                    }
                                } else {
                                    if(taskContentInfo.get().propertyValueInfo != null) {
                                        outputBindingContents.put(s.outputBinding, taskContentInfo.get().propertyValueInfo.value);
                                    }
                                }
                            } else {
                                if(taskContentInfo.get().propertyValueInfo != null) {
                                    outputBindingContents.put(s.outputBinding, taskContentInfo.get().propertyValueInfo.value);
                                }
                            }
                        }
                    });
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
        return outputBindingContents;
    }

    private List<String> getPropertyList(String source, String propertyValue) {
        List<String> privilegesList = new ArrayList<>();
        try {
            if (source != null) {
                JsonNode node = new ObjectMapper().readValue(new ByteArrayInputStream(source.getBytes()), JsonNode.class);
                if (node != null && node.isArray()) {
                    for (JsonNode singleFilter : node) {
                        JsonNode property = singleFilter.get(propertyValue);
                        if (property != null && property.textValue() != null) {
                            privilegesList.add(property.textValue());
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(this.errorInvalidMessage).build());
        }
        return privilegesList;
    }

    private void doUpdatePrivileges(BpmProcessDefinition process, List<BpmProcessPrivilege> newPrivileges, List<BpmProcessPrivilege> oldPrivileges) {
        List<BpmProcessPrivilege> toRevoke = oldPrivileges.stream()
                .filter(oldPrivilege -> !newPrivileges.stream()
                        .anyMatch(newPrivilege -> oldPrivilege.getPrivilegeName()
                                .equals(newPrivilege.getPrivilegeName())))
                .collect(Collectors.toList());
        List<BpmProcessPrivilege> toGrant = newPrivileges.stream()
                .filter(oldPrivilege -> !oldPrivileges.stream()
                        .anyMatch(newPrivilege -> oldPrivilege.getPrivilegeName()
                                .equals(newPrivilege.getPrivilegeName())))
                .map(privilege -> {
                    privilege.setProcessId(process.getId());
                    return privilege;
                })
                .collect(Collectors.toList());
        process.revokePrivileges(toRevoke);
        process.grantPrivileges(toGrant);
    }


    private String getQueryValue(UriInfo uriInfo, String key) {
        return uriInfo.getQueryParameters().getFirst(key);
    }

    private String getQueryParam(QueryParameters queryParam) {
        String req = "";
        int i = 0;
        Iterator<String> it = queryParam.keySet().iterator();
        while (it.hasNext()) {
            String theKey = it.next();
            if (i > 0) {
                req += "&";
            } else {
                req += "?";
            }
            req += theKey + "=" +  queryParam.getFirst(theKey);
            i++;
        }
        return req;
    }

    private ProcessDefinitionInfos getAvailableProcessesByAppKey(UriInfo uriInfo, String auth, String appKey) {
        MultivaluedMap<String, String> filterProperties = uriInfo.getQueryParameters();
        List<BpmProcessDefinition> activeProcesses = bpmService.getActiveBpmProcessDefinitions(appKey);
        return (filterProperties.get("type") != null) ? filterProcesses(activeProcesses, filterProperties.get("type").get(0), auth) : filterProcesses(activeProcesses, null, auth);
    }

}