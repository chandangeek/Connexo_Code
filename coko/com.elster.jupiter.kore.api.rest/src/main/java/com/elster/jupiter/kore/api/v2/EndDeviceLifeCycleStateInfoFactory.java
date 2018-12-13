/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeSlice;
import com.elster.jupiter.fsm.StateTimeline;
import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.util.HasName;

public class EndDeviceLifeCycleStateInfoFactory {
    public EndDeviceLifeCycleStateInfo asInfo(State state) {
        EndDeviceLifeCycleStateInfo info = new EndDeviceLifeCycleStateInfo();
        info.name = state.getName();
        info.stage = state.getStage().map(HasName::getName).orElse(null);
        return info;
    }

    public EndDeviceLifeCycleStateInfo asInfo(StateTimeSlice stateTimeSlice) {
        EndDeviceLifeCycleStateInfo info = new EndDeviceLifeCycleStateInfo();
        info.name = stateTimeSlice.getState().getName();
        info.interval = IntervalInfo.from(stateTimeSlice.getPeriod());
        info.stage = stateTimeSlice.getState().getStage().map(HasName::getName).orElse(null);
        return info;
    }
}
