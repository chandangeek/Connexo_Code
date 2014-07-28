package com.energyict.mdc.dashboard.rest.status;

import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.energyict.mdc.engine.status.ComServerType;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.joda.time.DateTimeConstants;

import javax.inject.Inject;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Models the REST resource that gets the summary of all the statusse
 * of the {@link com.energyict.mdc.engine.model.ComServer}s
 * that are configured in the system by invoking the
 * {@link ComServerStatusResource} for each such ComServer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-22 (16:23)
 */
@Path("/comserverstatussummary")
public class ComServerStatusSummaryResource {

    private static final Logger LOGGER = Logger.getLogger(ComServerStatusSummaryResource.class.getName());

    private final EngineModelService engineModelService;

    @Inject
    public ComServerStatusSummaryResource(EngineModelService engineModelService) {
        super();
        this.engineModelService = engineModelService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ComServerStatusSummaryInfo getComServerStatusSummary(@Context UriInfo uriInfo) {
        Client jerseyClient = this.newJerseyClient();
        ComServerStatusSummaryInfo statusSummaryInfo = new ComServerStatusSummaryInfo();
        for (OnlineComServer comServer : this.findAllOnlineComServers()) {
            this.addStatusInfo(statusSummaryInfo, comServer, jerseyClient);
        }
        for (RemoteComServer comServer : this.findAllRemoteComServers()) {
            this.addStatusInfo(statusSummaryInfo, comServer, jerseyClient);
        }
        return statusSummaryInfo;
    }

    private Client newJerseyClient() {
        // Todo: remove hard coding of development username/pass and replace with interval user that has specific privileges
        HttpAuthenticationFeature basicAuthentication = HttpAuthenticationFeature.basic("admin", "admin");
        return ClientBuilder.newClient().
                register(new JacksonFeature()).
                register(basicAuthentication).
                property(ClientProperties.CONNECT_TIMEOUT, DateTimeConstants.MILLIS_PER_SECOND * 5).
                property(ClientProperties.READ_TIMEOUT, DateTimeConstants.MILLIS_PER_SECOND * 2);
    }

    private void addStatusInfo(ComServerStatusSummaryInfo statusSummaryInfo, OnlineComServer comServer, Client jerseyClient) {
        this.addStatusInfo(statusSummaryInfo, jerseyClient, comServer.getName(), comServer.getStatusUri(), ComServerType.ONLINE);
    }

    private void addStatusInfo(ComServerStatusSummaryInfo statusSummaryInfo, RemoteComServer comServer, Client jerseyClient) {
        this.addStatusInfo(statusSummaryInfo, jerseyClient, comServer.getName(), comServer.getStatusUri(), ComServerType.REMOTE);
    }

    private void addStatusInfo(ComServerStatusSummaryInfo statusSummaryInfo, Client jerseyClient, String comServerName, String statusUri, ComServerType comServerType) {
        try {
            LOGGER.log(Level.FINE, "Executing " + statusUri);

            ComServerStatusInfo comServerStatusInfo =
                jerseyClient.
                    target(statusUri).
                    request(MediaType.APPLICATION_JSON).
//                    property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, "admin").
//                    property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, "admin").
                    get(ComServerStatusInfo.class);
            statusSummaryInfo.add(comServerStatusInfo);
        }
        catch (ClientErrorException | ProcessingException e) {
            /* Occurrence of ProcessingException was established when debugging the situation
             * where the host name of the ComServer was not known to the dns service.
             * The underlying exception in this case is: java.net.UnknownHostException */
            /* The ComServerStatusResource of this ComServer is not accessible,
             * most likely because the ComServer is not running. */
            LOGGER.info("ComServer " + comServerName + " is mostly likely not running");
            LOGGER.log(Level.FINE, "ComServer " + comServerName + " is mostly likely not running", e);
            ComServerStatusInfo statusInfo = new ComServerStatusInfo();
            statusInfo.comServerName = comServerName;
            statusInfo.comServerType = comServerType;
            statusInfo.blocked = false;
            statusInfo.running = false;
            statusSummaryInfo.add(statusInfo);
         }
    }

    private List<OnlineComServer> findAllOnlineComServers() {
        return this.engineModelService.findAllOnlineComServers();
    }

    private List<RemoteComServer> findAllRemoteComServers() {
        return this.engineModelService.findAllRemoteComServers();
    }

}