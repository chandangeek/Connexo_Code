package com.elster.jupiter.hsm.model.response.protocols;

import com.elster.jupiter.hsm.model.keys.HsmIrreversibleKey;

public class KeyRenewalMBusResponseImpl implements KeyRenewalMBusResponse {

    private final byte[] smartMeterKey;
    private final byte[] authenticationTag;
    private final byte[] mbusDeviceKey;
    private final HsmIrreversibleKey mdmSmWK;
    private final byte[] mBusAuthTag;

    public KeyRenewalMBusResponseImpl(byte[] smartMeterKey, byte[] authenticationTag, byte[] mbusDeviceKey, HsmIrreversibleKey mdmSmWK, byte[] mBusAuthTag) {
        this.smartMeterKey = smartMeterKey;
        this.authenticationTag = authenticationTag;
        this.mbusDeviceKey = mbusDeviceKey;
        this.mdmSmWK = mdmSmWK;
        this.mBusAuthTag = mBusAuthTag;
    }

    @Override
    public byte[] getSmartMeterKey() {
        return smartMeterKey;
    }

    @Override
    public byte[] getAuthenticationTag() {
        return authenticationTag;
    }

    @Override
    public byte[] getMbusDeviceKey() {
        return mbusDeviceKey;
    }

    @Override
    public HsmIrreversibleKey getMdmSmWK() {
        return mdmSmWK;
    }

    @Override
    public byte[] getMBusAuthTag() {
        return mBusAuthTag;
    }
}
