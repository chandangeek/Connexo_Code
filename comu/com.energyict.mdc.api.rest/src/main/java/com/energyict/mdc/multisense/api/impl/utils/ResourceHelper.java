package com.energyict.mdc.multisense.api.impl.utils;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Created by bvn on 7/15/15.
 */
public class ResourceHelper {

    private final DeviceService deviceService;

    @Inject
    public ResourceHelper(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    public Device findDeviceByMrIdOrThrowException(String mrid) {
        return deviceService.findByUniqueMrid(mrid).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
    }
}
