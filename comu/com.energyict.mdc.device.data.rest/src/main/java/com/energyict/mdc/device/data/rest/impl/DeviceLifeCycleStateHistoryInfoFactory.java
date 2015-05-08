package com.energyict.mdc.device.data.rest.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeSlice;
import com.elster.jupiter.fsm.StateTimeline;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

public class DeviceLifeCycleStateHistoryInfoFactory {

    private final Thesaurus thesaurus;

    @Inject
    public DeviceLifeCycleStateHistoryInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public DeviceLifeCycleStateChangeInfos asInfo(StateTimeline stateTimeline) {
        DeviceLifeCycleStateChangeInfos infos = new DeviceLifeCycleStateChangeInfos();
        List<StateTimeSlice> slices = stateTimeline.getSlices();
        if (slices.isEmpty()) {
            return infos;
        }
        StateTimeSlice fromState = null;
        StateTimeSlice toState = slices.get(0);
        infos.deviceLifeCycleStateChanges.add(createStateChangeInfo(fromState, toState));
        for (int i = 0; i < slices.size() - 1; i++) {
            fromState = slices.get(i);
            toState = slices.get(i + 1);
            infos.deviceLifeCycleStateChanges.add(createStateChangeInfo(fromState, toState));
        }
        infos.total = slices.size();
        return infos;
    }

    private DeviceLifeCycleStateChangeInfo createStateChangeInfo(StateTimeSlice fromState, StateTimeSlice toState) {
        DeviceLifeCycleStateChangeInfo info = new DeviceLifeCycleStateChangeInfo();
        info.fromState = fromState != null ? getStateName(fromState.getState()) : null;
        info.toState = getStateName(toState.getState());
        info.modTime = toState.getPeriod().lowerEndpoint();
        toState.getUser().ifPresent(user -> {
            info.author = new IdWithNameInfo(user.getId(), user.getName()); 
        });
        return info;
    }

    private String getStateName(State state) {
        Optional<DefaultState> defaultState = DefaultState.from(state);
        if (defaultState.isPresent()) {
            return thesaurus.getStringBeyondComponent(defaultState.get().getKey(), defaultState.get().getDefaultFormat());
        } else {
            return state.getName();
        }
    }

    static class DeviceLifeCycleStateChangeInfos {
        public int total = 0;
        public List<DeviceLifeCycleStateChangeInfo> deviceLifeCycleStateChanges = new ArrayList<>();
    }

    static class DeviceLifeCycleStateChangeInfo {
        public String fromState;
        public String toState;
        public IdWithNameInfo author;
        public Instant modTime;
    }
}
