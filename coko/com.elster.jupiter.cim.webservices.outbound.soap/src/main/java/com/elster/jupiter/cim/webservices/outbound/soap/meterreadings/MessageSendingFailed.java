/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.outbound.soap.meterreadings;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class MessageSendingFailed extends LocalizedException {

    public MessageSendingFailed(Thesaurus thesaurus, MessageSeed messageSeed, String endPointConfigurationName) {
        super(thesaurus, messageSeed, endPointConfigurationName);
    }
}
