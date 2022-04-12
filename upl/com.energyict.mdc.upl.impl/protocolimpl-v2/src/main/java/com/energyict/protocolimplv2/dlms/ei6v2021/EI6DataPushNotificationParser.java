package com.energyict.protocolimplv2.dlms.ei6v2021;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.InboundDiscoveryContext;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocolimplv2.dlms.ei7.EI7DataPushNotificationParser;

public class EI6DataPushNotificationParser extends EI7DataPushNotificationParser {

    private static final Unit DEFAULT_LOAD_PROFILE_UNIT_SCALAR = Unit.get(BaseUnit.NORMALCUBICMETER, -3);

    public EI6DataPushNotificationParser(ComChannel comChannel, InboundDiscoveryContext context) {
        super(comChannel, context);
    }

    @Override
    public Unit getDefaultLoadProfileUnitScalar() {
        return DEFAULT_LOAD_PROFILE_UNIT_SCALAR;
    }
}
