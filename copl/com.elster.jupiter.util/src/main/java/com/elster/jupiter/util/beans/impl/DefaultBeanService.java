/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.beans.impl;

import com.elster.jupiter.util.beans.BeanEvaluationException;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.NoSuchPropertyException;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultBeanService implements BeanService {

    private volatile Map<Class, BeanInfo> beanInfoCache = new ConcurrentHashMap<>();

    @Override
    public Class<?> getPropertyType(Class beanClass, String property) throws NoSuchPropertyException {
        return getter(beanClass, property).getReturnType();
    }

    @Override
    public Class<?> getPropertyType(Object bean, String property) throws NoSuchPropertyException {
        if (bean == null) {
            throw new IllegalArgumentException("Cannot evaluate null as a bean.");
        }
        return getter(bean, property).getReturnType();
    }

    @Override
    public Object get(Object bean, String property) {
        if (bean == null) {
            throw new IllegalArgumentException("Cannot evaluate null as a bean.");
        }
        return invokeGetter(bean, getter(bean, property));
    }

    @Override
    public void set(Object bean, String property, Object value) {
        if (bean == null) {
            throw new IllegalArgumentException("Cannot evaluate null as a bean.");
        }
        invokeSetter(bean, setter(bean, property), value);
    }

    private void invokeSetter(Object bean, Method setter, Object value) {
        try {
            setter.invoke(bean, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new NoSuchPropertyException(bean, setter.getName(), e);
        }
    }

    private PropertyDescriptor descriptor(Object bean, String property) {
        BeanInfo info = getAllBeanInfo(bean);
        PropertyDescriptor descriptor = this.descriptor(info, property);
        if (descriptor == null) {
            throw new NoSuchPropertyException(bean, property);
        }
        return descriptor;
    }

    private PropertyDescriptor descriptor(Class beanClass, String property) {
        BeanInfo info = getAllBeanInfo(beanClass);
        PropertyDescriptor descriptor = this.descriptor(info, property);
        if (descriptor == null) {
            if (!beanClass.isInterface()) {
                Class superclass = beanClass.getSuperclass();
                if (superclass != null) {
                    return this.descriptor(superclass, property);
                }
            }
            // Try the interfaces
            for (Class interfaze : beanClass.getInterfaces()) {
                try {
                    return this.descriptor(interfaze, property);
                }
                catch (NoSuchPropertyException e) {
                    // Try the next interface
                }
            }
            // None of the superclasses or interface has the property
            throw new NoSuchPropertyException(beanClass, property);
        }
        return descriptor;
    }

    private PropertyDescriptor descriptor(BeanInfo info, String property) {
        PropertyDescriptor descriptor = null;
        for (PropertyDescriptor candidate : info.getPropertyDescriptors()) {
            if (candidate.getName().equals(property)) {
                descriptor = candidate;
            }
        }
        return descriptor;
    }

    private BeanInfo getAllBeanInfo(Object bean) {
        return beanInfoCache.computeIfAbsent(bean.getClass(), clazz -> {
            try {
                return Introspector.getBeanInfo(bean.getClass(), Object.class);
            } catch (IntrospectionException e) {
                throw new BeanEvaluationException(bean, e);
            }
        });
    }

    private BeanInfo getAllBeanInfo(Class beanClass) {
        return beanInfoCache.computeIfAbsent(beanClass, clazz -> {
            try {
                return Introspector.getBeanInfo(beanClass);
            } catch (IntrospectionException e) {
                throw new BeanEvaluationException(beanClass, e);
            }
        });
    }

    private Method getter(Object bean, String property) {
        return descriptor(bean, property).getReadMethod();
    }

    private Method getter(Class beanClass, String property) {
        return descriptor(beanClass, property).getReadMethod();
    }

    private Object invokeGetter(Object bean, Method getter) {
        try {
            return getter.invoke(bean);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new NoSuchPropertyException(bean, getter.getName(), e);
        }
    }

    private Method setter(Object bean, String property) {
        return descriptor(bean, property).getWriteMethod();
    }

}
