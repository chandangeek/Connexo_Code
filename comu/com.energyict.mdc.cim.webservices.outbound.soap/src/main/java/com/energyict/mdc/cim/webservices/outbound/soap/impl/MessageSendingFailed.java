/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class MessageSendingFailed extends LocalizedException {

    public MessageSendingFailed(Thesaurus thesaurus, MessageSeed messageSeed, String endPointConfigurationName) {
        super(thesaurus, messageSeed, endPointConfigurationName);
    }
}
