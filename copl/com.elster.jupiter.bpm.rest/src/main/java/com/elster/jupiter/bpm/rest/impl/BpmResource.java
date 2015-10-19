package com.elster.jupiter.bpm.rest.impl;

import com.elster.jupiter.bpm.BpmServer;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.rest.*;
import com.elster.jupiter.bpm.rest.resource.StandardParametersBean;
import com.elster.jupiter.bpm.security.Privileges;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.util.*;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/runtime")
public class BpmResource {

    public static final String START = "start";
    public static final String LIMIT = "limit";
    public static final String ME = "me";
    public static final String LIKE = "like";
    private UserService userService;
    private Thesaurus thesaurus;
    private final BpmService bpmService;

    @Inject
    public BpmResource(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Inject
    public void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @GET
    @Path("/deployments")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.VIEW_BPM)
    public DeploymentInfos getAllDeployments(@Context UriInfo uriInfo) {
        return getAllDeployments();
    }

    @GET
    @Path("/startup")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.VIEW_BPM)
    public StartupInfo getStartup(@Context UriInfo uriInfo) {
        StartupInfo startupInfo = new StartupInfo();
        BpmServer server = bpmService.getBpmServer();
        startupInfo.url = server.getUrl();

        return startupInfo;
    }

    @GET
    @Path("/instances")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.VIEW_BPM)
    public ProcessInstanceInfos getAllInstances(@Context UriInfo uriInfo) {
        String jsonContent;
        JSONArray arr = null;
        DeploymentInfos deploymentInfos = getAllDeployments();
        if (deploymentInfos != null && deploymentInfos.total > 0) {
            // TODO:
            // Apparently - although not in line with the documentation - all instances are returned regardless of the deployment id
            // For future versions, we need to revise if this behavior changes
            //for (DeploymentInfo deployment : deploymentInfos.getDeployments()) {
            try {
                DeploymentInfo deployment = deploymentInfos.deployments.get(0);
                jsonContent = bpmService.getBpmServer().doGet("/rest/runtime/" + deployment.identifier + "/history/instances");
                if (!"".equals(jsonContent)) {
                    JSONObject obj = new JSONObject(jsonContent);
                    arr = obj.getJSONArray("result");
                }
            } catch (JSONException e) {
                // TODO: for now, an empty grid will be shown; in the future, we may display a more specific error message
            } catch (IOException e) {
                // TODO: for now, an empty grid will be shown; in the future, we may display a more specific error message
            }
        }
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        return new ProcessInstanceInfos(arr, queryParameters.getLimit(), queryParameters.getStartInt());
    }

    @GET
    @Path("/deployment/{deploymentId}/instance/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.VIEW_BPM)
    public ProcessInstanceInfo getInstance(@Context UriInfo uriInfo, @PathParam("deploymentId") String deploymentId, @PathParam("id") long instanceId) {
        JSONObject obj = null;
        String jsonContent;
        try {

            jsonContent = bpmService.getBpmServer().doGet("/rest/runtime/" + deploymentId + "/history/instance/" + instanceId);
            if (!"".equals(jsonContent)) {
                obj = (new JSONObject(jsonContent)).getJSONArray("result").getJSONObject(0);
            }
        } catch (JSONException e) {
            // TODO: for now, an empty grid will be shown; in the future, we may display a more specific error message
        } catch (IOException e) {
            // TODO: for now, an empty grid will be shown; in the future, we may display a more specific error message
        }
        return new ProcessInstanceInfo(obj);
    }

    @GET
    @Path("/deployment/{deploymentId}/instance/{id}/nodes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.VIEW_BPM)
    public NodeInfos getNodes(@Context UriInfo uriInfo, @PathParam("deploymentId") String deploymentId, @PathParam("id") long instanceId) {
        JSONArray arr = null;
        String jsonContent;
        try {

            jsonContent = bpmService.getBpmServer().doGet("/rest/runtime/" + deploymentId + "/history/instance/" + instanceId + "/node");
            if (!"".equals(jsonContent)) {
                arr = (new JSONObject(jsonContent)).getJSONArray("result");
            }
        } catch (JSONException e) {
            // TODO: for now, an empty grid will be shown; in the future, we may display a more specific error message
        } catch (IOException e) {
            // TODO: for now, an empty grid will be shown; in the future, we may display a more specific error message
        }
        return new NodeInfos(arr);
    }


    @GET
    @Path("/deployment/{deploymentId}/instance/{id}/variables")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.VIEW_BPM)
    public VariableInfos getVariables(@Context UriInfo uriInfo, @PathParam("deploymentId") String deploymentId, @PathParam("id") long instanceId) {
        JSONArray arr = null;
        String jsonContent;
        try {
            jsonContent = bpmService.getBpmServer().doGet("/rest/runtime/" + deploymentId + "/history/instance/" + instanceId + "/variable");
            if (!"".equals(jsonContent)) {
                arr = (new JSONObject(jsonContent)).getJSONArray("result");
            }
        } catch (JSONException e) {
            // TODO: for now, an empty grid will be shown; in the future, we may display a more specific error message
        } catch (IOException e) {
            // TODO: for now, an empty grid will be shown; in the future, we may display a more specific error message
        }
        return new VariableInfos(arr);
    }

    private DeploymentInfos getAllDeployments() {
        String jsonContent;
        JSONArray arr = null;
        try {
            jsonContent = bpmService.getBpmServer().doGet("/rest/deployment");
            if (!"".equals(jsonContent)) {
                arr = new JSONArray(jsonContent);
            }
        } catch (JSONException e) {
            // TODO: for now, an empty grid will be shown; in the future, we may display a more specific error message
        } catch (IOException e) {
            // TODO: for now, an empty grid will be shown; in the future, we may display a more specific error message
        }
        return new DeploymentInfos(arr);
    }

    @GET
    @Path("/tasks")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_TASK, Privileges.ASSIGN_TASK, Privileges.EXECUTE_TASK})
    public TaskInfos getTask(@Context UriInfo uriInfo, @BeanParam JsonQueryFilter filterX) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters(false));
        String jsonContent;
        int total = -1;
        JSONArray arr = null;
        try{
            String rest = "/rest/tasks";
            String req  = getQueryParam(queryParameters);
            if(!req.equals("")){
                rest += req;
            }
            jsonContent = bpmService.getBpmServer().doGet(rest);
            if (!"".equals(jsonContent)) {
                JSONObject obj = new JSONObject(jsonContent);
                total = Integer.valueOf(obj.get("total").toString());
                arr = obj.getJSONArray("tasks");
            }
        }catch (JSONException e) {
            // TODO: for now, an empty grid will be shown; in the future, we may display a more specific error message
        } catch (IOException e) {
            // TODO: for now, an empty grid will be shown; in the future, we may display a more specific error message
        }
        TaskInfos infos = new TaskInfos(arr);
        if(total > 0) {
            infos.total = total;
        }
        return infos;
    }

    @GET
    @Path("/tasks/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_TASK, Privileges.ASSIGN_TASK, Privileges.EXECUTE_TASK})
    public TaskInfo getTask(@PathParam("id")long id) {
        String jsonContent;
        TaskInfo taskInfo= new TaskInfo();
        try{
            String rest = "/rest/tasks/";
            rest += String.valueOf(id);
            jsonContent = bpmService.getBpmServer().doGet(rest);
            if (!"".equals(jsonContent)) {
                JSONObject obj = new JSONObject(jsonContent);
                taskInfo = new TaskInfo(obj);
            }

        }catch (JSONException e) {
        } catch (IOException e) {
        }
        return taskInfo;
    }

    @GET
    @Path("/processes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_TASK, Privileges.ASSIGN_TASK, Privileges.EXECUTE_TASK})
    public ProcessDefinitionInfos getProcesses(@Context UriInfo uriInfo) {
        String jsonContent;
        JSONArray arr = null;
        try{
            jsonContent = bpmService.getBpmServer().doGet("/rest/deployment/processes");
            if (!"".equals(jsonContent)) {
                JSONObject jsnobject = new JSONObject(jsonContent);
                arr = jsnobject.getJSONArray("processDefinitionList");
            }

        }catch (JSONException e) {
            // TODO: for now, an empty grid will be shown; in the future, we may display a more specific error message
        } catch (IOException e) {
            // TODO: for now, an empty grid will be shown; in the future, we may display a more specific error message
        }
        return new ProcessDefinitionInfos(arr);
    }

    @GET
    @Path("/assignees/users")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_TASK, Privileges.ASSIGN_TASK, Privileges.EXECUTE_TASK})
    public Response getUsers(@BeanParam StandardParametersBean params){
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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ASSIGN_TASK)
    public Response assignUser(@Context UriInfo uriInfo,@PathParam("id") long id,@Context SecurityContext securityContext,
                               @QueryParam("username") String userName){
        String rest = "/rest/tasks/";
        rest += String.valueOf(id);
        if(userName != null && !userName.isEmpty()) {
            rest += "/assign?username=" + userName;
            rest += "&currentuser=" + securityContext.getUserPrincipal().getName();
            bpmService.getBpmServer().doPost(rest);
            return Response.ok().build();
        }

        throw new LocalizedFieldValidationException(MessageSeeds.FIELD_CAN_NOT_BE_EMPTY, "assignee");
    }

    @GET
    @Path("/assignees")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_TASK, Privileges.ASSIGN_TASK, Privileges.EXECUTE_TASK})
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
        if(params.getStart() == 0 && (searchText == null || searchText.isEmpty())) {
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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ASSIGN_TASK)
    public Response assignUser(@Context UriInfo uriInfo,@PathParam("id") long id) {
        String priority = getQueryValue(uriInfo, "priority");
        String date = getQueryValue(uriInfo, "duedate");
        String rest = "/rest/tasks/";
        rest += String.valueOf(id) + "/set";
        if(priority != null || date != null) {
            if(priority !=null) {
                rest += "?priority=" + priority;
                if(date !=null) {
                    rest += "&duedate=" + date;
                }
            }else{
                rest += "?duedate=" + date;
            }
            bpmService.getBpmServer().doPost(rest);
            return Response.ok().build();
        }
        return Response.notModified().build();
    }

    private String getQueryParam(QueryParameters queryParam){
        String req = "";
        int i = 0;
        Iterator<String> it = queryParam.keySet().iterator();
        while(it.hasNext()){
            String theKey = (String)it.next();
            if(i > 0 ){
                req += "&";
            }else{
                req += "?";
            }
            req += theKey+"="+queryParam.getFirst(theKey);
            i++;
        }
        return req;
    }

    private String getQueryValue(UriInfo uriInfo,String key){
        return uriInfo.getQueryParameters().getFirst(key);
    }

}
