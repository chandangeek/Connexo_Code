package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ServiceCategoryImplTest {

    private ServiceCategoryImpl serviceCategory;

    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private Provider<MeterActivationImpl> meterActivationFactory;
    @Mock
    private Provider<UsagePointAccountabilityImpl> accountabilityFactory;
    

    @Before
    public void setUp() {
    	Provider<UsagePointImpl> usagePointFactory = new Provider<UsagePointImpl>() {
			@Override
			public UsagePointImpl get() {
				return new UsagePointImpl(dataModel, eventService, meterActivationFactory, accountabilityFactory);
			}
    	};
        serviceCategory = new ServiceCategoryImpl(dataModel,usagePointFactory).init(ServiceKind.ELECTRICITY);
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
        verify(dataModel).persist(serviceCategory);
    }

    @Test
    public void testNewUsagePoint() {
        UsagePoint usagePoint = serviceCategory.newUsagePoint("mrId").create();
        assertThat(usagePoint).isInstanceOf(UsagePointImpl.class);


    }

}
