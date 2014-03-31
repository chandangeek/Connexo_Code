package com.energyict.mdc.dynamic.impl;

import com.energyict.mdc.common.Environment;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Serves as the root for all test classes that will
 * focus on testing {@link com.energyict.mdc.dynamic.PropertySpec}
 * implementation classes.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-06 (16:16)
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractPropertySpecTest {

    static final int TEST_BUSINESS_OBJECT_ID = 97;

    @Mock
    private Environment environment;

    @Before
    public void setEnvironment () {
        Environment.DEFAULT.set(this.environment);
        when(this.environment.getErrorMsg(anyString())).thenReturn("MR");
    }

    @After
    public void cleanEnvironment () {
        Environment.DEFAULT.set(null);
    }

    protected TestBusinessObject newPersistentTestBusinessObject() {
        TestBusinessObject testBusinessObject = mock(TestBusinessObject.class);
        when(testBusinessObject.getId()).thenReturn(TEST_BUSINESS_OBJECT_ID);
        return testBusinessObject;
    }

}