/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.UtilitiesDeviceRegisteredNotification;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;

import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class SapApplicationJerseyTest extends FelixRestApplicationJerseyTest {
    @Mock
    protected DeviceService deviceService;

    @Mock
    protected SAPCustomPropertySets sapCustomPropertySets;

    @Mock
    protected EndPointConfigurationService endPointConfigurationService;

    @Mock
    protected UtilitiesDeviceRegisteredNotification utilitiesDeviceRegisteredNotification;

    @Mock
    protected Clock clock;

    @Override
    protected Application getApplication() {
        SapApplication application = new SapApplication();

        application.setNlsService(nlsService);
        when(nlsService.getThesaurus(SapApplication.COMPONENT_NAME, Layer.REST)).thenReturn(thesaurus);

        application.setDeviceService(deviceService);
        application.setSAPCustomPropertySets(sapCustomPropertySets);
        application.setEndPointConfigurationService(endPointConfigurationService);
        application.setUtilitiesDeviceRegisteredNotification(utilitiesDeviceRegisteredNotification);
        application.setClock(clock);
        application.setTransactionService(transactionService);

        return application;
    }

    void assertBadRequest(Response response, String message, String error) throws IOException {
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(model.<String>get("$.message")).isEqualTo(message);
        assertThat(model.<String>get("$.error")).isEqualTo(error);
    }
}
