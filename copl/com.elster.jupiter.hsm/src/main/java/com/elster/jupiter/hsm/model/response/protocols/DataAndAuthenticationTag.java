package com.elster.jupiter.hsm.model.response.protocols;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface DataAndAuthenticationTag {

    public byte[] getData();

    public byte[] getAuthenticationTag();
}
