/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.messaging;

import com.energyict.mdc.protocol.api.messaging.AdvancedMessaging;

/**
 * This interface is intended to be implemented by protocols that support requesting Partial LoadProfiles
 */
public interface LoadProfileRegisterMessaging extends AdvancedMessaging {

    LegacyLoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder();

}