package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.LoadProfileMessage;
import com.energyict.protocolimplv2.messages.MBusSetupDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.*;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.LoadProfileRegisterRequestMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.PartialLoadProfileMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadProfileAttributeName;

/**
 * Created by cisac on 8/6/2015.
 */
public class XemexWatchTalkMbusMessageConverter extends Dsmr23MBusDeviceMessageConverter{

    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    protected static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>(Dsmr23MBusDeviceMessageConverter.registry);

    static {

        // Disconnect control
        registry.remove(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE);
        registry.remove(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);
        registry.remove(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        // MBus setup
        registry.remove(MBusSetupDeviceMessage.UseCorrectedValues);
        registry.remove(MBusSetupDeviceMessage.UseUncorrectedValues);
        // LoadProfiles
        registry.remove(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST);
        registry.remove(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST);

    }

    public XemexWatchTalkMbusMessageConverter() {
        super();
    }

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }

}
