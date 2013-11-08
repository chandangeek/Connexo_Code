package com.energyict.smartmeterprotocolimpl.nta.dsmr40.ibm;

import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties;

/**
 * Copyrights EnergyICT
 * Date: 12/03/13
 * Time: 14:48
 * Author: khe
 */
public class KaifaProperties extends Dsmr40Properties {

    @Override
    @ProtocolProperty
    public boolean isBulkRequest() {
        return getBooleanProperty(BULK_REQUEST, "0");   //Don't use get-with-list by default
    }

}
