/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;

public class EndDeviceEventTypeInfo {

    public String code;
    public IdWithNameInfo deviceType;
    public IdWithNameInfo domain;
    public IdWithNameInfo subDomain;
    public IdWithNameInfo eventOrAction;

    public static EndDeviceEventTypeInfo from(EndDeviceEventType eventType, Thesaurus thesaurus) {
        EndDeviceEventTypeInfo info = new EndDeviceEventTypeInfo();

        info.code = eventType.getMRID();

        EndDeviceType type = eventType.getType();
        info.deviceType = new IdWithNameInfo(type.getValue(), thesaurus.getString(type.toString(), type.getMnemonic()));

        EndDeviceDomain domain = eventType.getDomain();
        info.domain = new IdWithNameInfo(domain.getValue(), thesaurus.getString(domain.toString(), domain.getMnemonic()));

        EndDeviceSubDomain subDomain = eventType.getSubDomain();
        info.subDomain = new IdWithNameInfo(subDomain.getValue(), thesaurus.getString(subDomain.toString(), subDomain.getMnemonic()));

        EndDeviceEventOrAction eventOrAction = eventType.getEventOrAction();
        info.eventOrAction = new IdWithNameInfo(eventOrAction.getValue(), thesaurus.getString(eventOrAction.toString(), eventOrAction.getMnemonic()));

        return info;
    }
}
