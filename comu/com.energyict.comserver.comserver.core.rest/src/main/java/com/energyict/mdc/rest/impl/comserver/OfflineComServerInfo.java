/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.config.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Optional;

@XmlRootElement
public class OfflineComServerInfo extends ComServerInfo<ComServer.ComServerBuilder, OfflineComServer> {

    public OfflineComServerInfo() {
    }

    public OfflineComServerInfo(OfflineComServer comServer) {
        super(comServer);
        readFrom(comServer);
    }

    public OfflineComServerInfo(OfflineComServer comServer, List<ComPort> comPorts, EngineConfigurationService engineConfigurationService, ComPortInfoFactory comPortInfoFactory) {
        super(comServer, comPorts, engineConfigurationService, comPortInfoFactory);
        readFrom(comServer);
    }

    private void readFrom(OfflineComServer offlineComServer) {
        this.onlineComServerId = offlineComServer.getOnlineComServer() != null ? offlineComServer.getOnlineComServer().getId() : null;
    }

    @Override
    public ComServer.ComServerBuilder writeTo(ComServer.ComServerBuilder comServerBuilder, EngineConfigurationService engineConfigurationService) {
        super.writeTo(comServerBuilder, engineConfigurationService);
        Optional<Long> onlineComServerId = Optional.ofNullable(this.onlineComServerId);
        if (onlineComServerId.isPresent()) {
            Optional<? extends ComServer> onlineComServer = engineConfigurationService.findComServer(onlineComServerId.get());
            if (onlineComServer.isPresent() && OnlineComServer.class.isAssignableFrom(onlineComServer.get().getClass())) {
                ((OfflineComServer.OfflineComServerBuilder)comServerBuilder).onlineComServer((OnlineComServer) onlineComServer.get());
            }
        }
        return comServerBuilder;
    }

    @Override
    public OfflineComServer updateTo(OfflineComServer comServer, EngineConfigurationService engineConfigurationService) {
        Optional<Long> onlineComServerId = Optional.ofNullable(this.onlineComServerId);
        if (onlineComServerId.isPresent()) {
            Optional<? extends ComServer> onlineComServer = engineConfigurationService.findComServer(onlineComServerId.get());
            if (onlineComServer.isPresent() && OnlineComServer.class.isAssignableFrom(onlineComServer.get().getClass())) {
                comServer.setOnlineComServer((OnlineComServer) onlineComServer.get());
            }
        }
        super.updateTo(comServer, engineConfigurationService);
        return comServer;
    }

    @Override
    protected ComServer.ComServerBuilder createNew(EngineConfigurationService engineConfigurationService) {
        return engineConfigurationService.newOfflineComServerBuilder();
    }
}
