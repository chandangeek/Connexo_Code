/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import javax.inject.Inject;

/**
 * This class links web services/endpoint to AppServer. If an end point becomes enable/disabled, it shall be enabled/disabled on
 * all AppServers it is configured for.
 * <p>
 * No enable/disable is possible on a per-appserver basis: don't link the endpoint
 * to an appserver if you don't want to support the end point on the app server.
 * <p>
 * Created by bvn on 5/18/16.
 */
public class EndPointForAppServerImpl implements WebServiceForAppServer {
    private final DataModel dataModel;
    private final Reference<AppServer> appServer = Reference.empty();
    private final Reference<EndPointConfiguration> endPointConfiguration = Reference.empty();

    @Inject
    public EndPointForAppServerImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public enum Fields {
        AppServer("appServer"),
        EndPointConfiguration("endPointConfiguration");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    EndPointForAppServerImpl init(AppServer appServer, EndPointConfiguration endPointConfiguration) {
        this.appServer.set(appServer);
        this.endPointConfiguration.set(endPointConfiguration);
        return this;
    }

    @Override
    public EndPointConfiguration getEndPointConfiguration() {
        return endPointConfiguration.get();
    }

    @Override
    public AppServer getAppServer() {
        return appServer.get();
    }

    public void save() {
        Save.CREATE.save(dataModel, this);
    }
}
