package com.energyict.mdc.upl.crypto;

public interface KeyRenewalMBusResponse {

    byte[] getSmartMeterKey();

    byte[] getAuthenticationTag();

    byte[] getMbusDeviceKey();

    IrreversibleKey getMdmSmWK();

    byte[] getMBusAuthTag();
}
