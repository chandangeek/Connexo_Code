/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.energyict.mdc.io.FlowControl;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class FlowControlInfo {

    @XmlJavaTypeAdapter(value = FlowControlAdapter.class)
    public FlowControl id;
    public String localizedValue;

    public FlowControlInfo() {}

    public FlowControlInfo(FlowControl flowControl) {
        this.id = flowControl;
        // the localizedValue value should be overwritten by the actually translated value in the correspondent factory class
        if(flowControl != null) {
            this.localizedValue = new FlowControlAdapter().marshal(flowControl);
        }
    }

}
