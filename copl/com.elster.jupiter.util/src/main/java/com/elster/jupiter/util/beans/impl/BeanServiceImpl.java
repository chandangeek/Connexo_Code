package com.elster.jupiter.util.beans.impl;

import com.elster.jupiter.util.beans.BeanEvaluationException;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.NoSuchPropertyException;
import org.osgi.service.component.annotations.Component;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Component(name = "com.elster.jupiter.time.beans", service = { BeanService.class })
public class BeanServiceImpl implements BeanService {
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
        PropertyDescriptor descriptor = null;
        BeanInfo info = getAllBeanInfo(bean);
        for (PropertyDescriptor candidate : info.getPropertyDescriptors()) {
            if (candidate.getName().equals(property)) {
                descriptor = candidate;
            }
        }
        if (descriptor == null) {
            throw new NoSuchPropertyException(bean, property);
        }
        return descriptor;
    }

    private BeanInfo getAllBeanInfo(Object bean) {
        try {
            return Introspector.getBeanInfo(bean.getClass(), Object.class);
        } catch (IntrospectionException e) {
            throw new BeanEvaluationException(bean, e);
        }
    }

    private Method getter(Object bean, String property) {
        return descriptor(bean, property).getReadMethod();
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
