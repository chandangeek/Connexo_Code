package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ServiceKind;
import com.energyict.mdc.protocol.api.DeviceFunction;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AdapterTest {

    @Test
    public void testServiceKindAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new ServiceKindAdapter(), ServiceKind.values());
    }

    @Test
    public void testDeviceFunctionAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new DeviceFunctionAdapter(), DeviceFunction.values());
    }

    private void testAdapter(XmlAdapter adapter, Object[] values) throws Exception {
        for (Object serverSideValue : values) {
            assertThat(adapter.marshal(serverSideValue)).describedAs("Unmapped server-side value detected in adapter "+adapter.getClass().getSimpleName()).isNotNull();
        }

    }

}
