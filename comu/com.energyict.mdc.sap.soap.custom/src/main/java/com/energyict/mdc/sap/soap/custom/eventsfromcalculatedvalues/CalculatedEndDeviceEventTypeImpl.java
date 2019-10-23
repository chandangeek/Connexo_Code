/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.EndDeviceEventOrAction;
import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.metering.events.EndDeviceEventType;

public final class CalculatedEndDeviceEventTypeImpl implements EndDeviceEventType {

    private String name;
    private EndDeviceDomain domain;

    CalculatedEndDeviceEventTypeImpl(String name, EndDeviceDomain domain) {
        this.name = name;
        this.domain = domain;
    }

    @Override
    public EndDeviceType getType() {
        return EndDeviceType.NA;
    }

    @Override
    public EndDeviceDomain getDomain() {
        return domain;
    }

    @Override
    public EndDeviceSubDomain getSubDomain() {
        return EndDeviceSubDomain.CALCULATION;
    }

    @Override
    public EndDeviceEventOrAction getEventOrAction() {
        return EndDeviceEventOrAction.CALCULATED;
    }

    @Override
    public String getAliasName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getMRID() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }
}
