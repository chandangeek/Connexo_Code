/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

public class ConnectionMethodInfoFactory {

    private final MdcPropertyUtils mdcPropertyUtils;
    private final Thesaurus thesaurus;

    @Inject
    public ConnectionMethodInfoFactory(MdcPropertyUtils mdcPropertyUtils, Thesaurus thesaurus) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.thesaurus = thesaurus;
    }

    public ConnectionMethodInfo<?> asInfo(PartialConnectionTask partialConnectionTask, UriInfo uriInfo) {
        if (PartialInboundConnectionTask.class.isAssignableFrom(partialConnectionTask.getClass())) {
            ConnectionMethodInfo connectionMethodInfo =  new InboundConnectionMethodInfo((PartialInboundConnectionTask) partialConnectionTask, uriInfo, mdcPropertyUtils);
            connectionMethodInfo.displayDirection = TranslationKeys.INBOUND.translateWith(thesaurus);
            return connectionMethodInfo;
        } else if (PartialScheduledConnectionTask.class.isAssignableFrom(partialConnectionTask.getClass())) {
            ConnectionMethodInfo connectionMethodInfo =  new ScheduledConnectionMethodInfo((PartialScheduledConnectionTask) partialConnectionTask, uriInfo, mdcPropertyUtils);
            connectionMethodInfo.displayDirection = TranslationKeys.OUTBOUND.translateWith(thesaurus);
            return connectionMethodInfo;
        } else {
            throw new IllegalArgumentException("Unsupported ConnectionMethod type "+partialConnectionTask.getClass().getSimpleName());
        }
    }
}
