/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OfflineComServer;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class OfflineComServerInfo extends ComServerInfo<ComServer.ComServerBuilder, OfflineComServer> {

    public OfflineComServerInfo() {
    }

    public OfflineComServerInfo(OfflineComServer comServer) {
        super(comServer);
    }

    public OfflineComServerInfo(OfflineComServer comServer, List<ComPort> comPorts, EngineConfigurationService engineConfigurationService, ComPortInfoFactory comPortInfoFactory) {
        super(comServer, comPorts, engineConfigurationService, comPortInfoFactory);
    }

    @Override
    public ComServer.ComServerBuilder writeTo(ComServer.ComServerBuilder comServerBuilder, EngineConfigurationService engineConfigurationService) {
        super.writeTo(comServerBuilder, engineConfigurationService);
        return comServerBuilder;
    }

    @Override
    public OfflineComServer updateTo(OfflineComServer comServer, EngineConfigurationService engineConfigurationService) {
        return super.updateTo(comServer, engineConfigurationService);
    }

    @Override
    protected ComServer.ComServerBuilder createNew(EngineConfigurationService engineConfigurationService) {
        return engineConfigurationService.newOfflineComServerBuilder();
    }
}
