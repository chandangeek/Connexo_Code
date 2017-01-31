/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;

enum MailDeviceMessageAttributes implements TranslationKey {

    SetPOPUsernameAttributeName(DeviceMessageConstants.SetPOPUsernameAttributeName, "Set POPUsername"),
    SetPOPPasswordAttributeName(DeviceMessageConstants.SetPOPPasswordAttributeName, "Set POPPassword"),
    SetPOPHostAttributeName(DeviceMessageConstants.SetPOPHostAttributeName, "Set POPHost"),
    SetPOPReadMailEveryAttributeName(DeviceMessageConstants.SetPOPReadMailEveryAttributeName, "Set POPReadMailEvery"),
    SetPOP3OptionsAttributeName(DeviceMessageConstants.SetPOP3OptionsAttributeName, "Set POP3Options"),
    SetSMTPFromAttributeName(DeviceMessageConstants.SetSMTPFromAttributeName, "Set SMTPFrom"),
    SetSMTPToAttributeName(DeviceMessageConstants.SetSMTPToAttributeName, "Set SMTPTo"),
    SetSMTPConfigurationToAttributeName(DeviceMessageConstants.SetSMTPConfigurationToAttributeName, "Set SMTPConfigurationTo"),
    SetSMTPServerAttributeName(DeviceMessageConstants.SetSMTPServerAttributeName, "Set SMTPServer"),
    SetSMTPDomainAttributeName(DeviceMessageConstants.SetSMTPDomainAttributeName, "Set SMTPDomain"),
    SetSMTPSendMailEveryAttributeName(DeviceMessageConstants.SetSMTPSendMailEveryAttributeName, "Set SMTPSendMailEvery"),
    SetSMTPCurrentIntervalAttributeName(DeviceMessageConstants.SetSMTPCurrentIntervalAttributeName, "Set SMTPCurrentInterval"),
    SetSMTPDatabaseIDAttributeName(DeviceMessageConstants.SetSMTPDatabaseIDAttributeName, "Set SMTPDatabaseID"),
    SetSMTPOptionsAttributeName(DeviceMessageConstants.SetSMTPOptionsAttributeName, "Set SMTPOptions"),
    ;

    private final String key;
    private final String defaultFormat;

    MailDeviceMessageAttributes(String key, String defaultFormat) {
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