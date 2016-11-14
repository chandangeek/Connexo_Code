package com.energyict.mdc.upl.cache;

import java.io.Serializable;

public interface Stub extends Serializable {

    Object getServerImplementation();

    Object getClientImplementation();

    String getClassName();

}