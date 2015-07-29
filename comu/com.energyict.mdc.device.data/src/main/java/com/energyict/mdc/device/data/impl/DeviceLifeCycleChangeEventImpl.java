package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.DeviceLifeCycleChangeEvent;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeSlice;
import com.elster.jupiter.users.User;

import java.time.Instant;
import java.util.Optional;

/**
 * Provides an implementation for the {@link DeviceLifeCycleChangeEvent} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-15 (13:18)
 */
public class DeviceLifeCycleChangeEventImpl implements DeviceLifeCycleChangeEvent {

    private final Type type;
    private final Instant timestamp;
    private final Optional<DeviceLifeCycle> deviceLifeCycle;
    private final Optional<State> state;
    private final Optional<User> user;

    static DeviceLifeCycleChangeEventImpl from (StateTimeSlice stateTimeSlice) {
        return new DeviceLifeCycleChangeEventImpl(stateTimeSlice);
    }

    static DeviceLifeCycleChangeEventImpl from (com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent deviceLifeCycleChangeEvent) {
        return new DeviceLifeCycleChangeEventImpl(deviceLifeCycleChangeEvent);
    }

    private DeviceLifeCycleChangeEventImpl(StateTimeSlice stateTimeSlice) {
        this.type = Type.STATE;
        this.timestamp = stateTimeSlice.getPeriod().lowerEndpoint();
        this.state = Optional.of(stateTimeSlice.getState());
        this.deviceLifeCycle = Optional.empty();
        this.user = stateTimeSlice.getUser();
    }

    private DeviceLifeCycleChangeEventImpl(com.energyict.mdc.device.config.DeviceLifeCycleChangeEvent deviceLifeCycleChangeEvent) {
        this.type = Type.LIFE_CYCLE;
        this.timestamp = deviceLifeCycleChangeEvent.getTimestamp();
        this.state = Optional.empty();
        this.deviceLifeCycle = Optional.of(deviceLifeCycleChangeEvent.getDeviceLifeCycle());
        this.user = deviceLifeCycleChangeEvent.getUser();
    }

    @Override
    public Instant getTimestamp() {
        return this.timestamp;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public DeviceLifeCycle getDeviceLifeCycle() {
        return this.deviceLifeCycle.get();
    }

    @Override
    public State getState() {
        return this.state.get();
    }

    @Override
    public Optional<User> getUser() {
        return this.user;
    }

}