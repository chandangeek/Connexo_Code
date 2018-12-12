/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.tasks.support;

import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;

/**
 * for marking DeviceMessageSupport classes using a {@link LegacyMessageConverter}
 */
public interface UsesLegacyMessageConverter {

    LegacyMessageConverter getLegacyMessageConverter();

}
