/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DeviceFactory.java
 *
 * Created on 2 april 2007, 15:12
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Koen
 */
public class DeviceFactory {

    static List devices = new ArrayList();

    static {
        devices.add(new Device("Square D", "15210"));
        devices.add(new Device("GE", "PQM2"));
    }

    /** Creates a new instance of DeviceFactory */
    public DeviceFactory() {
    }

}
