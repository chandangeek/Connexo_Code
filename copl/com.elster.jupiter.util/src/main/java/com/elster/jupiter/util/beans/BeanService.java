package com.elster.jupiter.util.beans;

/**
 * The BeanService is responsible for interacting dynamically with beans. Specifically it can get and set properties of a given bean.
 */
public interface BeanService {

    /**
     * @return the value of the given property on the given bean.
     */
    Object get(Object bean, String property) throws NoSuchPropertyException;

    /**
     * Sets the given value as the value of the given property on the given bean.
     */
    void set(Object bean, String property, Object value) throws NoSuchPropertyException;

}
