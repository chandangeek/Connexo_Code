package com.energyict.mdc.device.configuration.rest.impl;


import com.elster.jupiter.cbo.FlowDirection;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

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
        register("Q1", FlowDirection.Q1);
        register("Q1 minus Q4", FlowDirection.Q1MINUSQ4);
        register("Q1 plus Q2", FlowDirection.Q1PLUSQ2);
        register("Q1 plus Q3", FlowDirection.Q1PLUSQ3);
        register("Q1 plus Q4", FlowDirection.Q1PLUQQ4);
        register("Q2", FlowDirection.Q2);
        register("Q2 minus Q3", FlowDirection.Q2MINUSQ3);
        register("Q2 plus Q3", FlowDirection.Q2PLUSQ3);
        register("Q2 plus Q4", FlowDirection.Q2PLUSQ4);
        register("Q3", FlowDirection.Q3);
        register("Q3 minus Q2", FlowDirection.Q3MINUSQ2);
        register("Q3 plus Q4", FlowDirection.Q3PLUSQ4);
        register("Q4", FlowDirection.Q4);
    }
}
