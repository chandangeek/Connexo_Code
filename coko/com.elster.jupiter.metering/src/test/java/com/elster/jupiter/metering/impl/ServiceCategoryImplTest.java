package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceCategoryImplTest {

    private ServiceCategoryImpl serviceCategory;

    @Mock
    private DataMapper<ServiceCategory> serviceCategoryFactory;
    @Mock
    private DataModel dataModel;
    private EventService eventService;

    @Before
    public void setUp() {
        serviceCategory = new ServiceCategoryImpl(dataModel).init(ServiceKind.ELECTRICITY);

        when(dataModel.getInstance(UsagePointImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new UsagePointImpl(dataModel, eventService);
            }
        });
        when(dataModel.mapper(ServiceCategory.class)).thenReturn(serviceCategoryFactory);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreatingRemembersKind() {
        assertThat(serviceCategory.getKind()).isEqualTo(ServiceKind.ELECTRICITY);
    }

    @Test
    public void testGetAliasName() {
        String alias = "alias";
        serviceCategory.setAliasName(alias);

        assertThat(serviceCategory.getAliasName()).isEqualTo(alias);
    }

    @Test
    public void testGetDescription() {
        String description = "description";
        serviceCategory.setDescription(description);

        assertThat(serviceCategory.getDescription()).isEqualTo(description);
    }

    @Test
    public void testGetId() {
        assertThat(serviceCategory.getId()).isEqualTo(ServiceKind.ELECTRICITY.ordinal() + 1);
    }

    @Test
    public void testPersist() {
        serviceCategory.persist();

        verify(serviceCategoryFactory).persist(serviceCategory);
    }

    @Test
    public void testNewUsagePoint() {
        UsagePoint usagePoint = serviceCategory.newUsagePoint("mrId");
        assertThat(usagePoint).isInstanceOf(UsagePointImpl.class);


    }

}
