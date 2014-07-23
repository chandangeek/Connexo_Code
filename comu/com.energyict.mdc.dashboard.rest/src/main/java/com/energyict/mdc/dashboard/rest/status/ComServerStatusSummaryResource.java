package com.energyict.mdc.dashboard.rest.status;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.status.ComServerType;

import org.glassfish.jersey.client.ClientProperties;
import org.joda.time.DateTimeConstants;

import javax.inject.Inject;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
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
    public ComServerStatusSummaryInfo getComServerStatusSummary() {
        Client jerseyClient = this.newJerseyClient();
        ComServerStatusSummaryInfo statusSummaryInfo = new ComServerStatusSummaryInfo();
        for (ComServer comServer : this.findAllComServers()) {
            this.addStatusInfo(statusSummaryInfo, comServer, jerseyClient);
        }
        return statusSummaryInfo;
    }

    private Client newJerseyClient() {
        return ClientBuilder.newClient().
                property(ClientProperties.CONNECT_TIMEOUT, DateTimeConstants.MILLIS_PER_SECOND * 5).
                property(ClientProperties.READ_TIMEOUT, DateTimeConstants.MILLIS_PER_SECOND * 2);
    }

    private void addStatusInfo(ComServerStatusSummaryInfo statusSummaryInfo, ComServer comServer, Client jerseyClient) {
        try {
            String uri = comServer.getEventRegistrationUriIfSupported();
            LOGGER.log(Level.FINE, "Executing " + uri + "/api/dsb/comserverstatus");
            ComServerStatusInfo comServerStatusInfo =
                jerseyClient.
                    target(uri).
                    path("api/dsb/comserverstatus").
                    request(MediaType.APPLICATION_JSON).
                    get(ComServerStatusInfo.class);
            statusSummaryInfo.add(comServerStatusInfo);
        }
        catch (ClientErrorException e) {
            /* The ComServerStatusResource of this ComServer is not accessible,
             * most likely because the ComServer is not running. */
            LOGGER.info("ComServer " + comServer.getName() + " is mostly likely not running");
            LOGGER.log(Level.FINE, "ComServer " + comServer.getName() + " is mostly likely not running", e);
            ComServerStatusInfo statusInfo = new ComServerStatusInfo();
            statusInfo.comServerName = comServer.getName();
            statusInfo.comServerType = ComServerType.typeFor(comServer);
            statusInfo.blocked = false;
            statusInfo.running = false;
            statusSummaryInfo.add(statusInfo);
         }
        catch (BusinessException e) {
            /* The event api is not supported on this ComServer,
             * therefore it cannot be included in the summary. */
            LOGGER.info("Excluding ComServer " + comServer.getName() + " from summary because the event registration api is not configured");
         }
    }

    private List<ComServer> findAllComServers() {
        return this.engineModelService.findAllComServers().find();
    }

}