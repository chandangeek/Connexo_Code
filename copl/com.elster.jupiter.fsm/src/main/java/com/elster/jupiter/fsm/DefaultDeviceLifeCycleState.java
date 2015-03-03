package com.elster.jupiter.fsm;

/**
 * Models the default states of a device life cycle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (15:11)
 */
public enum DefaultDeviceLifeCycleState {

    Initial, Instock, Commissioned, Active, Inactive, Decommissioned, Deleted;

}