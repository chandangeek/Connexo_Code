package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.ServiceKind;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ServiceCategoryImplTest {

    private ServiceCategoryImpl serviceCategory;

    @Before
    public void setUp() {
        serviceCategory = new ServiceCategoryImpl(ServiceKind.ELECTRICITY);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCreatingRemembersKind() {
        assertThat(serviceCategory.getKind()).isEqualTo(ServiceKind.ELECTRICITY);
    }

}
