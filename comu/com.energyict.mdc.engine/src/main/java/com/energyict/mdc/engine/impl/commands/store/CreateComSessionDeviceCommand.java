package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.CanProvideDescriptionTitle;
import com.energyict.mdc.tasks.history.ComSessionBuilder;

/**
 * Models a {@link DeviceCommand} that will create a ComSession
 * that contains the details of a complete communication session.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-17 (15:43)
 */
public interface CreateComSessionDeviceCommand extends DeviceCommand, DeviceCommand.ExecutionLogger, CanProvideDescriptionTitle {

    public ComSessionBuilder getComSessionBuilder();

}