package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.jayway.jsonpath.JsonModel;
import java.util.Arrays;
import java.util.EnumSet;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 9/30/14.
 */
public class SecurityPropertySetResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Test
    public void testPlainGetter() throws Exception {
        Device device = mock(Device.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        SecurityPropertySet sps1 = mock(SecurityPropertySet.class);
        when(sps1.getUserActions()).thenReturn(EnumSet.of(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1));
        when(sps1.currentUserIsAllowedToViewDeviceProperties()).thenReturn(true);
        when(sps1.currentUserIsAllowedToEditDeviceProperties()).thenReturn(true);

        SecurityPropertySet sps2 = mock(SecurityPropertySet.class);
        when(sps2.getUserActions()).thenReturn(EnumSet.of(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2, DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2));
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceDataService.findByUniqueMrid("AX1")).thenReturn(device);

        String response = target("/devices/AX1/securityproperties").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<String>get("$.")).isEqualTo("");
    }
}
