package com.energyict.mdc.upl.crypto;

public interface DataAndAuthenticationTag {

    public byte[] getData();

    public byte[] getAuthenticationTag();
}
