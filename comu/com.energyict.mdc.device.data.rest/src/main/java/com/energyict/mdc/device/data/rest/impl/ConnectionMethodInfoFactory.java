/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

public class ConnectionMethodInfoFactory {

    private final MdcPropertyUtils mdcPropertyUtils;
    private final Thesaurus thesaurus;

    @Inject
    public ConnectionMethodInfoFactory(MdcPropertyUtils mdcPropertyUtils, Thesaurus thesaurus) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.thesaurus = thesaurus;
    }

    public ConnectionMethodInfo<?> asInfo(ConnectionTask<?,?> connectionTask, UriInfo uriInfo) {
        if (InboundConnectionTask.class.isAssignableFrom(connectionTask.getClass())) {
            ConnectionMethodInfo connectionMethodInfo = new InboundConnectionMethodInfo((InboundConnectionTask) connectionTask, uriInfo, mdcPropertyUtils);
            connectionMethodInfo.displayDirection = DefaultTranslationKey.INBOUND.translateWith(thesaurus);
            return connectionMethodInfo;
        } else if (ScheduledConnectionTask.class.isAssignableFrom(connectionTask.getClass())) {
            ConnectionMethodInfo connectionMethodInfo = new ScheduledConnectionMethodInfo((ScheduledConnectionTask) connectionTask, uriInfo, mdcPropertyUtils, thesaurus);
            connectionMethodInfo.displayDirection = DefaultTranslationKey.OUTBOUND.translateWith(thesaurus);
            return connectionMethodInfo;
        } else {
            throw new IllegalArgumentException("Unsupported ConnectionMethod type "+connectionTask.getClass().getSimpleName());
        }
    }

    public List<ConnectionMethodInfo<?>> asInfoList(List<ConnectionTask<?,?>> connectionTaskList, UriInfo uriInfo) {
        List<ConnectionMethodInfo<?>> infos = new ArrayList<>();
        for (ConnectionTask<?, ?> connectionTask : connectionTaskList) {
            infos.add(asInfo(connectionTask, uriInfo));
        }
        return infos;
    }
}
