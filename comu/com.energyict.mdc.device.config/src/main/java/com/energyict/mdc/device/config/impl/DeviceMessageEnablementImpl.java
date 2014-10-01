package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 9/30/14
 * Time: 1:36 PM
 */
public abstract class DeviceMessageEnablementImpl implements DeviceMessageEnablement {

    private static final String SINGLEDEVICEMESSAGEENABLEMENT = "0";
    private static final String DEVICEMESSAGECATEGORYENABLEMENT = "1";

    static final Map<String, Class<? extends DeviceMessageEnablement>> IMPLEMENTERS = new HashMap<>();

    static {
        IMPLEMENTERS.put(SINGLEDEVICEMESSAGEENABLEMENT, SingleDeviceMessageEnablement.class);
        IMPLEMENTERS.put(DEVICEMESSAGECATEGORYENABLEMENT, DeviceMessageCategoryEnablement.class);
    }

    static class DeviceMessageUserActionRecord {

        private DeviceMessageUserAction userAction;
        private Reference<DeviceMessageEnablement> deviceMessageEnablement = ValueReference.absent();

        DeviceMessageUserActionRecord() {
        }

        DeviceMessageUserActionRecord(DeviceMessageEnablement deviceMessageEnablement, DeviceMessageUserAction userAction) {
            this.deviceMessageEnablement.set(deviceMessageEnablement);
            this.userAction = userAction;
        }
    }

    private List<DeviceMessageUserActionRecord> deviceMessageUserActionRecords = new ArrayList<>();

    private Set<DeviceMessageUserAction> deviceMessageUserActions = EnumSet.of(
            DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1,
            DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2,
            DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3);

    private int deviceMessageCategoryId;
    private DeviceMessageCategory deviceMessageCategory;
    private DeviceMessageId deviceMessageId;

    @Override
    public Set<DeviceMessageUserAction> getUserActions() {
        return deviceMessageUserActions;
    }

    @Override
    public boolean isCategory() {
        return false;
    }

    @Override
    public boolean isSpecificMessage() {
        return false;
    }

    @Override
    public DeviceMessageCategory getDeviceMessageCategory() {
        if(this.deviceMessageCategory == null && this.deviceMessageCategoryId != 0){
//            this.deviceMessageCategory =
        }
        return null;
    }

    @Override
    public DeviceMessageId getDeviceMessageId() {
        return deviceMessageId;
    }
}
