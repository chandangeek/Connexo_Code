package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 10/3/14.
 */
public class ComSessionResource {
    private final ResourceHelper resourceHelper;
    private final ConnectionTaskService connectionTaskService;
    private final ComSessionInfoFactory comSessionInfoFactory;

    @Inject
    public ComSessionResource(ResourceHelper resourceHelper, ConnectionTaskService connectionTaskService, ComSessionInfoFactory comSessionInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.connectionTaskService = connectionTaskService;
        this.comSessionInfoFactory = comSessionInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ConnectionHistoryInfo getConnectionMethodHistory(@PathParam("mRID") String mrid, @PathParam("id") long connectionMethodId, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ConnectionTask<?, ?> connectionTask = resourceHelper.findConnectionTaskOrThrowException(device, connectionMethodId);
        ConnectionHistoryInfo info = new ConnectionHistoryInfo();
        info.connectionMethod = connectionTask.getName();
        List<ComSession> comSessions = connectionTaskService.findAllSessionsFor(connectionTask).stream().sorted((c1, c2) -> c1.getStartDate().compareTo(c2.getStartDate())).collect(toList());
        List<ComSessionInfo> comSessionsInPage = ListPager.of(comSessions).from(queryParameters).find().stream().map(comSessionInfoFactory::from).collect(toList());
        info.comSessionPage = PagedInfoList.asJson("comSessions", comSessionsInPage, queryParameters);
        return info;
    }
}

class ConnectionHistoryInfo {
    public String connectionMethod;
    public PagedInfoList comSessionPage;
}
