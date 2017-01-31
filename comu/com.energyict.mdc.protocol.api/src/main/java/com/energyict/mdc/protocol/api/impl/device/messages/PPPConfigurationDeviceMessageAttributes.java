/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

enum PPPConfigurationDeviceMessageAttributes implements TranslationKey {

    SetISP1PhoneAttributeName(DeviceMessageConstants.SetISP1PhoneAttributeName, "Set ISP1Phone"),
    SetISP1UsernameAttributeName(DeviceMessageConstants.SetISP1UsernameAttributeName, "Set ISP1Username"),
    SetISP1PasswordAttributeName(DeviceMessageConstants.SetISP1PasswordAttributeName, "Set ISP1Password"),
    SetISP1TriesAttributeName(DeviceMessageConstants.SetISP1TriesAttributeName, "Set ISP1Tries"),
    SetISP2PhoneAttributeName(DeviceMessageConstants.SetISP2PhoneAttributeName, "Set ISP2Phone"),
    SetISP2UsernameAttributeName(DeviceMessageConstants.SetISP2UsernameAttributeName, "Set ISP2Username"),
    SetISP2PasswordAttributeName(DeviceMessageConstants.SetISP2PasswordAttributeName, "Set ISP2Password"),
    SetISP2TriesAttributeName(DeviceMessageConstants.SetISP2TriesAttributeName, "Set ISP2Tries"),
    SetPPPIdleTimeoutAttributeName(DeviceMessageConstants.SetPPPIdleTimeoutAttributeName, "Set PPPIdleTimeout"),
    SetPPPRetryIntervalAttributeName(DeviceMessageConstants.SetPPPRetryIntervalAttributeName, "Set PPPRetryInterval"),
    SetPPPOptionsAttributeName(DeviceMessageConstants.SetPPPOptionsAttributeName, "Set PPPOptions"),
    SetPPPIdleTime(DeviceMessageConstants.SetPPPIdleTime, "set PPPIdleTime"),
    PPPDaemonResetThreshold(DeviceMessageConstants.PPPDaemonResetThreshold, "ppp DaemonResetThreshold"),
    ;

    private final String key;
    private final String defaultFormat;

    PPPConfigurationDeviceMessageAttributes(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

}