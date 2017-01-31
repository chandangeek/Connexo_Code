/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.beans.impl;

import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.NoSuchPropertyException;

import org.osgi.service.component.annotations.Component;

/**
 * Component that implements the BeanService interface.
 */
@Component(name = "com.elster.jupiter.beans", service = { BeanService.class })
public class BeanServiceImpl implements BeanService {

    private final BeanService beanService = new DefaultBeanService();

    @Override
    public Class<?> getPropertyType(Class beanClass, String property) throws NoSuchPropertyException {
        return beanService.getPropertyType(beanClass, property);
    }

    @Override
    public Class<?> getPropertyType(Object bean, String property) throws NoSuchPropertyException {
        return beanService.getPropertyType(bean, property);
    }

    @Override
    public Object get(Object bean, String property) throws NoSuchPropertyException {
        return beanService.get(bean, property);
    }

    @Override
    public void set(Object bean, String property, Object value) throws NoSuchPropertyException {
        beanService.set(bean, property, value);
    }

}