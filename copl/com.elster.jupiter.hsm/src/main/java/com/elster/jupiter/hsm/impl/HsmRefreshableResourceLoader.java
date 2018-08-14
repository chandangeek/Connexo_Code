package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.model.HsmBaseException;

public interface HsmRefreshableResourceLoader<T>  {

    T load() throws HsmBaseException;

    Long timeStamp();

}
