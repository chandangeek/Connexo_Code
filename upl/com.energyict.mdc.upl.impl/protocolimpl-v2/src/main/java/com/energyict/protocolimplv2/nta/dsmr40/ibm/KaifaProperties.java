package com.energyict.protocolimplv2.nta.dsmr40.ibm;

import com.energyict.protocolimplv2.nta.dsmr40.Dsmr40Properties;

import static com.energyict.dlms.common.DlmsProtocolProperties.BULK_REQUEST;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_BULK_REQUEST;

public class KaifaProperties extends Dsmr40Properties {

    @Override
    public boolean isBulkRequest() {
        return getProperties().<Boolean>getTypedProperty(BULK_REQUEST, DEFAULT_BULK_REQUEST);
    }
}
