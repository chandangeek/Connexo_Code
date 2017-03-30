/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceLifeCycleChangeEvent;
import com.energyict.mdc.device.data.DeviceLifeCycleChangeEvent.Type;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import javax.inject.Inject;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeviceLifeCycleHistoryInfoFactory {

    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    @Inject
    public DeviceLifeCycleHistoryInfoFactory(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    public DeviceLifeCycleChangeInfos createDeviceLifeCycleChangeInfos(Device device) {
        DeviceLifeCycleChangeInfos infos = new DeviceLifeCycleChangeInfos();
        Optional<State> previousState = Optional.empty();
        Optional<DeviceLifeCycle> previousLifeCycle = device.getDeviceType().getDeviceLifeCycle(device.getCreateTime());
        for (DeviceLifeCycleChangeEvent event : device.getDeviceLifeCycleChangeEvents()) {
            switch (event.getType()) {
            case STATE:
                infos.deviceLifeCycleChanges.add(createStateChangeInfo(event, previousState));
                previousState = Optional.of(event.getState());
                break;
            case LIFE_CYCLE:
                infos.deviceLifeCycleChanges.add(createDeviceLifeCycleChangeInfo(event, previousLifeCycle));
                previousLifeCycle = Optional.of(event.getDeviceLifeCycle());
                break;
            default:
                break;
            }
        }
        infos.total = infos.deviceLifeCycleChanges.size();
        return infos;
    }

    private DeviceLifeCycleChangeEventInfo createDeviceLifeCycleChangeInfo(DeviceLifeCycleChangeEvent event, Optional<DeviceLifeCycle> lastLifeCycle) {
        DeviceLifeCycleChangeEventInfo info = createChangeEventInfo(event);
        lastLifeCycle.ifPresent(lc -> {
            info.from = new IdWithNameInfo(lc.getId(), lc.getName());
        });
        info.to = new IdWithNameInfo(event.getDeviceLifeCycle().getId(), event.getDeviceLifeCycle().getName());
        return info;
    }

    private DeviceLifeCycleChangeEventInfo createStateChangeInfo(DeviceLifeCycleChangeEvent event, Optional<State> lastState) {
        DeviceLifeCycleChangeEventInfo info = createChangeEventInfo(event);
        lastState.ifPresent(state -> {
            info.from = new IdWithNameInfo(state.getId(), getStateName(state));
        });
        info.to = new IdWithNameInfo(event.getState().getId(), getStateName(event.getState()));
        return info;
    }

    private DeviceLifeCycleChangeEventInfo createChangeEventInfo(DeviceLifeCycleChangeEvent event) {
        DeviceLifeCycleChangeEventInfo info = new DeviceLifeCycleChangeEventInfo();
        info.type = event.getType();
        info.modTime = event.getTimestamp();
        event.getUser().ifPresent(user -> {
            info.author = new IdWithNameInfo(user.getId(), user.getName());
        });
        return info;
    }

    private String getStateName(State state) {
        return DefaultState
                .from(state)
                .map(deviceLifeCycleConfigurationService::getDisplayName)
                .orElseGet(state::getName);
    }

    static class DeviceLifeCycleChangeInfos {
        public int total = 0;
        public List<DeviceLifeCycleChangeEventInfo> deviceLifeCycleChanges = new ArrayList<>();
    }

    static class DeviceLifeCycleChangeEventInfo {
        @XmlJavaTypeAdapter(ChangeEventAdapter.class)
        public DeviceLifeCycleChangeEvent.Type type;
        public IdWithNameInfo from;
        public IdWithNameInfo to;
        public IdWithNameInfo author;
        public Instant modTime;
    }

    static class ChangeEventAdapter extends MapBasedXmlAdapter<DeviceLifeCycleChangeEvent.Type> {
        public ChangeEventAdapter() {
            register("lifeCycle", Type.LIFE_CYCLE);
            register("state", Type.STATE);
        }
    }
}
