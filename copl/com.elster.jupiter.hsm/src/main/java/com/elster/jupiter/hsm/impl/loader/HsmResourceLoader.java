package com.elster.jupiter.hsm.impl.loader;

import com.elster.jupiter.hsm.impl.resources.HsmReloadableResource;
import com.elster.jupiter.hsm.model.HsmBaseException;

import java.util.Objects;

public final class HsmResourceLoader<T> {

    private HsmReloadableResource<T> resource;
    private T loadedResource;
    private Long loadTime;

    HsmResourceLoader(HsmReloadableResource<T> resource) throws HsmBaseException {
        if (Objects.isNull(resource)){
            throw new HsmBaseException("Could not instantiate resource re-loader based on null resource loader");
        }
        this.resource = resource;
    }

    public T load() throws HsmBaseException {
        Long timeStamp = resource.timeStamp();
        if (loadTime == null){
            loadedResource = resource.load();
            loadTime = timeStamp;
        }
        else {
            if (loadTime < timeStamp || resource.changed()) {
                loadedResource = resource.reload();
                loadTime = timeStamp;
            }
        }
        return loadedResource;
    }

}
