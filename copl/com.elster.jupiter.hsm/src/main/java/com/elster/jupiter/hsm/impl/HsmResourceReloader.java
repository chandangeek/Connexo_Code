package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.model.HsmBaseException;

import java.util.Objects;

public final class HsmResourceReloader<T> {

    private HsmRefreshableResourceLoader<T> loader;
    private T reloadAbleInstance;
    private Long loadTime;

    public HsmResourceReloader(HsmRefreshableResourceLoader<T> loader) throws HsmBaseException {
        if (Objects.isNull(loader)){
            throw new HsmBaseException("Could not instantiate resource re-loader based on null resource loader");
        }
        this.loader = loader;
    }

    public T load() throws HsmBaseException {
        Long timeStamp = loader.timeStamp();
        if (loadTime == null || loadTime < timeStamp){
            reloadAbleInstance = loader.load();
            loadTime = timeStamp;
        }
        return reloadAbleInstance;
    }


}
