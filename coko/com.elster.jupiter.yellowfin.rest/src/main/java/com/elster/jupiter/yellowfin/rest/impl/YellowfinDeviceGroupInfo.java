/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.rest.impl;


public class YellowfinDeviceGroupInfo {
    public String name;
    public Boolean dynamic;

    public YellowfinDeviceGroupInfo() {
    }
    public YellowfinDeviceGroupInfo(String name, Boolean dynamic) {
        this.name = name;
        this.dynamic = dynamic;
    }
}
