package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.Register;
import com.google.common.base.Optional;
import java.util.List;
import javax.inject.Inject;

import static com.elster.jupiter.util.conditions.Where.where;

public class ResourceHelper {

    private final DeviceDataService deviceDataService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ResourceHelper(DeviceDataService deviceDataService, ExceptionFactory exceptionFactory) {
        super();
        this.deviceDataService = deviceDataService;
        this.exceptionFactory = exceptionFactory;
    }

    public Device findDeviceByMrIdOrThrowException(String mRID) {
        Device device = deviceDataService.findByUniqueMrid(mRID);
        if (device == null) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE, mRID);
        }
        return device;
    }

    public Register findRegisterOrThrowException(Device device, long registerId) {
        List<Register> registers = device.getRegisters();
        for(Register register : registers) {
            Optional<RegisterSpec> registerSpecOptional = Optional.fromNullable(register.getRegisterSpec());
            if(registerSpecOptional.isPresent() && registerSpecOptional.get().getId() == registerId) {
                return register;
            }
        }

        throw exceptionFactory.newException(MessageSeeds.NO_SUCH_REGISTER, registerId);
    }

    public LoadProfile findLoadProfileOrThrowException(Device device, long loadProfileId, String mrid) {
        for (LoadProfile loadProfile : device.getLoadProfiles()) {
            if (loadProfile.getId()==loadProfileId) {
                return loadProfile;
            }
        }
        throw exceptionFactory.newException(MessageSeeds.NO_SUCH_LOAD_PROFILE_ON_DEVICE, mrid, loadProfileId);
    }

    public Channel findChannelOrThrowException(LoadProfile loadProfile, long channelId) {
        for (Channel channel : loadProfile.getChannels()) {
            if (channel.getChannelSpec().getId()==channelId) {
                return channel;
            }
        }
        throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CHANNEL_ON_LOAD_PROFILE, loadProfile.getId(), channelId);
    }

    public Condition getQueryConditionForDevice(StandardParametersBean params) {
        Condition condition = Condition.TRUE;
        if(params.getQueryParameters().size() > 0) {
            condition = condition.and(addDeviceQueryCondition(params));
        }
        return condition;
    }

    private Condition addDeviceQueryCondition(StandardParametersBean params) {
        Condition conditionDevice = Condition.TRUE;
        String mRID = params.getFirst("mRID");
        if (mRID != null) {
            conditionDevice =  !params.isRegExp()
                    ? conditionDevice.and(where("mRID").isEqualTo(mRID))
                    : conditionDevice.and(where("mRID").likeIgnoreCase(mRID));
        }
        String serialNumber = params.getFirst("serialNumber");
        if (serialNumber != null) {
            conditionDevice =  !params.isRegExp()
                    ? conditionDevice.and(where("serialNumber").isEqualTo(serialNumber))
                    : conditionDevice.and(where("serialNumber").likeIgnoreCase(serialNumber));
        }
        String deviceType = params.getFirst("deviceTypeName");
        if (deviceType != null) {
            conditionDevice = conditionDevice.and(createMultipleConditions(deviceType,"deviceConfiguration.deviceType.name"));
        }
        String deviceConfiguration = params.getFirst("deviceConfigurationName");
        if (deviceConfiguration != null) {
            conditionDevice = conditionDevice.and(createMultipleConditions(deviceConfiguration,"deviceConfiguration.name"));
        }
        return conditionDevice;
    }

    private Condition createMultipleConditions(String params, String conditionField) {
        Condition condition = Condition.FALSE;
        String[] values = params.split(",");
        for (String value : values) {
            condition = condition.or(where(conditionField).isEqualTo(value.trim()));
        }
        return condition;
    }
}
