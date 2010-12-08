package com.energyict.genericprotocolimpl.elster.AM100R.Apollo;

import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Simple utils class to help the ProfileBuilder
 * <br/>
 * Copyrights EnergyICT<br/>
 * Date: 1-dec-2010<br/>
 * Time: 13:42:36<br/>
 */
public class ProfileUtils {

    /**
     * Checks if a {@link com.energyict.dlms.cosem.CapturedObject} is a valid Energy Channel.
     * The check is based of the type of the ClassId(3, 4, 5) and the AttributeIndex (2)
     *
     * @param co the capturedObject to test
     * @return true or false
     */
    public static boolean isChannelData(CapturedObject co) {
        return (((co.getClassId() == DLMSClassId.REGISTER.getClassId())
                || (co.getClassId() == DLMSClassId.EXTENDED_REGISTER.getClassId())
                || (co.getClassId() == DLMSClassId.DEMAND_REGISTER.getClassId()))
                && co.getAttributeIndex() == 2);
    }
}
