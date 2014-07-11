package com.elster.jupiter.bpm.rest.impl;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.rest.*;
import com.elster.jupiter.rest.util.QueryParameters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

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
    public DeploymentInfos getAllDeployments(@Context UriInfo uriInfo) {
        return getAllDeployments();
    }

    @GET
    @Path("/startup")
    @Produces(MediaType.APPLICATION_JSON)
    public StartupInfo getStartup(@Context UriInfo uriInfo) {
        return new StartupInfo();
    }

    @GET
    @Path("/instances")
    @Produces(MediaType.APPLICATION_JSON)
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
                DeploymentInfo deployment = deploymentInfos.deployments.get(0);
                rest.clearJsonContent();
                rest.doGet("/rest/runtime/"+deployment.identifier+"/history/instances");
                jsonContent = rest.getJsonContent();
                if (!jsonContent.equals("")) {
                    try {
                        JSONObject obj = new JSONObject(jsonContent);
                        arr = obj.getJSONArray("result");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            //}
        }
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        return new ProcessInstanceInfos(arr, queryParameters.getLimit(), queryParameters.getStart());
    }

    @GET
    @Path("/deployment/{deploymentId}/instance/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProcessInstanceInfo getInstance(@Context UriInfo uriInfo, @PathParam("deploymentId") String deploymentId, @PathParam("id") long instanceId) {
        JSONObject obj = null;
        String jsonContent;
        BpmRestCall rest =  new BpmRestCall();
        rest.doGet("/rest/runtime/"+deploymentId+"/history/instance/"+instanceId);
        jsonContent = rest.getJsonContent();
        if (!jsonContent.equals("")) {
            try{
                obj = (new JSONObject(jsonContent)).getJSONArray("result").getJSONObject(0);
            } catch (JSONException e ) {
                throw new RuntimeException(e);
            }
        }
        return new ProcessInstanceInfo(obj);
    }

    @GET
    @Path("/deployment/{deploymentId}/instance/{id}/nodes")
    @Produces(MediaType.APPLICATION_JSON)
    public NodeInfos getNodes(@Context UriInfo uriInfo, @PathParam("deploymentId") String deploymentId, @PathParam("id") long instanceId) {
        JSONArray arr = null;
        String jsonContent;
        BpmRestCall rest =  new BpmRestCall();
        rest.doGet("/rest/runtime/"+deploymentId+"/history/instance/"+instanceId+"/node");
        jsonContent = rest.getJsonContent();
        if (!jsonContent.equals("")) {
            try{
                arr = (new JSONObject(jsonContent)).getJSONArray("result");
            } catch (JSONException e ) {
                throw new RuntimeException(e);
            }
        }
        return new NodeInfos(arr);
    }



    @GET
    @Path("/deployment/{deploymentId}/instance/{id}/variables")
    @Produces(MediaType.APPLICATION_JSON)
    public VariableInfos getVariables(@Context UriInfo uriInfo, @PathParam("deploymentId") String deploymentId, @PathParam("id") long instanceId) {
        JSONArray arr = null;
        String jsonContent;
        BpmRestCall rest =  new BpmRestCall();
        rest.doGet("/rest/runtime/"+deploymentId+"/history/instance/"+instanceId+"/variable");
        jsonContent = rest.getJsonContent();
        if (!jsonContent.equals("")) {
            try{
                arr = (new JSONObject(jsonContent)).getJSONArray("result");
            } catch (JSONException e ) {
                throw new RuntimeException(e);
            }
        }
        return new VariableInfos(arr);
    }

    private DeploymentInfos getAllDeployments() {
        String jsonContent;
        JSONArray arr = null;
        BpmRestCall rest =  new BpmRestCall();
        rest.doGet("/rest/deployment");
        jsonContent = rest.getJsonContent();
        if (!jsonContent.equals("")) {
            try{
                arr = new JSONArray(jsonContent);
            } catch (JSONException e ) {
                throw new RuntimeException(e);
            }
        }
        return new DeploymentInfos(arr);
    }
}
