/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;


import com.elster.jupiter.cbo.FlowDirection;

public class FlowDirectionAdapter extends MapBasedXmlAdapter<FlowDirection> {

    public FlowDirectionAdapter() {
        register("", FlowDirection.NOTAPPLICABLE);
        register("Not applicable", FlowDirection.NOTAPPLICABLE);
        register("Total", FlowDirection.TOTAL);
        register("Total by phase", FlowDirection.TOTALBYPHASE);
        register("Forward", FlowDirection.FORWARD);
        register("Reverse", FlowDirection.REVERSE);
        register("Lagging", FlowDirection.LAGGING);
        register("Leading", FlowDirection.LEADING);
        register("Net", FlowDirection.NET);
        register("Q1 only", FlowDirection.Q1);
        register("Q1 - Q4", FlowDirection.Q1MINUSQ4);
        register("Q1 + Q2", FlowDirection.Q1PLUSQ2);
        register("Q1 + Q3", FlowDirection.Q1PLUSQ3);
        register("Q1 + Q4", FlowDirection.Q1PLUQQ4);
        register("Q2 only", FlowDirection.Q2);
        register("Q2 - Q3", FlowDirection.Q2MINUSQ3);
        register("Q2 + Q3", FlowDirection.Q2PLUSQ3);
        register("Q2 + Q4", FlowDirection.Q2PLUSQ4);
        register("Q3 only", FlowDirection.Q3);
        register("Q3 - Q2", FlowDirection.Q3MINUSQ2);
        register("Q3 + Q4", FlowDirection.Q3PLUSQ4);
        register("Q4 only", FlowDirection.Q4);
    }
}
