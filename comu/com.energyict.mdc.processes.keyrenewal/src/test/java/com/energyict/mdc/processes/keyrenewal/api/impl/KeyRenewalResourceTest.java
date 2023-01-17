package com.energyict.mdc.processes.keyrenewal.api.impl;

import com.energyict.mdc.common.device.data.Device;

import javax.ws.rs.core.Response;
import java.util.Optional;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by sla on 21/03/2017.
 */
public class KeyRenewalResourceTest extends KeyRenewalApplicationTest {

    @Test
    public void testGetDeviceRenew() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findDeviceByMrid("abc123")).thenReturn(Optional.of(device));
        Response response = target("/devices/abc123/renewKey").request().get();
    }
}
