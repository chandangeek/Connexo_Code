package com.energyict.mdc.device.data.impl.crlrequest;

import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;

import javax.inject.Inject;
import java.util.List;


public class CrlRequestTaskServiceImpl implements CrlRequestTaskService {

    private final DeviceDataModelService deviceDataModelService;

    @Inject
    public CrlRequestTaskServiceImpl(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
    }

    @Override
    public CrlRequestTaskProperty newCrlRequestTaskProperty() {
        return deviceDataModelService.dataModel().getInstance(CrlRequestTaskProperty.class);
    }

    @Override
    public List<CrlRequestTaskProperty> findAllCrlRequestTaskProperties() {
        return deviceDataModelService.dataModel().mapper(CrlRequestTaskProperty.class).find();
    }


}
