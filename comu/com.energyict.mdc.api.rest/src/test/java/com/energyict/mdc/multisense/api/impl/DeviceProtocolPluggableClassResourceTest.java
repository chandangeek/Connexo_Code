/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.common.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.common.protocol.security.RequestSecurityLevel;
import com.energyict.mdc.common.protocol.security.ResponseSecurityLevel;
import com.energyict.mdc.common.protocol.security.SecuritySuite;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.upl.UPLConnectionFunction;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class DeviceProtocolPluggableClassResourceTest extends MultisensePublicApiJerseyTest {

    @Test
    public void testAllGetDeviceProtocolPluggableClasssPaged() throws Exception {
        SecuritySuite securitySuite = mockSecuritySuite(1);
        AuthenticationDeviceAccessLevel authAccessLevel = mockAuthenticationAccessLevel(2);
        EncryptionDeviceAccessLevel encAccessLevel = mockEncryptionAccessLevel(3);
        RequestSecurityLevel requestSecurityLevel = mockRequestSecurityDeviceAccessLevel(4);
        ResponseSecurityLevel responseSecurityLevel = mockResponseSecurityDeviceAccessLevel(5);
        UPLConnectionFunction connectionFunction_1 = mockUPLConnectionFunction(1, "CF_1");
        UPLConnectionFunction connectionFunction_2 = mockUPLConnectionFunction(2, "CF_2");
        UPLConnectionFunction connectionFunction_3 = mockUPLConnectionFunction(3, "CF_3");
        DeviceProtocolPluggableClass pluggableClass = mockPluggableClass(31L, "WebRTU", "1.9.2.3546",
                Collections.singletonList(authAccessLevel),
                Collections.singletonList(encAccessLevel),
                Collections.singletonList(securitySuite)
                , Collections.singletonList(requestSecurityLevel),
                Collections.singletonList(responseSecurityLevel),
                Arrays.asList(connectionFunction_1, connectionFunction_2),
                Arrays.asList(connectionFunction_2, connectionFunction_3));
        com.energyict.mdc.upl.properties.PropertySpec clientPropertySpec = ConnexoToUPLPropertSpecAdapter.adaptTo(mockBigDecimalPropertySpec());
        Optional<com.energyict.mdc.upl.properties.PropertySpec> optionalClientPropertySpec = Optional.of(clientPropertySpec);
        when(pluggableClass.getDeviceProtocol().getClientSecurityPropertySpec()).thenReturn(optionalClientPropertySpec);
        Finder<DeviceProtocolPluggableClass> deviceProtocolPluggableClassFinder = mockFinder(Collections.singletonList(pluggableClass));
        when(protocolPluggableService.findAllDeviceProtocolPluggableClasses()).thenReturn(deviceProtocolPluggableClassFinder);
        Response response = target("/pluggableclasses").queryParam("start", 0).queryParam("limit", 10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/pluggableclasses?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(1);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(31);
        assertThat(model.<String>get("data[0].name")).isEqualTo("WebRTU");
        assertThat(model.<String>get("data[0].version")).isEqualTo("1.9.2.3546");
        assertThat(model.<String>get("$.data[0].javaClassName")).isEqualTo("com.energyict.prot.WebRTU.class");

        assertThat(model.<String>get("$.data[0].client.key")).isEqualTo("Property");
        assertThat(model.<String>get("$.data[0].client.name")).isEqualTo("Property");
        assertThat(model.<Boolean>get("$.data[0].client.required")).isEqualTo(false);

        assertThat(model.<List>get("$.data[0].securitySuites")).hasSize(1);
        assertThat(model.<Integer>get("$.data[0].securitySuites[0].id")).isEqualTo(1);
        assertThat(model.<String>get("$.data[0].securitySuites[0].link.params.rel")).isEqualTo("related");
        assertThat(model.<String>get("$.data[0].securitySuites[0].link.href")).isEqualTo("http://localhost:9998/pluggableclasses/31/securitysuites/1");

        assertThat(model.<List>get("$.data[0].authenticationAccessLevels")).hasSize(1);
        assertThat(model.<Integer>get("$.data[0].authenticationAccessLevels[0].id")).isEqualTo(2);
        assertThat(model.<String>get("$.data[0].authenticationAccessLevels[0].link.params.rel")).isEqualTo("related");
        assertThat(model.<String>get("$.data[0].authenticationAccessLevels[0].link.href")).isEqualTo("http://localhost:9998/pluggableclasses/31/authenticationaccesslevels/2");

        assertThat(model.<List>get("$.data[0].encryptionAccessLevels")).hasSize(1);
        assertThat(model.<Integer>get("$.data[0].encryptionAccessLevels[0].id")).isEqualTo(3);
        assertThat(model.<String>get("$.data[0].encryptionAccessLevels[0].link.params.rel")).isEqualTo("related");
        assertThat(model.<String>get("$.data[0].encryptionAccessLevels[0].link.href")).isEqualTo("http://localhost:9998/pluggableclasses/31/encryptionaccesslevels/3");

        assertThat(model.<List>get("$.data[0].requestSecurityAccessLevels")).hasSize(1);
        assertThat(model.<Integer>get("$.data[0].requestSecurityAccessLevels[0].id")).isEqualTo(4);
        assertThat(model.<String>get("$.data[0].requestSecurityAccessLevels[0].link.params.rel")).isEqualTo("related");
        assertThat(model.<String>get("$.data[0].requestSecurityAccessLevels[0].link.href")).isEqualTo("http://localhost:9998/pluggableclasses/31/requestsecurityaccesslevels/4");

        assertThat(model.<List>get("$.data[0].responseSecurityAccessLevels")).hasSize(1);
        assertThat(model.<Integer>get("$.data[0].responseSecurityAccessLevels[0].id")).isEqualTo(5);
        assertThat(model.<String>get("$.data[0].responseSecurityAccessLevels[0].link.params.rel")).isEqualTo("related");
        assertThat(model.<String>get("$.data[0].responseSecurityAccessLevels[0].link.href")).isEqualTo("http://localhost:9998/pluggableclasses/31/responsesecurityaccesslevels/5");

        assertThat(model.<List>get("$.data[0].providedConnectionFunctions")).hasSize(2);
        assertThat(model.<Integer>get("$.data[0].providedConnectionFunctions[0].id")).isEqualTo(1);
        assertThat(model.<String>get("$.data[0].providedConnectionFunctions[0].link.params.rel")).isEqualTo("related");
        assertThat(model.<String>get("$.data[0].providedConnectionFunctions[0].link.href")).isEqualTo("http://localhost:9998/pluggableclasses/31/connectionfunctions/1");
        assertThat(model.<Integer>get("$.data[0].providedConnectionFunctions[1].id")).isEqualTo(2);
        assertThat(model.<String>get("$.data[0].providedConnectionFunctions[1].link.params.rel")).isEqualTo("related");
        assertThat(model.<String>get("$.data[0].providedConnectionFunctions[1].link.href")).isEqualTo("http://localhost:9998/pluggableclasses/31/connectionfunctions/2");

        assertThat(model.<List>get("$.data[0].consumableConnectionFunctions")).hasSize(2);
        assertThat(model.<Integer>get("$.data[0].consumableConnectionFunctions[0].id")).isEqualTo(2);
        assertThat(model.<String>get("$.data[0].consumableConnectionFunctions[0].link.params.rel")).isEqualTo("related");
        assertThat(model.<String>get("$.data[0].consumableConnectionFunctions[0].link.href")).isEqualTo("http://localhost:9998/pluggableclasses/31/connectionfunctions/2");
        assertThat(model.<Integer>get("$.data[0].consumableConnectionFunctions[1].id")).isEqualTo(3);
        assertThat(model.<String>get("$.data[0].consumableConnectionFunctions[1].link.params.rel")).isEqualTo("related");
        assertThat(model.<String>get("$.data[0].consumableConnectionFunctions[1].link.href")).isEqualTo("http://localhost:9998/pluggableclasses/31/connectionfunctions/3");

        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/pluggableclasses/31");
    }

    @Test
    public void testGetSingleDeviceProtocolPluggableClassWithFields() throws Exception {
        mockPluggableClass(31L, "WebRTU", "1.9.2.3546");
        Response response = target("/pluggableclasses/31").queryParam("fields", "id,name").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<String>get("$.name")).isEqualTo("WebRTU");
        assertThat(model.<String>get("$.version")).isNull();
        assertThat(model.<String>get("$.link")).isNull();
        assertThat(model.<String>get("$.client")).isNull();
        assertThat(model.<String>get("$.securitySuites")).isNull();
        assertThat(model.<String>get("$.authenticationAccessLevels")).isNull();
        assertThat(model.<String>get("$.encryptionAccessLevels")).isNull();
        assertThat(model.<String>get("$.requestSecurityAccessLevels")).isNull();
        assertThat(model.<String>get("$.responseSecurityAccessLevels")).isNull();
    }

    @Test
    public void testDeviceProtocolPluggableClassFields() throws Exception {
        Response response = target("/pluggableclasses").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(13);
        assertThat(model.<List<String>>get("$")).containsOnly("id", "link", "name", "version", "javaClassName", "client", "securitySuites", "authenticationAccessLevels", "encryptionAccessLevels",
                "requestSecurityAccessLevels", "responseSecurityAccessLevels", "providedConnectionFunctions", "consumableConnectionFunctions");
    }

}