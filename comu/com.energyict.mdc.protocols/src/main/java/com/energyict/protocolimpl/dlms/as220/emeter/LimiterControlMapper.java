/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.as220.emeter;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.Limiter;
import com.energyict.dlms.cosem.attributes.LimiterAttributes;
import com.energyict.protocolimpl.base.AbstractDLMSAttributeMapper;
import com.energyict.protocolimpl.dlms.as220.AS220;

import java.io.IOException;

public class LimiterControlMapper extends AbstractDLMSAttributeMapper {

    private final AS220 as220;
    private Limiter limiter = null;

    public LimiterControlMapper(ObisCode baseObisCode, AS220 as220) {
        super(baseObisCode);
        this.as220 = as220;
    }

    @Override
    public int[] getSupportedAttributes() {
        return new int[]{
                LimiterAttributes.LOGICAL_NAME.getAttributeNumber(),
//                LimiterAttributes.MONITORED_VALUE.getAttributeNumber(),
//                LimiterAttributes.THRESHOLD_ACTIVE.getAttributeNumber(),
                LimiterAttributes.THRESHOLD_NORMAL.getAttributeNumber(),
//                LimiterAttributes.THRESHOLD_EMERGENCY.getAttributeNumber(),
                LimiterAttributes.MIN_OVER_THRESHOLD_DURATION.getAttributeNumber(),
//                LimiterAttributes.MIN_UNDER_THRESHOLD_DURATION.getAttributeNumber(),
//                LimiterAttributes.EMERGENCY_PROFILE.getAttributeNumber(),
//                LimiterAttributes.EMERGENCY_PROFILE_GROUP_ID_LIST.getAttributeNumber(),
//                LimiterAttributes.EMERGENCY_PROFILE_ACTIVE.getAttributeNumber(),
//                LimiterAttributes.ACTIONS.getAttributeNumber()
        };
    }

    @Override
    protected RegisterValue doGetAttributeValue(int attributeId) throws IOException {
        return getLimiter().asRegisterValue(attributeId);
    }

    @Override
    protected RegisterInfo doGetAttributeInfo(int attributeId) {
        LimiterAttributes attribute = LimiterAttributes.findByAttributeNumber(attributeId);
        if (attribute != null) {
            return new RegisterInfo("Limiter attribute " + attributeId + ": " + attribute);
        } else {
            return null;
        }
    }

    /**
     * Getter for the {@link AS220} protocol
     *
     * @return
     */
    public AS220 getAs220() {
        return as220;
    }

    /**
     * Getter for the used limiter object
     *
     * @return the used Limiter object
     */
    public Limiter getLimiter() throws IOException {
        if (this.limiter == null) {
            this.limiter = getAs220().getCosemObjectFactory().getLimiter();
        }
        return this.limiter;
    }
}
