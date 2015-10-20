package com.elster.jupiter.users.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.Resource;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;



@RunWith(MockitoJUnitRunner.class)
public class ResourceIT extends EqualsContractTest {

    private Resource resource;

    @Mock
    private DataModel dataModel;


    private static final String TEST_COMPONENT_NAME = "componentName";
    private static final String TEST_RESOURCE_DESCRIPTION= "resourceDescription";
    private static final String RESOURCE_NAME = "resourceName";
    private static final String OTHER_RESOURCE_NAME = "otherResourceName";
    private static final long ID = 0;
    private static final long OTHER_ID = 1;


    @Before
    public void equalsContractSetUp() {
        super.equalsContractSetUp();
    }


    @After
    public void tearDown() {
    }



    @Override
    protected Object getInstanceA() {
        if (resource == null) {
            resource =  ResourceImpl.from(dataModel, TEST_COMPONENT_NAME, RESOURCE_NAME, TEST_RESOURCE_DESCRIPTION);
        }
        return resource;
    }

    @Override
    protected Object getInstanceEqualToA() {
        Resource resourceB =  ResourceImpl.from(dataModel, TEST_COMPONENT_NAME, RESOURCE_NAME, TEST_RESOURCE_DESCRIPTION);
        return resourceB;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        Resource resourceC = ResourceImpl.from(dataModel, TEST_COMPONENT_NAME, OTHER_RESOURCE_NAME, TEST_RESOURCE_DESCRIPTION);
        return Collections.singletonList(resourceC);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

}


