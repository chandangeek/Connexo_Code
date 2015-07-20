package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;

import java.time.Clock;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateUsagePointTransactionTest {

    private static final String MR_ID = "mrId";
    private CreateUsagePointTransaction transaction;

    private UsagePointInfo info;

    @Mock
    private MeteringService meteringService;
    @Mock
    private ServiceCategory serviceCategory;
    @Mock
    private UsagePoint usagePoint;
    
    @Mock
	private Clock clock;
    
	private UsagePointBuilder usagePointBuilder;

    @Before
    public void setUp() {
        when(meteringService.getServiceCategory(ServiceKind.ELECTRICITY)).thenReturn(Optional.of(serviceCategory));
       

        info = new UsagePointInfo();
        info.serviceCategory = ServiceKind.ELECTRICITY;
        info.mRID = MR_ID;
        info.phaseCode = PhaseCode.A;

        transaction = new CreateUsagePointTransaction(info, meteringService, clock);
    }



    @After
    public void tearDown() {
    }

    @Test
    public void test() {
    	when(serviceCategory.newUsagePoint(MR_ID)).thenReturn(usagePoint);
    	when(serviceCategory.newUsagePointBuilder()).thenReturn(usagePointBuilder);
        when(usagePointBuilder.build()).thenReturn(usagePoint);
    	when(clock.instant()).thenReturn(Clock.systemDefaultZone().instant());
        
        
        UsagePoint result = transaction.perform();

        assertThat(result).isEqualTo(usagePoint);
    }

}
