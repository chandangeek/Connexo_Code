/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ServiceKind;
import com.energyict.mdc.common.rest.AccumulationAdapter;
import com.energyict.mdc.common.rest.AggregateAdapter;
import com.energyict.mdc.common.rest.CommodityAdapter;
import com.energyict.mdc.common.rest.FlowDirectionAdapter;
import com.energyict.mdc.common.rest.MacroPeriodAdapter;
import com.energyict.mdc.common.rest.MeasurementKindAdapter;
import com.energyict.mdc.common.rest.MetricMultiplierAdapter;
import com.energyict.mdc.common.rest.PhaseAdapter;
import com.energyict.mdc.common.rest.ReadingTypeUnitAdapter;
import com.energyict.mdc.common.rest.TimeAttributeAdapter;
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

    @Test
    public void testAccumulationAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new AccumulationAdapter(), Accumulation.values());
    }

    @Test
    public void testMacroPeriodAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new MacroPeriodAdapter(), MacroPeriod.values());
    }

    @Test
    public void testAggregateAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new AggregateAdapter(), Aggregate.values());
    }

    @Test
    public void testTimeAttributeAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new TimeAttributeAdapter(), TimeAttribute.values());
    }

    @Test
    public void testFlowDirectionAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new FlowDirectionAdapter(), FlowDirection.values());
    }

    @Test
    public void testCommodityAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new CommodityAdapter(), Commodity.values());
    }

    @Test
    public void testMeasurementKindAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new MeasurementKindAdapter(), MeasurementKind.values());
    }

    @Test
    public void testPhaseAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new PhaseAdapter(), Phase.values());
    }

    @Test
    public void testMetricMultiplierAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new MetricMultiplierAdapter(), MetricMultiplier.values());
    }

    @Test
    public void testReadingTypeUnitAdapterHasValueForEveryServerValue() throws Exception {
        testAdapter(new ReadingTypeUnitAdapter(), ReadingTypeUnit.values());
    }

    private void testAdapter(XmlAdapter adapter, Object[] values) throws Exception {
        for (Object serverSideValue : values) {
            assertThat(adapter.marshal(serverSideValue)).describedAs("Unmapped server-side value detected in adapter "+adapter.getClass().getSimpleName()).isNotNull();
        }
    }

}
