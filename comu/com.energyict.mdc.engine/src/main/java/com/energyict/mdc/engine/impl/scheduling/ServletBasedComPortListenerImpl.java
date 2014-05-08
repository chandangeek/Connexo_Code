package com.energyict.mdc.engine.impl.scheduling;

import com.energyict.comserver.commands.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServer;
import com.energyict.mdc.engine.model.InboundComPort;

import java.util.concurrent.ThreadFactory;

/**
 * Models ComPortListener functionality specifically for a
 * {@link EmbeddedWebServer WebServer}
 * based {@link com.energyict.mdc.engine.model.ComPort ComPort}
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/11/12
 * Time: 9:12
 */
public abstract class ServletBasedComPortListenerImpl extends ComPortListenerImpl{

    protected ServletBasedComPortListenerImpl(InboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor) {
        super(comPort, comServerDAO, deviceCommandExecutor);
    }

    protected ServletBasedComPortListenerImpl(InboundComPort comPort, ComServerDAO comServerDAO, ThreadFactory threadFactory, DeviceCommandExecutor deviceCommandExecutor) {
        super(comPort, comServerDAO, threadFactory, deviceCommandExecutor);
    }
}
