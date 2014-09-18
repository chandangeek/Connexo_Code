package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.engine.model.ComPortPool;
import java.util.Arrays;
import java.util.Map;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.dashboard.rest.status.ComServerStatusResource} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (17:27)
 */
public class DashboardFieldResourceTest extends DashboardRESTJerseyTest {

    @Test
    public void testJava8Construct() throws Exception {
        ComPortPool comPortPool1 = mockComPortPool(1, "one");
        ComPortPool comPortPool2 = mockComPortPool(2, "two");
        ComPortPool comPortPool3 = mockComPortPool(3, "three");
        when(engineModelService.findAllComPortPools()).thenReturn(Arrays.asList(comPortPool1, comPortPool2, comPortPool3));

        target("/field/comportpools").request().get(Map.class);
    }

    private ComPortPool mockComPortPool(long id, String name) {
        ComPortPool mock = mock(ComPortPool.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        return mock;
    }


}