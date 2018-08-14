package com.elster.jupiter.hsm.impl.resources;

import com.elster.jupiter.hsm.model.HsmBaseException;

public interface HsmRefreshableResourceBuilder<T>  {

    T build() throws HsmBaseException;

    Long timeStamp();

}
