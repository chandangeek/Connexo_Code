/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm;

import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties;

public class KaifaProperties extends Dsmr40Properties {

    @Override
    @ProtocolProperty
    public boolean isBulkRequest() {
        return getBooleanProperty(BULK_REQUEST, "0");   //Don't use get-with-list by default
    }

}
