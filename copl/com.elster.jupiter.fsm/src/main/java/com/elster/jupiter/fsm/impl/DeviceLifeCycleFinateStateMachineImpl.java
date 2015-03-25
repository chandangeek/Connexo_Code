package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (15:33)
 */
public class DeviceLifeCycleFinateStateMachineImpl extends FinateStateMachineImpl {

    @Inject
    public DeviceLifeCycleFinateStateMachineImpl(DataModel dataModel) {
        super(dataModel);
    }

}