package com.energyict.mdc.multisense.api.redknee.impl;

import com.jayway.jsonpath.JsonModel;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bvn on 9/16/15.
 */
public class SimulatorResourceTest extends MultisensePrepaymentApiJerseyTest {
    @Test
    public void testGetSimulatorNotRunning() throws Exception {
        Response response = target("/simulator").request().get();
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.consumption")).isEqualTo(0);
        assertThat(jsonModel.<Integer>get("$.frequency")).isEqualTo(0);
        assertThat(jsonModel.<List>get("$.usagePoints")).isEmpty();
    }

    @Test
    public void testStartSimulator() throws Exception {
        SimulatorResource.SimulatorInfo info = new SimulatorResource.SimulatorInfo();
        info.path = System.getProperty("java.io.tmpdir");
        info.frequency = 10;
        info.consumption = 1000;
        info.usagePoints = Collections.emptyList();

        Response response = target("/simulator").request().put(Entity.json(info));
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.consumption")).isEqualTo(1000);
        assertThat(jsonModel.<Integer>get("$.frequency")).isEqualTo(10);
        assertThat(jsonModel.<String>get("$.path")).isEqualTo(System.getProperty("java.io.tmpdir"));
        assertThat(jsonModel.<List>get("$.usagePoints")).isEmpty();
    }
}
