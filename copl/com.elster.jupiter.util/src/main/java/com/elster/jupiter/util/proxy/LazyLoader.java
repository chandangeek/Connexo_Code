/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.proxy;

/**
 * Interface for classes that know how to load the Object for which a proxy is being used.
 * The proxy will request load() as soon as a method is called upon it.
 *
 * @author Tom De Greyt (tgr)
 */
public interface LazyLoader<T> {

    /**
     * Performs the loading of the object.
     *
     * @return the loaded Object
     */
    T load();

    /**
     * @return the interface class that the proxy should implement.
     */
    Class<T> getImplementedInterface();

    /**
     * @return the ClassLoader which should be used to create an instance of the lazy object
     */
    ClassLoader getClassLoader();
}
