package com.elster.jupiter.util.beans;

public interface BeanService {

    Object get(Object bean, String property);

    void set(Object bean, String property, Object value);

}
