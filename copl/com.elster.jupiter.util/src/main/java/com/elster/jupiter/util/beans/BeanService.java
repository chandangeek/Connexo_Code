/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.beans;

import aQute.bnd.annotation.ProviderType;

/**
 * The BeanService is responsible for interacting dynamically with beans. Specifically it can get and set properties of a given bean.
 */
@ProviderType
public interface BeanService {

    /**
     * Gets the type of the specified property on the specified bean class.
     *
     * @return The type of the specified property on the specified bean
     */
    Class<?> getPropertyType(Class beanClass, String property) throws NoSuchPropertyException;

    /**
     * Gets the type of the specified property on the specified bean.
     *
     * @return The type of the specified property on the specified bean
     */
    Class<?> getPropertyType(Object bean, String property) throws NoSuchPropertyException;

    /**
     * @return the value of the given property on the given bean.
     */
    Object get(Object bean, String property) throws NoSuchPropertyException;

    /**
     * Sets the given value as the value of the given property on the given bean.
     */
    void set(Object bean, String property, Object value) throws NoSuchPropertyException;

}
