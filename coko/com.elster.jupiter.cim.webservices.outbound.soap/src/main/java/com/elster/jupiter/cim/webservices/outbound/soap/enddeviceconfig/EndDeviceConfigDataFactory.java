/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.enddeviceconfig;

import com.elster.jupiter.metering.DefaultState;
import com.elster.jupiter.metering.EndDeviceAttributesProvider;
import com.elster.jupiter.metering.LifecycleDates;

import ch.iec.tc57._2011.enddeviceconfig.EndDevice;
import ch.iec.tc57._2011.enddeviceconfig.EndDeviceConfig;
import ch.iec.tc57._2011.enddeviceconfig.LifecycleDate;
import ch.iec.tc57._2011.enddeviceconfig.Name;
import ch.iec.tc57._2011.enddeviceconfig.NameType;
import ch.iec.tc57._2011.enddeviceconfig.Status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

class EndDeviceConfigDataFactory {
    private static final String END_DEVICE_NAME_TYPE = "EndDevice";

    EndDeviceConfig asEndDevice(com.elster.jupiter.metering.EndDevice endDevice, String state, Instant effectiveDate, List<EndDeviceAttributesProvider> endDeviceAttributesProviders) {
        EndDeviceConfig endDeviceConfig = new EndDeviceConfig();
        EndDevice cimEndDevice = createEndDevice(endDevice);
        cimEndDevice.setLifecycle(createLifecycleDate(endDevice));
        cimEndDevice.setType(endDeviceAttributesProviders.stream().map(e -> e.getType(endDevice)).filter(Optional::isPresent).map(Optional::get).findFirst().orElse(null));
        cimEndDevice.setStatus(createStatus(DefaultState.fromKey(state).map(DefaultState::getDefaultFormat).orElse(state), effectiveDate));
        endDeviceConfig.getEndDevice().add(cimEndDevice);
        return endDeviceConfig;
    }

    private EndDevice createEndDevice(com.elster.jupiter.metering.EndDevice endDevice) {
        EndDevice cimEndDevice = new EndDevice();
        cimEndDevice.getNames().add(createName(endDevice.getName()));
        cimEndDevice.setMRID(endDevice.getMRID());
        cimEndDevice.setSerialNumber(endDevice.getSerialNumber());
        return cimEndDevice;
    }

    private Name createName(String name) {
        Name nameBean = new Name();
        nameBean.setName(name);
        NameType nameType = new NameType();
        nameType.setName(END_DEVICE_NAME_TYPE);
        nameBean.setNameType(nameType);
        return nameBean;
    }

    private LifecycleDate createLifecycleDate(com.elster.jupiter.metering.EndDevice endDevice) {
        LifecycleDate lifecycleDate = new LifecycleDate();
        LifecycleDates lifecycleDates = endDevice.getLifecycleDates();
        lifecycleDates.getInstalledDate().ifPresent(lifecycleDate::setInstallationDate);
        lifecycleDates.getReceivedDate().ifPresent(lifecycleDate::setReceivedDate);
        return lifecycleDate;
    }

    private Status createStatus(String state, Instant effectiveDate) {
        Status status = new Status();
        status.setValue(state);
        status.setDateTime(effectiveDate);
        return status;
    }
}