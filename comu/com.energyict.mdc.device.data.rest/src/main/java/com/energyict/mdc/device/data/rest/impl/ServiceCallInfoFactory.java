package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.rest.ServiceCallInfo;

import javax.inject.Inject;


public class ServiceCallInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public ServiceCallInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public ServiceCallInfo summarized(ServiceCall serviceCall) {
        ServiceCallInfo serviceCallInfo = new ServiceCallInfo();
        serviceCallInfo.id = serviceCall.getId();
        serviceCallInfo.name = serviceCall.getNumber();
        serviceCallInfo.version = serviceCall.getVersion();
        serviceCallInfo.creationTime = serviceCall.getCreationTime().toEpochMilli();
        serviceCallInfo.lastModificationTime = serviceCall.getLastModificationTime().toEpochMilli();
        serviceCallInfo.state = toInfo(serviceCall.getState());
        serviceCallInfo.externalReference = serviceCall.getExternalReference()
                .isPresent() ? serviceCall.getExternalReference().get() : null;
        serviceCallInfo.type = serviceCall.getType().getName();
        return serviceCallInfo;
    }

    private IdWithDisplayValueInfo<String> toInfo(DefaultState state) {
        return new IdWithDisplayValueInfo<>(state.getKey(), state.getDisplayName(thesaurus));
    }
}
