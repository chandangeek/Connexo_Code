package com.energyict.protocols.messaging;

import com.energyict.mdc.protocol.api.messaging.AdvancedMessaging;
import com.energyict.protocols.messaging.LegacyLoadProfileRegisterMessageBuilder;

/**
 * This interface is intended to be implemented by protocols that support requesting Partial LoadProfiles
 */
public interface LoadProfileRegisterMessaging extends AdvancedMessaging {

    LegacyLoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder();

}