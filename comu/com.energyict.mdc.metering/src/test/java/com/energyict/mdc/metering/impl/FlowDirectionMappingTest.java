/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.FlowDirection;
import com.energyict.mdc.common.ObisCode;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class FlowDirectionMappingTest {

    @Test
    public void forwardTest(){
        Map<ObisCode, FlowDirection> forwards = new HashMap<>();
        forwards.put(ObisCode.fromString("1.0.1.8.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.1.8.0.255")));
        forwards.put(ObisCode.fromString("1.0.3.8.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.3.8.0.255")));
        forwards.put(ObisCode.fromString("1.0.9.9.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.9.9.0.255")));
        forwards.put(ObisCode.fromString("1.0.21.9.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.21.9.0.255")));
        forwards.put(ObisCode.fromString("1.0.23.7.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.23.7.0.255")));
        forwards.put(ObisCode.fromString("1.0.29.7.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.29.7.0.255")));
        forwards.put(ObisCode.fromString("1.0.41.3.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.41.3.0.255")));
        forwards.put(ObisCode.fromString("1.0.43.3.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.43.3.0.255")));
        forwards.put(ObisCode.fromString("1.0.49.3.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.49.3.0.255")));
        forwards.put(ObisCode.fromString("1.0.61.1.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.61.1.0.255")));
        forwards.put(ObisCode.fromString("1.0.63.1.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.63.1.0.255")));
        forwards.put(ObisCode.fromString("1.0.69.1.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.69.1.0.255")));

        for (Map.Entry<ObisCode, FlowDirection> obisCodeFlowDirectionEntry : forwards.entrySet()) {
            assertThat(obisCodeFlowDirectionEntry.getValue()).overridingErrorMessage("ObisCode " + obisCodeFlowDirectionEntry.getKey()
                    + " should have resulted in a '" + FlowDirection.FORWARD + "' flowdirection but was '" + obisCodeFlowDirectionEntry.getValue() + "'")
                    .isEqualTo(FlowDirection.FORWARD);
        }
    }

    @Test
    public void nonElectricityForwardTest(){
        ObisCode obisCode = ObisCode.fromString("123.0.1.8.0.255");
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);

        assertThat(flowDirection).isNotEqualTo(FlowDirection.FORWARD);
        assertThat(flowDirection).isEqualTo(FlowDirection.NOTAPPLICABLE);
    }

    @Test
    public void electricityNonForwardForForwardTest() {
        ObisCode obisCode = ObisCode.fromString("1.0.2.8.0.255");
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);

        assertThat(flowDirection).isNotEqualTo(FlowDirection.FORWARD);
    }

    @Test
    public void reverseTest() {
        Map<ObisCode, FlowDirection> forwards = new HashMap<>();
        forwards.put(ObisCode.fromString("1.0.2.8.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.2.8.0.255")));
        forwards.put(ObisCode.fromString("1.0.4.8.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.4.8.0.255")));
        forwards.put(ObisCode.fromString("1.0.10.9.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.10.9.0.255")));
        forwards.put(ObisCode.fromString("1.0.22.9.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.22.9.0.255")));
        forwards.put(ObisCode.fromString("1.0.24.7.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.24.7.0.255")));
        forwards.put(ObisCode.fromString("1.0.30.7.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.30.7.0.255")));
        forwards.put(ObisCode.fromString("1.0.42.3.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.42.3.0.255")));
        forwards.put(ObisCode.fromString("1.0.44.3.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.44.3.0.255")));
        forwards.put(ObisCode.fromString("1.0.50.3.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.50.3.0.255")));
        forwards.put(ObisCode.fromString("1.0.62.1.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.62.1.0.255")));
        forwards.put(ObisCode.fromString("1.0.64.1.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.64.1.0.255")));
        forwards.put(ObisCode.fromString("1.0.70.1.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.70.1.0.255")));

        for (Map.Entry<ObisCode, FlowDirection> obisCodeFlowDirectionEntry : forwards.entrySet()) {
            assertThat(obisCodeFlowDirectionEntry.getValue()).overridingErrorMessage("ObisCode " + obisCodeFlowDirectionEntry.getKey()
                    + " should have resulted in a '" + FlowDirection.REVERSE + "' flowdirection but was '" + obisCodeFlowDirectionEntry.getValue() + "'")
                    .isEqualTo(FlowDirection.REVERSE);
        }
    }

    @Test
    public void nonElectricityReverseTest() {
        ObisCode obisCode = ObisCode.fromString("123.0.2.8.0.255");
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);

        assertThat(flowDirection).isNotEqualTo(FlowDirection.REVERSE);
        assertThat(flowDirection).isEqualTo(FlowDirection.NOTAPPLICABLE);
    }


    @Test
    public void electricityNonReverseForReverseTest(){
        ObisCode obisCode = ObisCode.fromString("1.0.3.8.0.255");
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);

        assertThat(flowDirection).isNotEqualTo(FlowDirection.REVERSE);
    }

    @Test
    public void q1Test() {
        Map<ObisCode, FlowDirection> forwards = new HashMap<>();
        forwards.put(ObisCode.fromString("1.0.5.8.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.5.8.0.255")));
        forwards.put(ObisCode.fromString("1.0.17.8.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.17.8.0.255")));
        forwards.put(ObisCode.fromString("1.0.25.9.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.25.9.0.255")));
        forwards.put(ObisCode.fromString("1.0.37.9.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.37.9.0.255")));
        forwards.put(ObisCode.fromString("1.0.45.7.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.45.7.0.255")));
        forwards.put(ObisCode.fromString("1.0.57.7.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.57.7.0.255")));
        forwards.put(ObisCode.fromString("1.0.65.3.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.65.3.0.255")));
        forwards.put(ObisCode.fromString("1.0.77.3.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.77.3.0.255")));

        for (Map.Entry<ObisCode, FlowDirection> obisCodeFlowDirectionEntry : forwards.entrySet()) {
            assertThat(obisCodeFlowDirectionEntry.getValue()).overridingErrorMessage("ObisCode " + obisCodeFlowDirectionEntry.getKey()
                    + " should have resulted in a '" + FlowDirection.Q1 + "' flowdirection but was '" + obisCodeFlowDirectionEntry.getValue() + "'")
                    .isEqualTo(FlowDirection.Q1);
        }
    }

    @Test
    public void nonElectricityQ1Test() {
        ObisCode obisCode = ObisCode.fromString("123.0.5.8.0.255");
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);

        assertThat(flowDirection).isNotEqualTo(FlowDirection.Q1);
        assertThat(flowDirection).isEqualTo(FlowDirection.NOTAPPLICABLE);
    }

    @Test
    public void electricityNonQ1ForQ1Test() {
        ObisCode obisCode = ObisCode.fromString("1.0.7.8.0.255");
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);

        assertThat(flowDirection).isNotEqualTo(FlowDirection.Q1);
    }

    @Test
    public void q2Test() {
        Map<ObisCode, FlowDirection> forwards = new HashMap<>();
        forwards.put(ObisCode.fromString("1.0.6.8.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.6.8.0.255")));
        forwards.put(ObisCode.fromString("1.0.18.8.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.18.8.0.255")));
        forwards.put(ObisCode.fromString("1.0.26.9.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.26.9.0.255")));
        forwards.put(ObisCode.fromString("1.0.38.9.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.38.9.0.255")));
        forwards.put(ObisCode.fromString("1.0.46.7.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.46.7.0.255")));
        forwards.put(ObisCode.fromString("1.0.58.7.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.58.7.0.255")));
        forwards.put(ObisCode.fromString("1.0.66.3.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.66.3.0.255")));
        forwards.put(ObisCode.fromString("1.0.78.3.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.78.3.0.255")));

        for (Map.Entry<ObisCode, FlowDirection> obisCodeFlowDirectionEntry : forwards.entrySet()) {
            assertThat(obisCodeFlowDirectionEntry.getValue()).overridingErrorMessage("ObisCode " + obisCodeFlowDirectionEntry.getKey()
                    + " should have resulted in a '" + FlowDirection.Q2 + "' flowdirection but was '" + obisCodeFlowDirectionEntry.getValue() + "'")
                    .isEqualTo(FlowDirection.Q2);
        }
    }

    @Test
    public void nonElectricityQ2Test() {
        ObisCode obisCode = ObisCode.fromString("123.0.6.8.0.255");
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);

        assertThat(flowDirection).isNotEqualTo(FlowDirection.Q2);
        assertThat(flowDirection).isEqualTo(FlowDirection.NOTAPPLICABLE);
    }

    @Test
    public void electricityNonQ2ForQ2Test() {
        ObisCode obisCode = ObisCode.fromString("1.0.7.8.0.255");
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);

        assertThat(flowDirection).isNotEqualTo(FlowDirection.Q2);
    }

    @Test
    public void q3Test() {
        Map<ObisCode, FlowDirection> forwards = new HashMap<>();
        forwards.put(ObisCode.fromString("1.0.7.8.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.7.8.0.255")));
        forwards.put(ObisCode.fromString("1.0.19.8.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.19.8.0.255")));
        forwards.put(ObisCode.fromString("1.0.27.9.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.27.9.0.255")));
        forwards.put(ObisCode.fromString("1.0.39.9.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.39.9.0.255")));
        forwards.put(ObisCode.fromString("1.0.47.7.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.47.7.0.255")));
        forwards.put(ObisCode.fromString("1.0.59.7.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.59.7.0.255")));
        forwards.put(ObisCode.fromString("1.0.67.3.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.67.3.0.255")));
        forwards.put(ObisCode.fromString("1.0.79.3.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.79.3.0.255")));

        for (Map.Entry<ObisCode, FlowDirection> obisCodeFlowDirectionEntry : forwards.entrySet()) {
            assertThat(obisCodeFlowDirectionEntry.getValue()).overridingErrorMessage("ObisCode " + obisCodeFlowDirectionEntry.getKey()
                    + " should have resulted in a '" + FlowDirection.Q3 + "' flowdirection but was '" + obisCodeFlowDirectionEntry.getValue() + "'")
                    .isEqualTo(FlowDirection.Q3);
        }
    }

    @Test
    public void nonElectricityQ3Test() {
        ObisCode obisCode = ObisCode.fromString("123.0.7.8.0.255");
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);

        assertThat(flowDirection).isNotEqualTo(FlowDirection.Q3);
        assertThat(flowDirection).isEqualTo(FlowDirection.NOTAPPLICABLE);
    }

    @Test
    public void electricityNonQ3ForQ3Test() {
        ObisCode obisCode = ObisCode.fromString("1.0.8.8.0.255");
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);

        assertThat(flowDirection).isNotEqualTo(FlowDirection.Q3);
    }

    @Test
    public void q4Test() {
        Map<ObisCode, FlowDirection> forwards = new HashMap<>();
        forwards.put(ObisCode.fromString("1.0.8.8.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.8.8.0.255")));
        forwards.put(ObisCode.fromString("1.0.20.8.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.20.8.0.255")));
        forwards.put(ObisCode.fromString("1.0.28.9.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.28.9.0.255")));
        forwards.put(ObisCode.fromString("1.0.40.9.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.40.9.0.255")));
        forwards.put(ObisCode.fromString("1.0.48.7.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.48.7.0.255")));
        forwards.put(ObisCode.fromString("1.0.60.7.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.60.7.0.255")));
        forwards.put(ObisCode.fromString("1.0.68.3.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.68.3.0.255")));
        forwards.put(ObisCode.fromString("1.0.80.3.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.80.3.0.255")));

        for (Map.Entry<ObisCode, FlowDirection> obisCodeFlowDirectionEntry : forwards.entrySet()) {
            assertThat(obisCodeFlowDirectionEntry.getValue()).overridingErrorMessage("ObisCode " + obisCodeFlowDirectionEntry.getKey()
                    + " should have resulted in a '" + FlowDirection.Q4 + "' flowdirection but was '" + obisCodeFlowDirectionEntry.getValue() + "'")
                    .isEqualTo(FlowDirection.Q4);
        }
    }

    @Test
    public void nonElectricityQ4Test() {
        ObisCode obisCode = ObisCode.fromString("123.0.8.8.0.255");
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);

        assertThat(flowDirection).isNotEqualTo(FlowDirection.Q4);
        assertThat(flowDirection).isEqualTo(FlowDirection.NOTAPPLICABLE);
    }

    @Test
    public void electricityNonQ4ForQ4Test() {
        ObisCode obisCode = ObisCode.fromString("1.0.9.8.0.255");
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);

        assertThat(flowDirection).isNotEqualTo(FlowDirection.Q4);
    }

    @Test
    public void leadingTest() {
        Map<ObisCode, FlowDirection> forwards = new HashMap<>();
        forwards.put(ObisCode.fromString("1.0.13.8.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.13.8.0.255")));
        forwards.put(ObisCode.fromString("1.0.33.8.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.33.8.0.255")));
        forwards.put(ObisCode.fromString("1.0.53.9.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.53.9.0.255")));
        forwards.put(ObisCode.fromString("1.0.73.9.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.73.9.0.255")));

        for (Map.Entry<ObisCode, FlowDirection> obisCodeFlowDirectionEntry : forwards.entrySet()) {
            assertThat(obisCodeFlowDirectionEntry.getValue()).overridingErrorMessage("ObisCode " + obisCodeFlowDirectionEntry.getKey()
                    + " should have resulted in a '" + FlowDirection.LEADING + "' flowdirection but was '" + obisCodeFlowDirectionEntry.getValue() + "'")
                    .isEqualTo(FlowDirection.LEADING);
        }
    }

    @Test
    public void nonElectricityLeadingTest() {
        ObisCode obisCode = ObisCode.fromString("123.0.13.8.0.255");
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);

        assertThat(flowDirection).isNotEqualTo(FlowDirection.LEADING);
        assertThat(flowDirection).isEqualTo(FlowDirection.NOTAPPLICABLE);
    }

    @Test
    public void electricityNonLeadingForLeadingTest() {
        ObisCode obisCode = ObisCode.fromString("1.0.14.8.0.255");
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);

        assertThat(flowDirection).isNotEqualTo(FlowDirection.LEADING);
    }

    @Test
    public void laggingTest() {
        Map<ObisCode, FlowDirection> forwards = new HashMap<>();
        forwards.put(ObisCode.fromString("1.0.84.8.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.84.8.0.255")));
        forwards.put(ObisCode.fromString("1.0.85.8.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.85.8.0.255")));
        forwards.put(ObisCode.fromString("1.0.86.9.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.86.9.0.255")));
        forwards.put(ObisCode.fromString("1.0.87.9.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.87.9.0.255")));

        for (Map.Entry<ObisCode, FlowDirection> obisCodeFlowDirectionEntry : forwards.entrySet()) {
            assertThat(obisCodeFlowDirectionEntry.getValue()).overridingErrorMessage("ObisCode " + obisCodeFlowDirectionEntry.getKey()
                    + " should have resulted in a '" + FlowDirection.LAGGING + "' flowdirection but was '" + obisCodeFlowDirectionEntry.getValue() + "'")
                    .isEqualTo(FlowDirection.LAGGING);
        }
    }

    @Test
    public void nonElectricityLaggingTest() {
        ObisCode obisCode = ObisCode.fromString("123.0.84.8.0.255");
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);

        assertThat(flowDirection).isNotEqualTo(FlowDirection.LAGGING);
        assertThat(flowDirection).isEqualTo(FlowDirection.NOTAPPLICABLE);
    }

    @Test
    public void electricityNonLaggingForLaggingTest() {
        ObisCode obisCode = ObisCode.fromString("1.0.80.8.0.255");
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);

        assertThat(flowDirection).isNotEqualTo(FlowDirection.LAGGING);
    }

    @Test
    public void totalTest() {
        Map<ObisCode, FlowDirection> forwards = new HashMap<>();
        forwards.put(ObisCode.fromString("1.0.15.8.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.15.8.0.255")));
        forwards.put(ObisCode.fromString("1.0.35.8.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.35.8.0.255")));
        forwards.put(ObisCode.fromString("1.0.55.9.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.55.9.0.255")));
        forwards.put(ObisCode.fromString("1.0.75.9.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.75.9.0.255")));

        for (Map.Entry<ObisCode, FlowDirection> obisCodeFlowDirectionEntry : forwards.entrySet()) {
            assertThat(obisCodeFlowDirectionEntry.getValue()).overridingErrorMessage("ObisCode " + obisCodeFlowDirectionEntry.getKey()
                    + " should have resulted in a '" + FlowDirection.TOTAL + "' flowdirection but was '" + obisCodeFlowDirectionEntry.getValue() + "'")
                    .isEqualTo(FlowDirection.TOTAL);
        }
    }

    @Test
    public void nonElectricityTotalTest() {
        ObisCode obisCode = ObisCode.fromString("123.0.35.8.0.255");
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);

        assertThat(flowDirection).isNotEqualTo(FlowDirection.TOTAL);
        assertThat(flowDirection).isEqualTo(FlowDirection.NOTAPPLICABLE);
    }

    @Test
    public void electricityNonTotalForTotalTest() {
        ObisCode obisCode = ObisCode.fromString("1.0.36.8.0.255");
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);

        assertThat(flowDirection).isNotEqualTo(FlowDirection.TOTAL);
    }

    @Test
    public void netTest() {
        Map<ObisCode, FlowDirection> forwards = new HashMap<>();
        forwards.put(ObisCode.fromString("1.0.16.8.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.16.8.0.255")));
        forwards.put(ObisCode.fromString("1.0.36.8.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.36.8.0.255")));
        forwards.put(ObisCode.fromString("1.0.56.9.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.56.9.0.255")));
        forwards.put(ObisCode.fromString("1.0.76.9.0.255"), FlowDirectionMapping.getFlowDirectionFor(ObisCode.fromString("1.0.76.9.0.255")));

        for (Map.Entry<ObisCode, FlowDirection> obisCodeFlowDirectionEntry : forwards.entrySet()) {
            assertThat(obisCodeFlowDirectionEntry.getValue()).overridingErrorMessage("ObisCode " + obisCodeFlowDirectionEntry.getKey()
                    + " should have resulted in a '" + FlowDirection.NET + "' flowdirection but was '" + obisCodeFlowDirectionEntry.getValue() + "'")
                    .isEqualTo(FlowDirection.NET);
        }
    }

    @Test
    public void nonElectricityNetTest() {
        ObisCode obisCode = ObisCode.fromString("123.0.36.8.0.255");
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);

        assertThat(flowDirection).isNotEqualTo(FlowDirection.NET);
        assertThat(flowDirection).isEqualTo(FlowDirection.NOTAPPLICABLE);
    }

    @Test
    public void electricityNonNetForNetTest() {
        ObisCode obisCode = ObisCode.fromString("1.0.37.8.0.255");
        FlowDirection flowDirection = FlowDirectionMapping.getFlowDirectionFor(obisCode);

        assertThat(flowDirection).isNotEqualTo(FlowDirection.NET);
    }
}
