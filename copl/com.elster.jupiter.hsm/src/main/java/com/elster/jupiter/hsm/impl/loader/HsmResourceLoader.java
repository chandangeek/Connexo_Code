package com.elster.jupiter.hsm.impl.loader;

import com.elster.jupiter.hsm.impl.resources.HsmRefreshableResourceBuilder;
import com.elster.jupiter.hsm.model.HsmBaseException;

import java.util.Objects;

public final class HsmResourceLoader<T> {

    private static HsmResourceLoader instance;

    private HsmRefreshableResourceBuilder<T> loader;
    private T reloadAbleInstance;
    private Long loadTime;

    private HsmResourceLoader(HsmRefreshableResourceBuilder<T> loader) throws HsmBaseException {
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

    public static  HsmResourceLoader getInstance(HsmRefreshableResourceBuilder newLoader) throws HsmBaseException {
        if (instance == null || !instance.loader.equals(newLoader)) {
            instance = new HsmResourceLoader(newLoader);
        }
        return instance;
    }



}
