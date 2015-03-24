package com.energyict.mdc.device.lifecycle.config.rest;

import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LifecycleResourceTest extends DeviceLifecycleConfigApplicationJerseyTest {

    @Test
    public void testLifecycleJsonModel(){
        DeviceLifeCycle dlc = mockSimpleDeviceLifeCycle(1L, "Standard");
        Finder<DeviceLifeCycle> finder = mock(Finder.class);
        when(finder.find()).thenReturn(Collections.singletonList(dlc));
        when(deviceLifeCycleConfigurationService.findAllDeviceLifeCycles()).thenReturn(finder);
        String stringResponse = target("/devicelifecycles").request().get(String.class);
        JsonModel model = JsonModel.create(stringResponse);

        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List<?>>get("$.deviceLifeCycles")).isNotNull();
        assertThat(model.<List<?>>get("$.deviceLifeCycles")).hasSize(1);
        assertThat(model.<Number>get("$.deviceLifeCycles[0].id")).isEqualTo(1);
        assertThat(model.<String>get("$.deviceLifeCycles[0].name")).isEqualTo("Standard");
    }

    @Test
    public void testEmptylifecycleList(){
        Finder<DeviceLifeCycle> finder = mock(Finder.class);
        when(finder.find()).thenReturn(Collections.emptyList());
        when(deviceLifeCycleConfigurationService.findAllDeviceLifeCycles()).thenReturn(finder);
        String stringResponse = target("/devicelifecycles").request().get(String.class);
        JsonModel model = JsonModel.create(stringResponse);

        assertThat(model.<Number>get("$.total")).isEqualTo(0);
        assertThat(model.<List<?>>get("$.deviceLifeCycles")).isNotNull();
        assertThat(model.<List<?>>get("$.deviceLifeCycles")).isEmpty();
    }
}
