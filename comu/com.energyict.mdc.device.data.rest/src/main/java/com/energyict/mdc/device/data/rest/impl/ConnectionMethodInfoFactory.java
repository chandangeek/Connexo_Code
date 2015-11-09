package com.energyict.mdc.device.data.rest.impl;

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

    @Inject
    public ConnectionMethodInfoFactory(MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    public ConnectionMethodInfo<?> asInfo(ConnectionTask<?,?> connectionTask, UriInfo uriInfo) {
        if (InboundConnectionTask.class.isAssignableFrom(connectionTask.getClass())) {
            return new InboundConnectionMethodInfo((InboundConnectionTask) connectionTask, uriInfo, mdcPropertyUtils);
        } else if (ScheduledConnectionTask.class.isAssignableFrom(connectionTask.getClass())) {
            return new ScheduledConnectionMethodInfo((ScheduledConnectionTask) connectionTask, uriInfo, mdcPropertyUtils);
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
