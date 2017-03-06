package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceSecurityPropertySetResourceTest extends MultisensePublicApiJerseyTest {

    Clock clock = Clock.fixed(Instant.ofEpochSecond(1448841600), ZoneId.systemDefault());
    private Device device;
    private SecurityPropertySet sps1, sps2;


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        DeviceType deviceType = mockDeviceType(1L, "device type", 1001L);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(2L, "device config", deviceType, 1002L);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationAccessLevel(3);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mockEncryptionAccessLevel(4);
        sps1 = mockSecurityPropertySet(5L, deviceConfiguration, "sps1", encryptionDeviceAccessLevel, authenticationDeviceAccessLevel, 1003L);
        sps2 = mockSecurityPropertySet(6L, deviceConfiguration, "sps2", encryptionDeviceAccessLevel, authenticationDeviceAccessLevel, 1004L);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        device = mockDevice("XAS", "10101010101011", deviceConfiguration, 1005L);
        SecurityProperty securityProperty1 = mock(SecurityProperty.class);
        when(securityProperty1.getName()).thenReturn("string.property");
        when(securityProperty1.getActivePeriod()).thenReturn(Interval.of(Range.closed(Instant.now(clock), Instant.now(clock).plusSeconds(60))));
        when(securityProperty1.getValue()).thenReturn("Hello world 1");
        when(securityProperty1.isComplete()).thenReturn(true);
        when(device.getSecurityProperties(sps1)).thenReturn(Collections.singletonList(securityProperty1));
        SecurityProperty securityProperty2 = mock(SecurityProperty.class);
        when(securityProperty2.getName()).thenReturn("string.property");
        when(securityProperty2.getActivePeriod()).thenReturn(Interval.of(Range.closed(Instant.now(clock), Instant.now(clock).plusSeconds(60))));
        when(securityProperty2.getValue()).thenReturn("Hello world 2");
        when(securityProperty2.isComplete()).thenReturn(true);
        when(device.getSecurityProperties(sps2)).thenReturn(Collections.singletonList(securityProperty1));
    }

    @Test
    public void testAllGetDeviceSecurityPropertySetsPaged() throws Exception {
        PropertyInfo propertyInfo = new PropertyInfo("name", "name", new PropertyValueInfo<>("value", null), new PropertyTypeInfo(), false);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("/devices/XAS/securitypropertysets").queryParam("start",0).queryParam("limit",10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/devices/XAS/securitypropertysets?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(2);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(5);
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/devices/XAS/securitypropertysets/5");
        assertThat(model.<Integer>get("data[1].id")).isEqualTo(6);
        assertThat(model.<String>get("data[1].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[1].link.href")).isEqualTo("http://localhost:9998/devices/XAS/securitypropertysets/6");
    }

    @Test
    public void testGetSingleDeviceSecurityPropertySetWithFields() throws Exception {
        Response response = target("/devices/XAS/securitypropertysets/5").queryParam("fields","id,version").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(5);
        assertThat(model.<Integer>get("$.version")).isEqualTo(1003);
        assertThat(model.<String>get("$.complete")).isNull();
        assertThat(model.<String>get("$.link")).isNull();
        assertThat(model.get("$.configuredSecurityPropertySet")).isNull();
    }

    @Test
    public void testGetSingleDeviceSecurityPropertySetAllFields() throws Exception {
        PropertyInfo propertyInfo = new PropertyInfo("string.property", "string.property", new PropertyValueInfo<>("Hello world 1", "default"), new PropertyTypeInfo(SimplePropertyType.TEXT, null, null, null), true);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("/devices/XAS/securitypropertysets/5").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(5);
        assertThat(model.<Integer>get("$.version")).isEqualTo(1003);
        assertThat(model.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.link.href")).isEqualTo("http://localhost:9998/devices/XAS/securitypropertysets/5");
        assertThat(model.<String>get("$.device.link.href")).isEqualTo("http://localhost:9998/devices/XAS");
        assertThat(model.<Boolean>get("$.complete")).isEqualTo(true);
        assertThat(model.<String>get("$.configuredSecurityPropertySet.link.href")).isEqualTo("http://localhost:9998/devicetypes/1/deviceconfigurations/2/securitypropertysets/5");
        assertThat(model.<Long>get("$.effectivePeriod.start")).isEqualTo(1448841600000L);
        assertThat(model.<Long>get("$.effectivePeriod.end")).isEqualTo(1448841660000L);
        assertThat(model.<String>get("$.properties[0].key")).isEqualTo("string.property");
        assertThat(model.<String>get("$.properties[0].propertyValueInfo.defaultValue")).isEqualTo("default");
        assertThat(model.<String>get("$.properties[0].propertyValueInfo.value")).isEqualTo("Hello world 1");
        assertThat(model.<String>get("$.properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(model.<Boolean>get("$.properties[0].required")).isEqualTo(true);
    }

    @Test
    public void testSetValuesForSecuritySet() throws Exception {
        DeviceSecurityPropertySetInfo info = new DeviceSecurityPropertySetInfo();
        info.device = new LinkInfo();
        info.device.version = 1005L;
        info.configuredSecurityPropertySet = new LinkInfo();
        info.configuredSecurityPropertySet.version = 1003L;
        PropertyValueInfo<String> valueInfo = new PropertyValueInfo<>("Hello Kitty", null, null, true);
        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo();
        propertyTypeInfo.simplePropertyType = SimplePropertyType.TEXT;
        PropertyInfo propertyInfo = new PropertyInfo("string.property", "string.property", valueInfo, propertyTypeInfo, true);
        info.properties = new ArrayList<>();
        info.properties.add(propertyInfo);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        when(propertyValueInfoService.findPropertyValue(any(), any())).thenReturn(propertyInfo.getPropertyValueInfo().getValue());
        Response response = target("/devices/XAS/securitypropertysets/5").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        ArgumentCaptor<TypedProperties> typedPropertiesArgumentCaptor = ArgumentCaptor.forClass(TypedProperties.class);

        verify(device).setSecurityProperties(eq(sps1), typedPropertiesArgumentCaptor.capture());
        assertThat(typedPropertiesArgumentCaptor.getValue().hasValueFor("string.property")).isTrue();
        assertThat(typedPropertiesArgumentCaptor.getValue().getProperty("string.property")).isEqualTo("Hello Kitty");
    }

    @Test
    public void testSetValuesForSecuritySetWrongDeviceVersion() throws Exception {
        DeviceSecurityPropertySetInfo info = new DeviceSecurityPropertySetInfo();
        info.device = new LinkInfo();
        info.device.version = 99999L; // WRONG VERSION

        Response response = target("/devices/XAS/securitypropertysets/5").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testSetValuesForSecuritySetToNull() throws Exception {
        DeviceSecurityPropertySetInfo info = new DeviceSecurityPropertySetInfo();
        info.device = new LinkInfo();
        info.device.version = 1005L;
        info.configuredSecurityPropertySet = new LinkInfo();
        info.configuredSecurityPropertySet.version = 1003L;
        PropertyValueInfo<String> valueInfo = new PropertyValueInfo<>(null, null, null, true);
        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo();
        propertyTypeInfo.simplePropertyType = SimplePropertyType.TEXT;
        PropertyInfo propertyInfo = new PropertyInfo("string.property", "string.property", valueInfo, propertyTypeInfo, true);
        info.properties = new ArrayList<>();
        info.properties.add(propertyInfo);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("/devices/XAS/securitypropertysets/5").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        ArgumentCaptor<TypedProperties> typedPropertiesArgumentCaptor = ArgumentCaptor.forClass(TypedProperties.class);

        verify(device).setSecurityProperties(eq(sps1), typedPropertiesArgumentCaptor.capture());
        assertThat(typedPropertiesArgumentCaptor.getValue().hasValueFor("string.property")).isTrue();
        assertThat(typedPropertiesArgumentCaptor.getValue().getProperty("string.property")).isEqualTo(null);
    }

    @Test
    public void testSetValuesForSecuritySetUnset() throws Exception {
        DeviceSecurityPropertySetInfo info = new DeviceSecurityPropertySetInfo();
        info.device = new LinkInfo();
        info.device.version = 1005L;
        info.configuredSecurityPropertySet = new LinkInfo();
        info.configuredSecurityPropertySet.version = 1003L;
        PropertyValueInfo<String> valueInfo = new PropertyValueInfo<>(null, null, null, false);
        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo();
        propertyTypeInfo.simplePropertyType = SimplePropertyType.TEXT;
        PropertyInfo propertyInfo = new PropertyInfo("string.property", "string.property", valueInfo, propertyTypeInfo, true);
        info.properties = new ArrayList<>();
        info.properties.add(propertyInfo);
        when(propertyValueInfoService.getPropertyInfo(any(), any())).thenReturn(propertyInfo);
        Response response = target("/devices/XAS/securitypropertysets/5").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        ArgumentCaptor<TypedProperties> typedPropertiesArgumentCaptor = ArgumentCaptor.forClass(TypedProperties.class);

        verify(device).setSecurityProperties(eq(sps1), typedPropertiesArgumentCaptor.capture());
        assertThat(typedPropertiesArgumentCaptor.getValue().hasValueFor("string.property")).isFalse();
    }

    @Test
    public void testDeviceSecurityPropertySetFields() throws Exception {
        Response response = target("/devices/XAS/securitypropertysets").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(8);
        assertThat(model.<List<String>>get("$")).containsOnly("complete","configuredSecurityPropertySet","device","effectivePeriod","id","link","properties","version");
    }

}
