package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.cache.TypeCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceCategoryImplTest {

    private ServiceCategoryImpl serviceCategory;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceLocator serviceLocator;
    @Mock
    private TypeCache<ServiceCategory> serviceCategoryFactory;

    @Before
    public void setUp() {
        serviceCategory = new ServiceCategoryImpl(ServiceKind.ELECTRICITY);

        when(serviceLocator.getOrmClient().getServiceCategoryFactory()).thenReturn(serviceCategoryFactory);

        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
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
