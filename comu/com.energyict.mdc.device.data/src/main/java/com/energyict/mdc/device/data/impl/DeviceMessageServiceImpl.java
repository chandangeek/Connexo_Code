package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import javax.inject.Inject;
import java.util.Optional;

public class DeviceMessageServiceImpl implements DeviceMessageService {

    private final DeviceDataModelService deviceDataModelService;

    @Inject
    public DeviceMessageServiceImpl(DeviceDataModelService deviceDataModelService) {
        super();
        this.deviceDataModelService = deviceDataModelService;
    }

    @Override
    public Optional<DeviceMessage> findDeviceMessageById(long id) {
        return this.deviceDataModelService.dataModel().mapper(DeviceMessage.class).getOptional(id);
    }

    @Override
    public Optional<DeviceMessage> findAndLockDeviceMessageByIdAndVersion(long id, long version) {
        return this.deviceDataModelService.dataModel().mapper(DeviceMessage.class).lockObjectIfVersion(version, id);
    }
}
