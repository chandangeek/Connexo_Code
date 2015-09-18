package com.elster.jupiter.prepayment.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.device.data.DeviceService;
import org.mockito.Mock;

import javax.ws.rs.core.Application;
import java.time.Clock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

public class MultisensePrepaymentApiJerseyTest extends FelixRestApplicationJerseyTest {
    @Mock
    DeviceService deviceService;
    @Mock
    Clock clock;
    @Mock
    MeteringService meteringService;

    @Override
    protected Application getApplication() {
        PrepaymentApplication application = new PrepaymentApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setDeviceService(deviceService);
        application.setMeteringService(meteringService);
        application.setClock(clock);
        return application;
    }

}