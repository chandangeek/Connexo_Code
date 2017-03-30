/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ComServerFieldResourceTest extends ComserverCoreApplicationJerseyTest {

    @Test
    public void testNrOfStopBitsKeys() throws Exception {
        Map response = target("/field/nrOfStopBits").request().get(Map.class);
        assertThat(response).containsKey("nrOfStopBits");
        List<Map<String, String>> content = (List<Map<String, String>>) response.get("nrOfStopBits");
        for (Map<String, String> value : content) {
            assertThat(value.containsKey("nrOfStopBits"));
        }
    }

    @Test
    public void testNrOfDataBitsKeys() throws Exception {
        Map response = target("/field/nrOfDataBits").request().get(Map.class);
        assertThat(response).containsKey("nrOfDataBits");
        List<Map<String, String>> content = (List<Map<String, String>>) response.get("nrOfDataBits");
        for (Map<String, String> value : content) {
            assertThat(value.containsKey("nrOfDataBits"));
        }
    }

    @Test
    public void testLogLevelKeys() throws Exception {
        Map response = target("/field/logLevel").request().get(Map.class);
        assertThat(response).containsKey("logLevels");
        List<Map<String, String>> content = (List<Map<String, String>>) response.get("logLevels");
        for (Map<String, String> value : content) {
            assertThat(value.containsKey("logLevel"));
        }
    }

    @Test
    public void testComPortTypeKeys() throws Exception {
        Map response = target("/field/comPortType").request().get(Map.class);
        assertThat(response).containsKey("comPortTypes");
        List<Map<String, String>> content = (List<Map<String, String>>) response.get("comPortTypes");
        for (Map<String, String> value : content) {
            assertThat(value.containsKey("comPortType"));
        }
    }

    @Test
    public void testBaudRateKeys() throws Exception {
        Map response = target("/field/baudRate").request().get(Map.class);
        assertThat(response).containsKey("baudRates");
        List<Map<String, String>> content = (List<Map<String, String>>) response.get("baudRates");
        for (Map<String, String> value : content) {
            assertThat(value.containsKey("baudRate"));
        }
    }

    @Test
    public void testTimeUnitKeys() throws Exception {
        Map response = target("/field/timeUnit").request().get(Map.class);
        assertThat(response).containsKey("timeUnits");
        List<Map<String, String>> content = (List<Map<String, String>>) response.get("timeUnits");
        for (Map<String, String> value : content) {
            assertThat(value.containsKey("timeUnit"));
        }
    }

    @Test
    public void testFlowControlKeys() throws Exception {
        Map response = target("/field/flowControl").request().get(Map.class);
        assertThat(response).containsKey("flowControls");
        List<Map<String, String>> content = (List<Map<String, String>>) response.get("flowControls");
        for (Map<String, String> value : content) {
            assertThat(value.containsKey("flowControl"));
        }
    }

    @Test
    public void testParitiesKeys() throws Exception {
        Map response = target("/field/parity").request().get(Map.class);
        assertThat(response).containsKey("parities");
        List<Map<String, String>> content = (List<Map<String, String>>) response.get("parities");
        for (Map<String, String> value : content) {
            assertThat(value.containsKey("parity"));
        }
    }
}