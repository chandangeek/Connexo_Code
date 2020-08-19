/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.outbound.soap.meterreadings;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MessageSendingFailed extends LocalizedException {
    public MessageSendingFailed(Thesaurus thesaurus, EndPointConfiguration... endPointConfigurations) {
        super(thesaurus, MessageSeeds.MESSAGE_SENDING_FAILED,
                Arrays.stream(endPointConfigurations).map(EndPointConfiguration::getName).map(name -> "'" + name + "'").sorted().collect(Collectors.joining(", ")));
    }
}
