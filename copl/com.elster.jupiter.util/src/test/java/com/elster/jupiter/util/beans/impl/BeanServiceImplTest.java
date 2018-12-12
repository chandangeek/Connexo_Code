/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.beans.impl;

import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.NoSuchPropertyException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BeanServiceImplTest {

    private static final String NAME = "name";
    private static final int AGE = 45;

    private BeanService beanService = new BeanServiceImpl();

    private Bean bean;

    @Before
    public void setUp() {
        bean = new Bean();
        bean.setName(NAME);
        bean.setAge(AGE);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetter() {
        assertThat(beanService.get(bean, "name")).isEqualTo(NAME);
    }

    @Test
    public void testSetter() {
        beanService.set(bean, "name", "Franky Avalon");

        assertThat(bean.getName()).isEqualTo("Franky Avalon");
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
