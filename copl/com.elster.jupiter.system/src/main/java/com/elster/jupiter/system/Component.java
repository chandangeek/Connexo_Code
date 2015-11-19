package com.elster.jupiter.system;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface Component {

    String getSymbolicName();

    String getVersion();

    BundleType getBundleType();
}
