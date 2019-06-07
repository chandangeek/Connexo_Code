package com.elster.jupiter.hsm.model.response.protocols;

import com.elster.jupiter.hsm.model.keys.HsmIrreversibleKey;

public interface KeyRenewalMBusResponse {

    byte[] getSmartMeterKey();

    byte[] getAuthenticationTag();

    byte[] getMbusDeviceKey();

    HsmIrreversibleKey getMdmSmWK();

    byte[] getMBusAuthTag();
}
