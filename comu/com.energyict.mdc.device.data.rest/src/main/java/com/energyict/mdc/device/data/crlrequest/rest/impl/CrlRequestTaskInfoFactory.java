package com.energyict.mdc.device.data.crlrequest.rest.impl;

import com.elster.jupiter.rest.util.LongIdWithNameInfo;
import com.energyict.mdc.device.configuration.rest.SecurityAccessorInfoFactory;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTask;
import com.energyict.mdc.device.data.crlrequest.rest.CrlRequestTaskInfo;

import javax.inject.Inject;

public class CrlRequestTaskInfoFactory {
    private final SecurityAccessorInfoFactory securityAccessorInfoFactory;

    @Inject
    public CrlRequestTaskInfoFactory(SecurityAccessorInfoFactory securityAccessorInfoFactory) {
        this.securityAccessorInfoFactory = securityAccessorInfoFactory;
    }

    public CrlRequestTaskInfo asInfo(CrlRequestTask crlRequestTask) {
        CrlRequestTaskInfo info = new CrlRequestTaskInfo();
        info.id = crlRequestTask.getId();
        info.deviceGroup = new LongIdWithNameInfo(crlRequestTask.getDeviceGroup().getId(), crlRequestTask.getDeviceGroup().getName());
        info.securityAccessor = securityAccessorInfoFactory.from(crlRequestTask.getSecurityAccessor());
        info.certificateAlias = crlRequestTask.getCertificate().getAlias();
        info.caName = crlRequestTask.getCaName();
        info.requestFrequency = crlRequestTask.getFrequency();
        return info;
    }
}
