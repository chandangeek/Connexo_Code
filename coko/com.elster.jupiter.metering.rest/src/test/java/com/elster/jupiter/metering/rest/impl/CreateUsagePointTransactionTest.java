package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
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
import org.mockito.Matchers;
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
    @Mock
	private UsagePointBuilder usagePointBuilder;
    @Mock
	private ElectricityDetailBuilder edBuilder;
    @Mock
	private ElectricityDetail electricityDetail;

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
    	when(serviceCategory.getKind()).thenReturn(ServiceKind.ELECTRICITY);
    	
		when(usagePointBuilder.withAliasName(Matchers.anyString())).thenReturn(usagePointBuilder);
    	when(usagePointBuilder.withDescription(Matchers.anyString())).thenReturn(usagePointBuilder);
    	when(usagePointBuilder.withIsSdp(Matchers.anyBoolean())).thenReturn(usagePointBuilder);
    	when(usagePointBuilder.withIsVirtual(Matchers.anyBoolean())).thenReturn(usagePointBuilder);
    	when(usagePointBuilder.withMRID(Matchers.anyString())).thenReturn(usagePointBuilder);
    	when(usagePointBuilder.withName(Matchers.anyString())).thenReturn(usagePointBuilder);
    	when(usagePointBuilder.withOutageRegion(Matchers.anyString())).thenReturn(usagePointBuilder);
    	when(usagePointBuilder.withReadCycle(Matchers.anyString())).thenReturn(usagePointBuilder);
    	when(usagePointBuilder.withReadRoute(Matchers.anyString())).thenReturn(usagePointBuilder);
    	when(usagePointBuilder.withServicePriority(Matchers.anyString())).thenReturn(usagePointBuilder);
    	
        when(usagePointBuilder.build()).thenReturn(usagePoint);
        
        when(usagePoint.newElectricityDetailBuilder(Matchers.any())).thenReturn(edBuilder);
        
        when(edBuilder.withAmiBillingReady(Matchers.any())).thenReturn(edBuilder);
        when(edBuilder.withCheckBilling(Matchers.anyBoolean())).thenReturn(edBuilder);
        when(edBuilder.withConnectionState(Matchers.any())).thenReturn(edBuilder);
        when(edBuilder.withEstimatedLoad(Matchers.any())).thenReturn(edBuilder);
        when(edBuilder.withGrounded(Matchers.anyBoolean())).thenReturn(edBuilder);
        when(edBuilder.withMinimalUsageExpected(Matchers.anyBoolean())).thenReturn(edBuilder);
        when(edBuilder.withNominalServiceVoltage(Matchers.any())).thenReturn(edBuilder);
        when(edBuilder.withPhaseCode(Matchers.any())).thenReturn(edBuilder);
        when(edBuilder.withRatedCurrent(Matchers.any())).thenReturn(edBuilder);
        when(edBuilder.withRatedPower(Matchers.any())).thenReturn(edBuilder);
        when(edBuilder.withServiceDeliveryRemark(Matchers.anyString())).thenReturn(edBuilder);
        
        when(edBuilder.build()).thenReturn(electricityDetail);
        
        
        UsagePoint result = transaction.perform();

        assertThat(result).isEqualTo(usagePoint);
    }

}
