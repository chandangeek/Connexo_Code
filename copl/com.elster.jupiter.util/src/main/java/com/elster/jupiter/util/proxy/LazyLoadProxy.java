/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * LazyLoadProxy is intended to create on the fly proxies for an interface, which will delay loading the actual object for that interface until a method is called on it.
 * The way to create such a proxy is to provide a LazyLoader to the newInstance factory method.
 * The typical use is to avoid the potentially heavy cost of loading the object, when it was not necessary to load it.
 *
 * @author Tom De Greyt (tgr)
 */
public final class LazyLoadProxy<T> implements java.lang.reflect.InvocationHandler {

    private LazyLoader<T> loader;
    private T obj;

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(LazyLoader<T> initializer) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(
            initializer.getClassLoader(),
            new Class[] {initializer.getImplementedInterface()},
            new LazyLoadProxy<>(initializer));
    }

    private LazyLoadProxy(LazyLoader<T> loader) {
    	this.loader = loader;
    }

    public Object invoke(Object proxy, Method m, Object[] args)
            throws Throwable {
        try {
            if (obj == null) {
                obj = loader.load();
            }
            return m.invoke(obj, args);
        }
        catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    public static <S> S unwrap(Object object) {
        LazyLoadProxy<S> lazyLoadProxy = (LazyLoadProxy<S>) Proxy.getInvocationHandler(object);
        return lazyLoadProxy.getValue();
    }

    public static boolean isLazyLoadProxy(Object object) {
        return Proxy.isProxyClass(object.getClass()) && Proxy.getInvocationHandler(object) instanceof LazyLoadProxy;
    }

    private T getValue() {
        if (obj == null) {
            obj = loader.load();
        }
        return obj;
    }
}