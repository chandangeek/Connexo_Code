/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * A3.java
 *
 * Created on 11 Februari 2006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.meteridentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class A3 extends AbstractManufacturer {

    /** Creates a new instance of KV2 */
    public A3() {
    }

    public String getManufacturer() throws IOException {
        return "Elster Metering";
    }

    public String getMeterProtocolClass() throws IOException {
        return "com.energyict.protocolimpl.elster.a3.AlphaA3";
    }

    public String[] getMeterSerialNumberRegisters() throws IOException {
        return null;
    }

    public String getMeterDescription() throws IOException {
        return "Elster Metering Alpha A3";
    }
}
