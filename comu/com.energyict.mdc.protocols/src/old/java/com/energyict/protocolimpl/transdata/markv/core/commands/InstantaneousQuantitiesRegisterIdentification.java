/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * InstantaneousQuantitiesRegisterIdentification.java
 *
 * Created on 2 september 2005, 16:53
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Koen
 */
public class InstantaneousQuantitiesRegisterIdentification {

    static List<RegisterDataId> list = new ArrayList<>();
    static {
        list.add(new RegisterDataId(RegisterDataId.POWER_FACTOR3,RegisterDataId.INT,71,1, 0, 0, "Instantaneous value register power factor all phases"));
        list.add(new RegisterDataId(RegisterDataId.VOLTAGE_A,RegisterDataId.INT,505,1, 0, 0, "Instantaneous value register voltage phase A"));
        list.add(new RegisterDataId(RegisterDataId.VOLTAGE_B,RegisterDataId.INT,506,1, 0, 0, "Instantaneous value register voltage phase B"));
        list.add(new RegisterDataId(RegisterDataId.VOLTAGE_C,RegisterDataId.INT,507,1, 0, 0, "Instantaneous value register voltage phase C"));
        list.add(new RegisterDataId(RegisterDataId.AMPERE_A,RegisterDataId.INT,509,1, 0, 0, "Instantaneous value register current phase A"));
        list.add(new RegisterDataId(RegisterDataId.AMPERE_B,RegisterDataId.INT,510,1, 0, 0, "Instantaneous value register current phase B"));
        list.add(new RegisterDataId(RegisterDataId.AMPERE_C,RegisterDataId.INT,511,1, 0, 0, "Instantaneous value register current phase C"));
        list.add(new RegisterDataId(RegisterDataId.WATT3,RegisterDataId.INT,512,1, 0, 0, "Instantaneous value register WATT all phases"));
        list.add(new RegisterDataId(RegisterDataId.VAR3,RegisterDataId.INT,516,1, 0, 0, "Instantaneous value register var all phases"));
    }

    public static List<RegisterDataId> getRegisterDataIds() {
        return list;
    }

}