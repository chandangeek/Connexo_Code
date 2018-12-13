/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.favorites;

import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.Device;

import java.time.Instant;

public interface DeviceLabel {

    Device getDevice();
    
    User getUser();
    
    LabelCategory getLabelCategory();
    
    String getComment();
    
    Instant getCreationDate();
    
}
