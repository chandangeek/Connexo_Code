package com.elster.jupiter.metering.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Interval;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;

/**
 * Provides an implementation for the {@link EndDeviceLifeCycleStatus} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-13 (16:53)
 */
public class EndDeviceLifeCycleStatusImpl implements EndDeviceLifeCycleStatus {

    private final UserService userService;

    @IsPresent
    private Reference<EndDevice> endDevice = ValueReference.absent();
    @IsPresent
    private Reference<State> state = ValueReference.absent();
    private Interval interval;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    public EndDeviceLifeCycleStatusImpl(UserService userService) {
        super();
        this.userService = userService;
    }

    EndDeviceLifeCycleStatusImpl initialize(Interval interval, EndDevice endDevice, State state) {
        this.endDevice.set(endDevice);
        this.state.set(state);
        this.interval = interval;
        return this;
    }

    @Override
    public EndDevice getEndDevice() {
        return this.endDevice.get();
    }

    @Override
    public State getState() {
        return this.state.get();
    }

    @Override
    public Optional<User> getUser() {
        return this.userService.findUser(this.userName);
    }

    @Override
    public Interval getInterval() {
        return this.interval;
    }

    @Override
    public void close(Instant closingDate) {
        if (!isEffectiveAt(closingDate)) {
            throw new IllegalArgumentException();
        }
        this.interval = this.interval.withEnd(closingDate);
    }

}