package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateUsagePointTransactionTest {

    private static final String MR_ID = "mrId";
    private CreateUsagePointTransaction transaction;

    private UsagePointInfo info;

    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private MeteringService meteringService;
    @Mock
    private ServiceCategory serviceCategory;

    @Before
    public void setUp() {
        when(serviceLocator.getMeteringService()).thenReturn(meteringService);
        when(meteringService.getServiceCategory(ServiceKind.ELECTRICITY)).thenReturn(Optional.of(serviceCategory));

        info = new UsagePointInfo();
        info.serviceCategory = ServiceKind.ELECTRICITY;
        info.mRID = MR_ID;

        transaction = new CreateUsagePointTransaction(info);

        Bus.setServiceLocator(serviceLocator);
    }



    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void test() {
        transaction.perform();

        verify(serviceCategory).newUsagePoint(MR_ID);
    }

}
