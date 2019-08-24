package com.elster.jupiter.hsm.impl.loader;

import com.elster.jupiter.hsm.impl.resources.HsmReloadableResource;
import com.elster.jupiter.hsm.model.HsmBaseException;

import java.util.Objects;

public final class HsmResourceLoader<T> {

    private HsmReloadableResource<T> loader;
    private T reloadAbleInstance;
    private Long loadTime;

    HsmResourceLoader(HsmReloadableResource<T> loader) throws HsmBaseException {
        if (Objects.isNull(loader)){
            throw new HsmBaseException("Could not instantiate resource re-loader based on null resource loader");
        }
        this.loader = loader;
    }

    public T load() throws HsmBaseException {
        Long timeStamp = loader.timeStamp();
        if (loadTime == null){
            reloadAbleInstance = loader.load();
            loadTime = timeStamp;
        }
        else {
            if (loadTime < timeStamp) {
                reloadAbleInstance = loader.reload();
                loadTime = timeStamp;
            }
        }
        return reloadAbleInstance;
    }

}
