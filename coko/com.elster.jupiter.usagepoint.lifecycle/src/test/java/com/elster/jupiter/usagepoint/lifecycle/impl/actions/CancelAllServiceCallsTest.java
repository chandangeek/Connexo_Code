package com.elster.jupiter.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategory;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategoryTranslationKeys;
import com.elster.jupiter.usagepoint.lifecycle.impl.UsagePointLifeCycleServiceImpl;

import java.time.Instant;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Created by h241414 on 7/31/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class CancelAllServiceCallsTest {
    @Mock
    private ServiceCallService serviceCallService;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private State state;

    private CancelAllServiceCalls action;

    @Before
    public void initializeMocks() {
        action = new CancelAllServiceCalls(serviceCallService);
        action.setThesaurus(NlsModule.SimpleThesaurus.from(new UsagePointLifeCycleServiceImpl().getKeys()));
    }

    @Test
    public void testInfo() {
        assertThat(action.getKey()).isEqualTo(CancelAllServiceCalls.class.getSimpleName());
        assertThat(action.getName()).isEqualTo(MicroActionTranslationKeys.CANCEL_ALL_SERVICE_CALLS_NAME.getDefaultFormat());
        assertThat(action.getDescription()).isEqualTo(MicroActionTranslationKeys.CANCEL_ALL_SERVICE_CALLS_DESCRIPTION.getDefaultFormat());
    }

    @Test
    public void testCategory() {
        assertThat(action.getCategory()).isEqualTo(MicroCategory.MONITORING.name());
        assertThat(action.getCategoryName()).isEqualTo(MicroCategoryTranslationKeys.MONITORING_NAME.getDefaultFormat());
    }

    @Test
    public void testMandatory() {
       assertThat(action.isMandatoryForTransition(state,state)).isFalse();
    }
    private CancelAllServiceCalls getTestInstance()
    {
        return new CancelAllServiceCalls(this.serviceCallService);
    }

   @Test
    public void testdoExecute() {
        // create micro action CancelAllServiceCalls
        CancelAllServiceCalls microAction = this.getTestInstance();

       // Business method
       microAction.execute(this.usagePoint, Instant.now(), Collections.emptyMap());

        // Asserts
        verify(this.serviceCallService).cancelServiceCallsFor(this.usagePoint);
    }

}
