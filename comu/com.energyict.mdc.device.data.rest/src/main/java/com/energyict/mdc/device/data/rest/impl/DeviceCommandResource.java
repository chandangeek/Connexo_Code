package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.tasks.ComTask;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 10/22/14.
 */
public class DeviceCommandResource {
    private final ResourceHelper resourceHelper;
    private final DeviceMessageInfoFactory deviceMessageInfoFactory;

    @Inject
    public DeviceCommandResource(ResourceHelper resourceHelper, DeviceMessageInfoFactory deviceMessageInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.deviceMessageInfoFactory = deviceMessageInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getDeviceCommands(@PathParam("mRID") String mrid, @BeanParam QueryParameters queryParameters) {



        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);

        List<ComTask> comTasks = device.getDeviceConfiguration().
                getComTaskEnablements().stream().
                map(ComTaskEnablement::getComTask).

//                map(ComTask::getProtocolTasks).
//                filter(task -> task instanceof MessagesTask).
//                map(task -> ((MessagesTask)task).getDeviceMessageCategories()).
//                ;
//
        device.getComTaskExecutions().stream().map(ComTaskExecution::)

        List<DeviceMessageInfo> infos = device.getMessages().stream().
                sorted((c1, c2) -> c2.getReleaseDate().compareTo(c1.getReleaseDate())).
                map(deviceMessageInfoFactory::asInfo).
                collect(toList());
        return PagedInfoList.asJson("commands", infos, queryParameters);
    }
}
