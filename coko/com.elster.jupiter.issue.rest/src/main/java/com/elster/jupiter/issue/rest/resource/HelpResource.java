package com.elster.jupiter.issue.rest.resource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * This class should be used only in test purpose
 */
@Path("/issue")
@Deprecated
public class HelpResource extends BaseResource {

    @GET
    @Path("/event")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getEvent(
           @QueryParam("topic") @DefaultValue("com/energyict/mdc/connectiontasksetup/FAILURE") String topic,
           @QueryParam("comPortName") @DefaultValue("TCP") String comPortName,
           @QueryParam("comServerName") @DefaultValue("FakeComServer") String comServerName,
           @QueryParam("deviceIdentifier") @DefaultValue("1") String deviceIdentifier,
           @QueryParam("connectionTypePluggableClassId") @DefaultValue("1") long connectionTypePluggableClassId,
           @QueryParam("comTaskId") @DefaultValue("1") String comTaskId,
           @QueryParam("discoveryProtocolId") @DefaultValue("1") long discoveryProtocolId,
           @QueryParam("masterDeviceId") @DefaultValue("1") String masterDeviceId){
        long timestamp = System.currentTimeMillis();

        getIssueHelpService().postEvent(timestamp, topic, comPortName, comServerName, deviceIdentifier, connectionTypePluggableClassId, comTaskId, discoveryProtocolId, masterDeviceId);
        return "Event was send. It has following properties:\n    "+
            "timestamp - " + timestamp + "\n    " +
            "topic - " + topic + "\n    " +
            "comPortName - " + comPortName + "\n    " +
            "comServerName - " + comServerName + "\n    " +
            "deviceIdentifier - " + deviceIdentifier + "\n    " +
            "connectionTypePluggableClassId - " + connectionTypePluggableClassId + "\n    " +
            "comTaskId - " + comTaskId + "\n    " +
            "discoveryProtocolId - " + discoveryProtocolId + "\n    " +
            "masterDeviceId - " + masterDeviceId + "\n    ";
    }
}
