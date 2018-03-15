package com.energyict.mdc.device.data.impl.crlrequest;

import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;

import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;


public class CrlRequestTaskServiceImpl implements CrlRequestTaskService {
    private volatile DeviceDataModelService deviceDataModelService;

    @Inject
    public CrlRequestTaskServiceImpl(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
    }

    @Override
    public CrlRequestTaskProperty newCrlRequestTaskProperties() {
        return deviceDataModelService.dataModel().getInstance(CrlRequestTaskProperty.class);
    }

    @Override
    public CrlRequestTaskProperty findCrlRequestTaskProperties() {
        return deviceDataModelService.dataModel().mapper(CrlRequestTaskProperty.class).find().get(0);
    }


}
