/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.device.data.DeviceService;
import org.apache.commons.lang3.math.NumberUtils;

import javax.inject.Inject;

public class DeviceMessageServiceCallHandler implements ServiceCallHandler {

    public static final String VERSION = "v1.0";
    public static final String SERVICE_CALL_HANDLER_NAME = "DeviceMessageServiceCallHandler";
    public static final String APPLICATION = "MDC";
    private final DeviceService deviceService;

    @Inject
    public DeviceMessageServiceCallHandler(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());

        if (newState == DefaultState.CANCELLED || newState == DefaultState.FAILED || newState == DefaultState.REJECTED) {
            serviceCall.log(LogLevel.FINE, "Trying to revoke related device message");
            revokeRelatedDeviceMessage(serviceCall);
        }
    }

    private void revokeRelatedDeviceMessage(ServiceCall serviceCall) {
        ServiceCall parentServiceCall = serviceCall.getParent()
                .orElseThrow(() -> new IllegalStateException("Unable to get parent service call for service call"));
        SubParentGetMeterReadingsDomainExtension extension = parentServiceCall.getExtension(SubParentGetMeterReadingsDomainExtension.class)
                .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
        String mrid = extension.getEndDeviceMrid();
        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(() -> new IllegalStateException("Unable to get device by its mrid " + mrid));
        DeviceMessage deviceMessage = device.getMessages().stream()
                .filter(dm -> serviceCall.getId() == NumberUtils.toLong(dm.getTrackingId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unable to find device message for service call with id:" + serviceCall
                        .getId()));
        serviceCall.log(LogLevel.FINE, String.format("Device message '%s'(id: %d, release date: %s) is revoked",
                deviceMessage.getSpecification().getName(), deviceMessage.getId(), deviceMessage.getReleaseDate()));

        deviceMessage.revoke();
    }
}