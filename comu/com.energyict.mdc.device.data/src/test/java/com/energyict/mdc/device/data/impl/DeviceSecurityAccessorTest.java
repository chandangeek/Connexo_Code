/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.KeyAccessorStatus;
import com.energyict.mdc.device.data.impl.pki.PlainTextSymmetricKeyAccessorImpl;
import com.energyict.mdc.device.data.impl.pki.SymmetricKeyAccessorImpl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 4/12/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceSecurityAccessorTest {
    @Mock
    SecurityManagementService securityManagementService;
    @Mock
    SecurityAccessorType securityAccessorType;
    @Mock
    DataModel dataModel;
    @Mock
    Device device;

    private SymmetricKeyAccessorImpl symmetricKeyAccessor;

    @Before
    public void setUp() throws Exception {
        PropertySpec propertySpec1 = mock(PropertySpec.class);
        when(propertySpec1.getName()).thenReturn("prop1");
        PropertySpec propertySpec2 = mock(PropertySpec.class);
        when(propertySpec2.getName()).thenReturn("prop2");
        when(securityManagementService.getPropertySpecs(securityAccessorType)).thenReturn(Arrays.asList(propertySpec1, propertySpec2));
        when(dataModel.asRefAny(anyObject())).then(invocation->new FakeRefAny(invocation.getArguments()[0]));
        symmetricKeyAccessor = new PlainTextSymmetricKeyAccessorImpl(dataModel, securityManagementService, null);
        symmetricKeyAccessor.init(securityAccessorType, device);
    }

    @Test
    public void testCompleteState() throws Exception {
        SymmetricKeyWrapper symmetricKeyWrapper = mock(SymmetricKeyWrapper.class);
        symmetricKeyAccessor.setActualValue(symmetricKeyWrapper);
        Map<String, Object> properties = new HashMap<>();
        properties.put("prop1", "value");
        properties.put("prop2", "value");
        when(symmetricKeyWrapper.getProperties()).thenReturn(properties);

        assertThat(symmetricKeyAccessor.getStatus()).isEqualTo(KeyAccessorStatus.COMPLETE);
    }

    @Test
    public void testStateWithMissingProperty() throws Exception {
        SymmetricKeyWrapper symmetricKeyWrapper = mock(SymmetricKeyWrapper.class);
        symmetricKeyAccessor.setActualValue(symmetricKeyWrapper);
        Map<String, Object> properties = new HashMap<>();
        properties.put("prop1", "value");
        when(symmetricKeyWrapper.getProperties()).thenReturn(properties);

        assertThat(symmetricKeyAccessor.getStatus()).isEqualTo(KeyAccessorStatus.INCOMPLETE);
    }

    @Test
    public void testStateWithNullProperty() throws Exception {
        SymmetricKeyWrapper symmetricKeyWrapper = mock(SymmetricKeyWrapper.class);
        symmetricKeyAccessor.setActualValue(symmetricKeyWrapper);
        Map<String, Object> properties = new HashMap<>();
        properties.put("prop1", "value");
        properties.put("prop2", null);
        when(symmetricKeyWrapper.getProperties()).thenReturn(properties);

        assertThat(symmetricKeyAccessor.getStatus()).isEqualTo(KeyAccessorStatus.INCOMPLETE);
    }

    class FakeRefAny implements RefAny {

        private Object value;

        public FakeRefAny() {
        }

        public FakeRefAny(Object value) {
            this.value = value;
        }

        @Override
        public boolean isPresent() {
            return value != null;
        }

        @Override
        public Object get() {
            return value;
        }

        @Override
        public Optional<?> getOptional() {
            return Optional.ofNullable(value);
        }

        @Override
        public String getComponent() {
            return "PKI";
        }

        @Override
        public String getTableName() {
            return "";
        }

        @Override
        public Object[] getPrimaryKey() {
            return new Object[0];
        }

        @Override
        public void clearCachedObject() {

        }
    }

}
