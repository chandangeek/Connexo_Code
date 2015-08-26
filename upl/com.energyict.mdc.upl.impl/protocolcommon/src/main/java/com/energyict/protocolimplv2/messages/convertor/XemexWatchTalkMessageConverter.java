package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.*;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.OneTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.LoadProfileRegisterRequestMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.special.PartialLoadProfileMessageEntry;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Created by cisac on 8/6/2015.
 */
public class XemexWatchTalkMessageConverter extends Dsmr23MessageConverter{

    /**
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>(Dsmr23MessageConverter.registry);

    @Override
    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }

    static {
        registry.remove(ContactorDeviceMessage.CONTACTOR_OPEN);
        registry.remove(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        registry.remove(ContactorDeviceMessage.CONTACTOR_CLOSE);
        registry.remove(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);
        registry.remove(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE);
        registry.remove(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL);
        registry.remove(SecurityMessage.CHANGE_PASSWORD_WITH_NEW_PASSWORD);
        registry.remove(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM);
        registry.remove(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP);
        registry.remove(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS);
        registry.remove(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS);
        registry.remove(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST);
        registry.remove(DeviceActionMessage.GLOBAL_METER_RESET);
        registry.remove(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS);
        registry.remove(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS);
        registry.remove(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION);
        registry.remove(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST);
        registry.remove(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST);
        registry.remove(MBusSetupDeviceMessage.Commission_With_Channel);
        registry.remove(ConfigurationChangeDeviceMessage.ChangeDefaultResetWindow);

    }

    public XemexWatchTalkMessageConverter(){
        super();
    }

}
