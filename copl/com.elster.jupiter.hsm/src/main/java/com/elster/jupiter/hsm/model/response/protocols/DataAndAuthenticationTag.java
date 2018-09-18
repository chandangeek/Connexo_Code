package com.elster.jupiter.hsm.model.response.protocols;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface DataAndAuthenticationTag {

    byte[] getData();

    byte[] getAuthenticationTag();
}
