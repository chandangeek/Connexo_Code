package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

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
 * Provides an implementation for the {@link DeviceLifeCycleInDeviceType} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-15 (08:47)
 */
public class DeviceLifeCycleInDeviceTypeImpl implements DeviceLifeCycleInDeviceType {

    private final UserService userService;

    @IsPresent
    private Reference<DeviceTypeImpl> deviceType = ValueReference.absent();
    @IsPresent
    private Reference<DeviceLifeCycle> deviceLifeCycle = ValueReference.absent();
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
    public DeviceLifeCycleInDeviceTypeImpl(UserService userService) {
        super();
        this.userService = userService;
    }

    DeviceLifeCycleInDeviceTypeImpl initialize(Interval interval, DeviceTypeImpl deviceType, DeviceLifeCycle deviceLifeCycle) {
        this.interval = interval;
        this.deviceType.set(deviceType);
        this.deviceLifeCycle.set(deviceLifeCycle);
        return this;
    }

    @Override
    public DeviceTypeImpl getDeviceType() {
        return deviceType.get();
    }

    @Override
    public DeviceLifeCycle getDeviceLifeCycle() {
        return deviceLifeCycle.get();
    }

    @Override
    public Interval getInterval() {
        return interval;
    }

    @Override
    public void close(Instant closingDate) {
        if (!isEffectiveAt(closingDate)) {
            throw new IllegalArgumentException();
        }
        this.interval = this.interval.withEnd(closingDate);
    }

    @Override
    public Optional<User> getUser() {
        return this.userService.findUser(this.userName);
    }

}