/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;

import javax.ws.rs.core.Application;
import java.time.Clock;

import org.mockito.Mock;

public class CustomPropertySetApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    CustomPropertySetService customPropertySetService;
    @Mock
    PropertyValueInfoService propertyValueInfoService;
    @Mock
    Clock clock;

    @Override
    protected Application getApplication() {
        CustomPropertySetApplication application = new CustomPropertySetApplication();
        application.setPropertyValueInfoService(propertyValueInfoService);
        application.setNlsService(nlsService);
        application.setTransactionService(transactionService);
        application.setCustomPropertySetService(customPropertySetService);
        application.setClock(clock);
        return application;
    }
}