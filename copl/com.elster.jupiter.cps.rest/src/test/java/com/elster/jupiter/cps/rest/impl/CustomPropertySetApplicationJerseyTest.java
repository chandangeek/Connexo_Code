package com.elster.jupiter.cps.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import org.mockito.Mock;

import javax.ws.rs.core.Application;
import java.time.Clock;

public class CustomPropertySetApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    CustomPropertySetService customPropertySetService;
    @Mock
    Clock clock;

    @Override
    protected Application getApplication() {
        CustomPropertySetApplication application = new CustomPropertySetApplication();
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setCustomPropertySetService(customPropertySetService);
        application.setClock(clock);
        return application;
    }
}