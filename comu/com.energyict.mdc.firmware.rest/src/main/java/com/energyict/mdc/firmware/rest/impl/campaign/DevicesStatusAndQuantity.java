/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl.campaign;

import com.elster.jupiter.rest.util.IdWithNameInfo;

public class DevicesStatusAndQuantity {

    public IdWithNameInfo status;
    public long quantity;

    public DevicesStatusAndQuantity() {
        //for json
    }

    public DevicesStatusAndQuantity(IdWithNameInfo status, long quantity) {
        this.status = status;
        this.quantity = quantity;
    }
}
