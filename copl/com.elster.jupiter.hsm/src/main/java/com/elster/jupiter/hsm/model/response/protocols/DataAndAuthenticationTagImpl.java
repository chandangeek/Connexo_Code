package com.elster.jupiter.hsm.model.response.protocols;

import com.energyict.mdc.upl.crypto.DataAndAuthenticationTag;

public class DataAndAuthenticationTagImpl implements DataAndAuthenticationTag {

    private byte[] data;

    private byte[] authenticationTag;

    public DataAndAuthenticationTagImpl(byte[] data, byte[] authenticationTag) {
        this.data = data;
        this.authenticationTag = authenticationTag;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getAuthenticationTag() {
        return authenticationTag;
    }

}
