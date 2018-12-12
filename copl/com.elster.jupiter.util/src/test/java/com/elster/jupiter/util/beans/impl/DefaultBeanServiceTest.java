/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.beans.impl;

import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.NoSuchPropertyException;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultBeanServiceTest {

    private static final String NAME = "name";
    private static final int AGE = 45;

    private BeanService beanService = new DefaultBeanService();

    private Bean bean;

    @Before
    public void setUp() {
        bean = new Bean();
        bean.setName(NAME);
        bean.setAge(AGE);
    }

    @Test
    public void testPropertyTypeForClass() {
        // Business method
        Class<?> propertyType = beanService.getPropertyType(Bean.class, "name");

        // Asserts
        assertThat(propertyType).isEqualTo(String.class);
    }

    @Test
    public void testPropertyTypeForInterfaceHierarchy() {
        // Business method
        Class<?> propertyType = beanService.getPropertyType(HasThat.class, "this");

        // Asserts
        assertThat(propertyType).isEqualTo(String.class);
    }

    @Test
    public void testPropertyTypeForClassHierarchy() {
        // Business method
        Class<?> propertyType = beanService.getPropertyType(HasThisAndThat.class, "this");

        // Asserts
        assertThat(propertyType).isEqualTo(String.class);
    }

    @Test(expected = NoSuchPropertyException.class)
    public void testPropertyTypeForNonExistingPropertyOnClass() {
        // Business method
        beanService.getPropertyType(Bean.class, "doesNotExist");

        // Asserts: see expected exception rule
    }

    @Test
    public void testPropertyType() {
        // Business method
        Class<?> propertyType = beanService.getPropertyType(bean, "name");

        // Asserts
        assertThat(propertyType).isEqualTo(String.class);
    }

    @Test(expected = NoSuchPropertyException.class)
    public void testPropertyTypeForNonExistingProperty() {
        // Business method
        beanService.getPropertyType(bean, "doesNotExist");

        // Asserts: see expected exception rule
    }

    @Test
    public void testGetter() {
        assertThat(beanService.get(bean, "name")).isEqualTo(NAME);
    }

    @Test(expected = NoSuchPropertyException.class)
    public void testGetterForPropertyThatDoesNotExist() {
        // Business method
        beanService.get(bean, "doesNotExist");

        // Asserts: see expected exception rule
    }

    @Test
    public void testSetter() {
        // Business method
        beanService.set(bean, "name", "Franky Avalon");

        // Asserts
        assertThat(bean.getName()).isEqualTo("Franky Avalon");
    }

    @Test(expected = NoSuchPropertyException.class)
    public void testSetterForPropertyThatDoesNotExist() {
        // Business method
        beanService.set(bean, "doesNotExist", "Franky Avalon");

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullAsBeanIsIllegalArgumentForGet() {
        beanService.get(null, "name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullAsBeanIsIllegalArgumentForSet() {
        beanService.set(null, "name", "Bert");
    }

    @Test(expected = NoSuchPropertyException.class)
    public void testNoSuchPropertyExceptionOnPropertyNotFound() {
        beanService.get(bean, "ancestry");
    }

}