/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.custom;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.EventPushNotificationConfig;

import java.io.IOException;

public class PushEventNotificationSetupRegisterMapping extends CustomRegisterMapping {

    public PushEventNotificationSetupRegisterMapping(CosemObjectFactory cosemObjectFactory) {
        this.cosemObjectFactory = cosemObjectFactory;
    }

    public ObisCode getObisCode() {
        return EventPushNotificationConfig.getDefaultObisCode();
    }

    @Override
    public RegisterValue readRegister() throws IOException {
        return createAttributesOverview(
                getCosemObjectFactory().getEventPushNotificationConfig().getAttrbAbstractDataType(2),   //Push object list
                getCosemObjectFactory().getEventPushNotificationConfig().getAttrbAbstractDataType(3),   //Send destination & method
                getCosemObjectFactory().getEventPushNotificationConfig().getAttrbAbstractDataType(4),   //Communication window
                getCosemObjectFactory().getEventPushNotificationConfig().getAttrbAbstractDataType(5),   //Randomisation start interval
                getCosemObjectFactory().getEventPushNotificationConfig().getAttrbAbstractDataType(6),   //Number of retries
                getCosemObjectFactory().getEventPushNotificationConfig().getAttrbAbstractDataType(7)    //Repetition delay
        );
    }
}