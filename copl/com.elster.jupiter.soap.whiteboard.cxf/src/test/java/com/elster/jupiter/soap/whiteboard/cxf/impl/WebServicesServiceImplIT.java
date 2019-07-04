/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.util.conditions.Where;

import javax.validation.ConstraintViolationException;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WebServicesServiceImplIT extends AbstractWebServiceIT {
    @Test
    @Transactional
    public void findWebServices() {
        assertThat(webServicesService.getWebServices()).isEmpty();
    }

    @Test
    @Transactional
    public void findEndPoints() {
        assertThat(endPointConfigurationService.findEndPointConfigurations().find()).isEmpty();
        assertThat(endPointConfigurationService.streamEndPointConfigurations().findAny()).isEmpty();
    }

    @Test
    @Transactional
    public void testCreateInboundEndpoint() {
        EndPointConfiguration endPointConfiguration = endPointConfigurationService.newInboundEndPointConfiguration("service", "webservice", "/srv")
                .setAuthenticationMethod(EndPointAuthentication.NONE).logLevel(LogLevel.SEVERE).create();
        assertThat(endPointConfigurationService.findEndPointConfigurations().find()).hasSize(1);
        assertThat(endPointConfigurationService.streamEndPointConfigurations().findAny()).isPresent();
        assertThat(endPointConfigurationService.streamEndPointConfigurations().filter(Where.where("logLevel").isEqualTo(LogLevel.INFO)).findAny()).isEmpty();
        assertThat(endPointConfigurationService.streamEndPointConfigurations().filter(Where.where("logLevel").isEqualTo(LogLevel.SEVERE)).findAny()).isPresent();
    }

    @Test
    @Transactional
    public void testCreateOutboundEndpoint() {
        EndPointConfiguration endPointConfiguration = endPointConfigurationService.newOutboundEndPointConfiguration("service", "webservice", "/srv")
                .setAuthenticationMethod(EndPointAuthentication.NONE).logLevel(LogLevel.INFO).create();
        assertThat(endPointConfigurationService.findEndPointConfigurations().find()).hasSize(1);
        assertThat(endPointConfigurationService.streamEndPointConfigurations().findAny()).isPresent();
        assertThat(endPointConfigurationService.streamEndPointConfigurations().filter(Where.where("logLevel").isEqualTo(LogLevel.SEVERE)).findAny()).isEmpty();
        assertThat(endPointConfigurationService.streamEndPointConfigurations().filter(Where.where("logLevel").isEqualTo(LogLevel.INFO)).findAny()).isPresent();
    }

    @Test
    @Transactional
    @Expected(ConstraintViolationException.class)
    public void testCreateOutboundEndpointMissingUserName() {
        EndPointConfiguration endPointConfiguration = endPointConfigurationService.newOutboundEndPointConfiguration("service", "webservice", "/srv")
                .setAuthenticationMethod(EndPointAuthentication.BASIC_AUTHENTICATION)
                .logLevel(LogLevel.INFO)
                .password("pass")
                .create();
    }

    @Test
    @Transactional
    @Expected(ConstraintViolationException.class)
    public void testCreateOutboundEndpointMissingPassword() {
        EndPointConfiguration endPointConfiguration = endPointConfigurationService.newOutboundEndPointConfiguration("service", "webservice", "/srv")
                .setAuthenticationMethod(EndPointAuthentication.BASIC_AUTHENTICATION).username("user").create();
    }
}
