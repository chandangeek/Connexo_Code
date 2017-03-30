/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import java.util.Collection;
import java.util.List;

/**
 * Represents a composition of {@link DeviceCommand}s.
 * Refer <a href="http://en.wikipedia.org/wiki/Composite_pattern">here</a>
 * for a detailed description of the Composite Design Pattern.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-22 (16:43)
 */
public interface CompositeDeviceCommand extends DeviceCommand {

    public void add (DeviceCommand command);

    public void add (CreateComSessionDeviceCommand command);

    public void add (PublishConnectionTaskEventDeviceCommand command);

    public void add (RescheduleExecutionDeviceCommand command);

    public void addAll (DeviceCommand... commands);

    public void addAll (Collection<DeviceCommand> commands);

    public List<DeviceCommand> getChildren ();

}