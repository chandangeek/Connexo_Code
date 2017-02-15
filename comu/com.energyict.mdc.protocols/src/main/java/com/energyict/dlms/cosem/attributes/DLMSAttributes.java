/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.cosem.DLMSClassId;

public interface DLMSAttributes {

    /**
     * Getter for the ClassId for this object
     *
     * @return the DLMS ClassID
     */
    DLMSClassId getDlmsClassId();

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    int getShortName();

}
