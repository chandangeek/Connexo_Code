/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status;

import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.RemoteComServer;
import com.energyict.mdc.engine.config.security.Privileges;
import com.energyict.mdc.engine.status.ComServerType;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.joda.time.DateTimeConstants;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.logging.Logger;

/**
 * Models the REST resource that gets the summary of all the statuses
 * of the {@link com.energyict.mdc.engine.config.ComServer}s
 * that are configured in the system by invoking the
 * {@link ComServerStatusResource} for each such ComServer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-22 (16:23)
 */
@Path("/comserverstatussummary")
public class ComServerStatusSummaryResource {

    private static final Logger LOGGER = Logger.getLogger(ComServerStatusSummaryResource.class.getName());

    private final EngineConfigurationService engineConfigurationService;
    private final ComServerStatusInfoFactory comServerStatusInfoFactory;

    @Inject
    public ComServerStatusSummaryResource(EngineConfigurationService engineConfigurationService, ComServerStatusInfoFactory comServerStatusInfoFactory) {
        super();
        this.engineConfigurationService = engineConfigurationService;
        this.comServerStatusInfoFactory = comServerStatusInfoFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION, Privileges.Constants.VIEW_STATUS_COMMUNICATION_INFRASTRUCTURE})
    public ComServerStatusSummaryInfo getComServerStatusSummary(@Context UriInfo uriInfo, @HeaderParam("Authorization") String authorization) {
        Client jerseyClient = this.newJerseyClient();
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(ComServerStatusResource.class).host("{host}");
        ComServerStatusSummaryInfo statusSummaryInfo = new ComServerStatusSummaryInfo();
        this.engineConfigurationService.findAllOnlineComServers().stream().filter(ComServer::isActive).forEach(cs -> addStatusInfo(statusSummaryInfo, cs, jerseyClient, authorization, uriBuilder));
        this.engineConfigurationService.findAllRemoteComServers().stream().filter(ComServer::isActive).forEach(cs -> addStatusInfo(statusSummaryInfo, cs, jerseyClient, authorization, uriBuilder));
        return statusSummaryInfo;
    }

    private Client newJerseyClient() {
        return ClientBuilder.newClient().
                register(new JacksonFeature()).
                property(ClientProperties.CONNECT_TIMEOUT, DateTimeConstants.MILLIS_PER_SECOND * 5).
                property(ClientProperties.READ_TIMEOUT, DateTimeConstants.MILLIS_PER_SECOND * 2);
    }

    private void addStatusInfo(ComServerStatusSummaryInfo statusSummaryInfo, OnlineComServer comServer, Client jerseyClient, String authorizationHeader, UriBuilder uriBuilder) {
        this.addStatusInfo(statusSummaryInfo, jerseyClient, authorizationHeader, comServer.getId(), comServer.getName(), comServer.getStatusUri(), ComServerType.ONLINE);
    }

    private void addStatusInfo(ComServerStatusSummaryInfo statusSummaryInfo, RemoteComServer comServer, Client jerseyClient, String authorizationHeader, UriBuilder uriBuilder) {
        this.addStatusInfo(statusSummaryInfo, jerseyClient, authorizationHeader, comServer.getId(), comServer.getName(), comServer.getStatusUri(), ComServerType.REMOTE);
    }

    private void addStatusInfo(ComServerStatusSummaryInfo statusSummaryInfo, Client jerseyClient, String authorizationHeader, long comServerId, String comServerName, String statusUri, ComServerType comServerType) {
        try {
            LOGGER.fine(() -> "Executing " + statusUri);
            ComServerStatusInfo comServerStatusInfo =
                    jerseyClient.
                            target(statusUri).
                            request(MediaType.APPLICATION_JSON).
                            header("Authorization", authorizationHeader)
                            .get(ComServerStatusInfo.class);
            statusSummaryInfo.comServerStatusInfos.add(comServerStatusInfoFactory.translate(comServerStatusInfo));
        } catch (ClientErrorException | ProcessingException e) {
            /* Occurrence of ProcessingException was established when debugging the situation
             * where the host name of the ComServer was not known to the dns service.
             * The underlying exception in this case is: java.net.UnknownHostException */
            /* The ComServerStatusResource of this ComServer is not accessible,
             * most likely because the ComServer is not running. */
            LOGGER.info(() -> "ComServer " + comServerName + " is most likely not running");
            ComServerStatusInfo statusInfo = comServerStatusInfoFactory.from(comServerId, comServerName, statusUri, comServerType);
            statusSummaryInfo.comServerStatusInfos.add(comServerStatusInfoFactory.translate(statusInfo));
        }
    }

}