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
import javax.validation.ValidatorFactory;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private javax.validation.Validator validator;


    @Before
    public void setUp() {
    	Provider<UsagePointImpl> usagePointFactory = new Provider<UsagePointImpl>() {
			@Override
			public UsagePointImpl get() {
				return new UsagePointImpl(dataModel, eventService, meterActivationFactory, accountabilityFactory);
			}
    	};
        serviceCategory = new ServiceCategoryImpl(dataModel,usagePointFactory).init(ServiceKind.ELECTRICITY);
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(validator.validate(any(), anyVararg())).thenReturn(Collections.emptySet());
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
        when(dataModel.getInstance(UsagePointImpl.class)).thenReturn(new UsagePointImpl(dataModel, eventService, () -> null, () -> null));

        UsagePoint usagePoint = serviceCategory.newUsagePoint("mrId").create();
        assertThat(usagePoint).isInstanceOf(UsagePointImpl.class);


    }

}
