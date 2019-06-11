/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

public class DevicesStatusAndQuantity {

    public String status;
    public long quantity;

    public DevicesStatusAndQuantity() {
        //for json
    }

    public DevicesStatusAndQuantity(String status, long quantity) {
        this.status = status;
        this.quantity = quantity;
    }
}
