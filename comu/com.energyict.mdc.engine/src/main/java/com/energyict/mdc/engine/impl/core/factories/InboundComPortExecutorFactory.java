package com.energyict.mdc.engine.impl.core.factories;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.InboundComPortExecutor;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.model.InboundComPort;

/**
 * Copyrights EnergyICT
 * Date: 23/10/12
 * Time: 9:43
 */
public interface InboundComPortExecutorFactory {

    /**
     * Creates an InboundComPortExecutor based on the given parameters.
     *
     *
     * @param inboundComPort        the used InboundComPort
     * @param comServerDAO          the used ComServerDAO
     * @param deviceCommandExecutor the used DeviceCommandExecutor
     * @param serviceProvider
     * @return the newly created InboundComPortExecutor
     */
    public InboundComPortExecutor create(InboundComPort inboundComPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider);

}
