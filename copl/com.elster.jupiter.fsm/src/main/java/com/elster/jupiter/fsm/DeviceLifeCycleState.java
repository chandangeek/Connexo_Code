package com.elster.jupiter.fsm;

/**
 * Models a {@link State} that is part of the default device life cycle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (15:10)
 */
public interface DeviceLifeCycleState extends State {

    public DefaultDeviceLifeCycleState getDefaultState();

}