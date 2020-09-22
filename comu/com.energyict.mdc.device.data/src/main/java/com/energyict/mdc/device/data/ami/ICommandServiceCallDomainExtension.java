/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.ami;

import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.servicecall.ServiceCall;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface ICommandServiceCallDomainExtension extends PersistentDomainExtension<ServiceCall> {
    Instant getReleaseDate();

    void setReleaseDate(Instant releaseDate);

    List<Long> getDeviceMessageIds();
}
