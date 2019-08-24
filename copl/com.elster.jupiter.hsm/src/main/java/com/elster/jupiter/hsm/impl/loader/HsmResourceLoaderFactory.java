package com.elster.jupiter.hsm.impl.loader;

import com.elster.jupiter.hsm.impl.resources.HsmReloadableResource;
import com.elster.jupiter.hsm.model.HsmBaseException;

import java.util.HashMap;
import java.util.Map;

public class HsmResourceLoaderFactory {

    public static final Map<HsmReloadableResource, HsmResourceLoader> instances = new HashMap<>();

    public static <T> HsmResourceLoader<T> getInstance(HsmReloadableResource<T> reloadableResource) throws HsmBaseException {
        HsmResourceLoader<T> resourceLoader = instances.get(reloadableResource);
        if (resourceLoader == null) {
            resourceLoader = new HsmResourceLoader(reloadableResource);
            instances.put(reloadableResource, resourceLoader);
        }
        return resourceLoader;
    }

}
