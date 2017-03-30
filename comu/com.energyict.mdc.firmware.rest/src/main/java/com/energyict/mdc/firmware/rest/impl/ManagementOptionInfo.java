/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

public class ManagementOptionInfo extends IdWithLocalizedValue<String>{
    public ManagementOptionInfo() {
        super();
    }

    public ManagementOptionInfo(String id, String localizedValue)  {
        super(id, localizedValue);
    }
}
