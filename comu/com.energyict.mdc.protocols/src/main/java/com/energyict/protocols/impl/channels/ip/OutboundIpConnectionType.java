/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.ip;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.protocols.impl.channels.ConnectionTypeImpl;

import java.math.BigDecimal;

public abstract class OutboundIpConnectionType extends ConnectionTypeImpl {

    static final TimeDuration DEFAULT_CONNECTION_TIMEOUT = TimeDuration.seconds(10);

    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    public OutboundIpConnectionType(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected String hostPropertyValue() {
        return (String) this.getProperty(OutboundIpConnectionProperties.Fields.HOST.propertySpecName());
    }

    protected int portNumberPropertyValue() {
        return intProperty((BigDecimal) getProperty(OutboundIpConnectionProperties.Fields.PORT_NUMBER.propertySpecName()));
    }

    protected int connectionTimeOutPropertyValue() {
        TimeDuration value = (TimeDuration) this.getProperty(OutboundIpConnectionProperties.Fields.CONNECTION_TIMEOUT.propertySpecName(), DEFAULT_CONNECTION_TIMEOUT);
        return this.intProperty(value);
    }

    protected int intProperty(BigDecimal value) {
        if (value == null) {
            return 0;
        }
        else {
            return value.intValue();
        }
    }

    protected int intProperty(TimeDuration value) {
        if (value == null) {
            return 0;
        }
        else {
            return (int) value.getMilliSeconds();
        }
    }

    @Override
    public Direction getDirection() {
        return Direction.OUTBOUND;
    }

}