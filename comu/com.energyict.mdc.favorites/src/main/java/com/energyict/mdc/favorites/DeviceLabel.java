package com.energyict.mdc.favorites;

import java.time.Instant;

import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.Device;

public interface DeviceLabel {

    Device getDevice();
    
    User getUser();
    
    LabelCategory getLabelCategory();
    
    String getComment();
    
    Instant getCreationDate();
    
}
