package com.energyict.mdc.rest.impl;

import com.energyict.mdc.channels.serial.BaudrateValue;
import com.energyict.mdc.channels.serial.FlowControl;
import com.energyict.mdc.channels.serial.NrOfDataBits;
import com.energyict.mdc.channels.serial.NrOfStopBits;
import com.energyict.mdc.channels.serial.Parities;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.rest.impl.comserver.BaudrateAdapter;
import com.energyict.mdc.rest.impl.comserver.FlowControlAdapter;
import com.energyict.mdc.rest.impl.comserver.LogLevelAdapter;
import com.energyict.mdc.rest.impl.comserver.NrOfDataBitsAdapter;
import com.energyict.mdc.rest.impl.comserver.NrOfStopBitsAdapter;
import com.energyict.mdc.rest.impl.comserver.ParitiesAdapter;
import org.junit.Test;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import static org.assertj.core.api.Assertions.assertThat;

public class AdapterTest {

    @Test
    public void testNrOfDataBitsAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new NrOfDataBitsAdapter(), NrOfDataBits.values());
    }

    @Test
    public void testNrOfStopBitsAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new NrOfStopBitsAdapter(), NrOfStopBits.values());
    }

    @Test
    public void testBaudrateAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new BaudrateAdapter(), BaudrateValue.values());
    }

    @Test
    public void testFlowControlHasValueForEveryServerValue() throws Exception {
        testAdapter(new FlowControlAdapter(), FlowControl.values());
    }

    @Test
    public void testParitiesHasValueForEveryServerValue() throws Exception {
        testAdapter(new ParitiesAdapter(), Parities.values());
    }

    @Test
    public void testLogLevelAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new LogLevelAdapter(), ComServer.LogLevel.values());
    }

    private void testAdapter(XmlAdapter adapter, Object[] values) throws Exception {
        for (Object serverSideValue : values) {
            assertThat(adapter.marshal(serverSideValue)).describedAs("Unmapped server-side value detected in adapter "+adapter.getClass().getSimpleName()).isNotNull();
        }

    }

}
