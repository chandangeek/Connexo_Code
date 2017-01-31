/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3;

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.protocols.mdc.services.impl.OrmClient;

import com.energyict.protocolimpl.dlms.g3.messaging.G3MessagingSagemCom;

import javax.inject.Inject;

public class SagemCom extends AS330D {

    @Override
    public String getProtocolDescription() {
        return "Sagemcom Linky DLMS (G3 Linky)";
    }

    @Inject
    public SagemCom(PropertySpecService propertySpecService, CalendarService calendarService, OrmClient ormClient) {
        super(propertySpecService, calendarService, ormClient);
    }

    @Override
    protected void initMessaging() {
        setMessaging(new G3MessagingSagemCom(getSession(), getProperties(), this.getCalendarService()));
    }

    protected G3Properties getProperties() {
        if (properties == null) {
            properties = new SagemComG3Properties();
        }
        return properties;
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }
}