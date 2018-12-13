/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Optional;

public class ResourceHelper {
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final ProtocolPluggableService protocolPluggableService;

    @Inject
    public ResourceHelper(ConcurrentModificationExceptionFactory conflictFactory, ProtocolPluggableService protocolPluggableService) {
        this.conflictFactory = conflictFactory;
        this.protocolPluggableService = protocolPluggableService;
    }

    public DeviceProtocolPluggableClass findDeviceProtocolPluggableClassByMrIdOrThrowException(long id) {
        return protocolPluggableService.findDeviceProtocolPluggableClass(id)
                .orElseThrow(() -> new WebApplicationException("No DeviceProtocolPluggableClass with id " + id, Response.Status.NOT_FOUND));
    }

    public Long getCurrentDeviceProtocolPluggableClassVersion(long id) {
        return protocolPluggableService.findDeviceProtocolPluggableClass(id).map(DeviceProtocolPluggableClass::getEntityVersion).orElse(null);
    }

    public Optional<DeviceProtocolPluggableClass> getLockedDeviceProtocolPluggableClass(long id, long version) {
        return protocolPluggableService.findAndLockDeviceProtocolPluggableClassByIdAndVersion(id, version);
    }

    public DeviceProtocolPluggableClass lockDeviceProtocolPluggableClassOrThrowException(DeviceCommunicationProtocolInfo info) {
        return getLockedDeviceProtocolPluggableClass(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentDeviceProtocolPluggableClassVersion(info.id))
                        .supplier());
    }

    public InboundDeviceProtocolPluggableClass findInboundDeviceProtocolPluggableClassOrThrowException(long id) {
        return this.protocolPluggableService.findInboundDeviceProtocolPluggableClass(id)
                .orElseThrow(() -> new WebApplicationException("No InboundDeviceProtocolPluggableClass with id " + id, Response.Status.NOT_FOUND));
    }

    public Long getCurrentInboundDeviceProtocolPluggableClassVersion(long id) {
        return protocolPluggableService.findInboundDeviceProtocolPluggableClass(id).map(InboundDeviceProtocolPluggableClass::getEntityVersion).orElse(null);
    }

    public Optional<InboundDeviceProtocolPluggableClass> getLockedInboundDeviceProtocolPluggableClass(long id, long version) {
        return protocolPluggableService.findAndLockInboundDeviceProtocolPluggableClassByIdAndVersion(id, version);
    }

    public InboundDeviceProtocolPluggableClass lockInboundDeviceProtocolPluggableClassOrThrowException(DeviceDiscoveryProtocolInfo info) {
        return getLockedInboundDeviceProtocolPluggableClass(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentInboundDeviceProtocolPluggableClassVersion(info.id))
                        .supplier());
    }
}
