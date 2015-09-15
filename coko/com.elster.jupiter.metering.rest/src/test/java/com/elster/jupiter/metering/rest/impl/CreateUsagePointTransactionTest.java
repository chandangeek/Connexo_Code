package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

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
    private UsagePointBuilder usagePointBuilder;

    @Before
    public void setUp() {
        when(meteringService.getServiceCategory(ServiceKind.ELECTRICITY)).thenReturn(Optional.of(serviceCategory));

        usagePointBuilder = FakeBuilder.initBuilderStub(usagePoint, UsagePointBuilder.class);

        info = new UsagePointInfo();
        info.serviceCategory = ServiceKind.ELECTRICITY;
        info.mRID = MR_ID;
        info.phaseCode = PhaseCode.A;

        transaction = new CreateUsagePointTransaction(info, meteringService);
    }



    @After
    public void tearDown() {
    }

    @Test
    public void test() {
        when(serviceCategory.newUsagePoint(MR_ID)).thenReturn(usagePointBuilder);

        UsagePoint result = transaction.perform();

        assertThat(result).isEqualTo(usagePoint);
    }

}
