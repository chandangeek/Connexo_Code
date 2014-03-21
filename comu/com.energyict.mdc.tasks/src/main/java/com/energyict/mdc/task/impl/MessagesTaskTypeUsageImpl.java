package com.energyict.mdc.task.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.task.DeviceMessageService;
import com.energyict.mdc.task.ProtocolTask;

/**
 * Implementation of a {@link com.energyict.mdc.task.impl.MessagesTaskTypeUsage} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 26/02/13
 * Time: 14:38
 */
public class MessagesTaskTypeUsageImpl implements MessagesTaskTypeUsage {

    enum Fields {
        PROTOCOL_TASK("protocolTask"),
        DEVICE_MESSAGE_SPEC("deviceMessageSpec"),
        DEVICE_MESSAGE_CATEGORY("deviceMessageCategory");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private long id;
    private Reference<ProtocolTask> protocolTask = ValueReference.absent();
    private String deviceMessageSpec;
    private String deviceMessageCategory;

    public MessagesTaskTypeUsageImpl() {
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public ProtocolTask getProtocolTask() {
        return protocolTask.get();
    }

    @Override
    public void setProtocolTask(ProtocolTask protocolTask) {
        this.protocolTask.set(protocolTask);
    }

    @Override
    public void setDeviceMessageSpec(DeviceMessageSpec deviceMessageSpec) {
        this.deviceMessageSpec = deviceMessageSpec.getPrimaryKey().getValue();
    }

    @Override
    public void setDeviceMessageCategory(DeviceMessageCategory deviceMessageCategory) {
        this.deviceMessageCategory = deviceMessageCategory.getPrimaryKey().getValue();
    }

    @Override
    public DeviceMessageSpec getDeviceMessageSpec() {
        return getDeviceMessageService().findDeviceMessageSpec(deviceMessageSpec);
    }

    @Override
    public DeviceMessageCategory getDeviceMessageCategory() {
        return getDeviceMessageService().findDeviceMessageCategory(deviceMessageCategory);
    }

    private DeviceMessageService getDeviceMessageService() {
        return Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DeviceMessageService.class).get(0);
    }


}
