package com.elster.jupiter.bpm.rest.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.rest.*;
import com.elster.jupiter.bpm.security.Privileges;
import com.elster.jupiter.rest.util.QueryParameters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

@Path("/runtime")
public class BpmResource {

    private final BpmService bpmService;

    @Inject
    public BpmResource(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @GET
    @Path("/deployments")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_BPM)
    public DeploymentInfos getAllDeployments(@Context UriInfo uriInfo) {
        return getAllDeployments();
    }

    @GET
    @Path("/startup")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_BPM)
    public StartupInfo getStartup(@Context UriInfo uriInfo) {
        return new StartupInfo();
    }

    @GET
    @Path("/instances")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_BPM)
    public ProcessInstanceInfos getAllInstances(@Context UriInfo uriInfo) {
        String jsonContent;
        JSONArray arr = null;
        DeploymentInfos deploymentInfos = getAllDeployments();
        if (deploymentInfos != null && deploymentInfos.total > 0) {
            BpmRestCall rest =  new BpmRestCall();
            // TODO:
            // Apparently - although not in line with the documentation - all instances are returned regardless of the deployment id
            // For future versions, we need to revise if this behavior changes
            //for (DeploymentInfo deployment : deploymentInfos.getDeployments()) {
            try{
                DeploymentInfo deployment = deploymentInfos.deployments.get(0);
                rest.clearJsonContent();
                rest.doGet("/rest/runtime/"+deployment.identifier+"/history/instances");
                jsonContent = rest.getJsonContent();
                if (!jsonContent.equals("")) {
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
        return new ProcessInstanceInfos(arr, queryParameters.getLimit(), queryParameters.getStart());
    }

    @GET
    @Path("/deployment/{deploymentId}/instance/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_BPM)
    public ProcessInstanceInfo getInstance(@Context UriInfo uriInfo, @PathParam("deploymentId") String deploymentId, @PathParam("id") long instanceId) {
        JSONObject obj = null;
        String jsonContent;
        BpmRestCall rest =  new BpmRestCall();
        try{
            rest.doGet("/rest/runtime/"+deploymentId+"/history/instance/"+instanceId);
            jsonContent = rest.getJsonContent();
            if (!jsonContent.equals("")) {
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
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_BPM)
    public NodeInfos getNodes(@Context UriInfo uriInfo, @PathParam("deploymentId") String deploymentId, @PathParam("id") long instanceId) {
        JSONArray arr = null;
        String jsonContent;
        BpmRestCall rest =  new BpmRestCall();
        try{
            rest.doGet("/rest/runtime/"+deploymentId+"/history/instance/"+instanceId+"/node");
            jsonContent = rest.getJsonContent();
            if (!jsonContent.equals("")) {
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
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_BPM)
    public VariableInfos getVariables(@Context UriInfo uriInfo, @PathParam("deploymentId") String deploymentId, @PathParam("id") long instanceId) {
        JSONArray arr = null;
        String jsonContent;
        BpmRestCall rest =  new BpmRestCall();
        try{
            rest.doGet("/rest/runtime/"+deploymentId+"/history/instance/"+instanceId+"/variable");
            jsonContent = rest.getJsonContent();
            if (!jsonContent.equals("")) {
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
        BpmRestCall rest =  new BpmRestCall();
        try{
            rest.doGet("/rest/deployment");
            jsonContent = rest.getJsonContent();
            if (!jsonContent.equals("")) {
                arr = new JSONArray(jsonContent);
            }
        } catch (JSONException e) {
            // TODO: for now, an empty grid will be shown; in the future, we may display a more specific error message
        } catch (IOException e) {
            // TODO: for now, an empty grid will be shown; in the future, we may display a more specific error message
        }
        return new DeploymentInfos(arr);
    }
}
