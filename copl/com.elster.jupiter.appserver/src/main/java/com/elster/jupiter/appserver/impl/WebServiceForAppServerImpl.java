package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.soap.whiteboard.EndPointConfiguration;

/**
 * This class links web services to AppServer. If a web service becomes enable/disabled, it shall be enabled/disabled on
 * all AppServers it is configured for.
 * <p>
 * No enable/disable is possible on a per-appserver basis: don't link the webservice
 * to an appserver if you don't want to enable the web service on the app server.
 * <p>
 * Created by bvn on 5/18/16.
 */
public class WebServiceForAppServerImpl implements WebServiceForAppServer {
    private final Reference<AppServer> appServer = Reference.empty();
    private final Reference<EndPointConfiguration> endPointConfiguration = Reference.empty();

    public WebServiceForAppServerImpl(AppServer appServer, EndPointConfiguration endPointConfiguration) {
        this.appServer.set(appServer);
        this.endPointConfiguration.set(endPointConfiguration);
    }

    @Override
    public EndPointConfiguration getEndPointConfiguration() {
        return endPointConfiguration.get();
    }

    @Override
    public AppServer getAppServer() {
        return appServer.get();
    }
}
