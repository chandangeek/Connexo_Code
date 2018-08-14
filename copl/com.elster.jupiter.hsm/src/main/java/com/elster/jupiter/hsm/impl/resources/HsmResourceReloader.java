package com.elster.jupiter.hsm.impl.resources;

import com.elster.jupiter.hsm.impl.resources.HsmRefreshableResourceBuilder;
import com.elster.jupiter.hsm.model.HsmBaseException;

import java.util.Objects;

public final class HsmResourceReloader<T> {

    private HsmRefreshableResourceBuilder<T> loader;
    private T reloadAbleInstance;
    private Long loadTime;

    public HsmResourceReloader(HsmRefreshableResourceBuilder<T> loader) throws HsmBaseException {
        if (Objects.isNull(loader)){
            throw new HsmBaseException("Could not instantiate resource re-loader based on null resource loader");
        }
        this.loader = loader;
    }

    public T load() throws HsmBaseException {
        Long timeStamp = loader.timeStamp();
        if (loadTime == null || loadTime < timeStamp){
            reloadAbleInstance = loader.build();
            loadTime = timeStamp;
        }
        return reloadAbleInstance;
    }


}
