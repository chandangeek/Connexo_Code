package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * Copyrights EnergyICT
 * Date: 20/07/11
 * Time: 17:59
 */
public interface DLMSAttributes {

    DLMSClassId getDlmsClassId();

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    int getShortName();

}
