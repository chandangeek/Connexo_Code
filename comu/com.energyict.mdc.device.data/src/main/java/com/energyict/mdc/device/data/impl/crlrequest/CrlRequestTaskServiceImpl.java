package com.energyict.mdc.device.data.impl.crlrequest;

import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;


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
    public Optional<CrlRequestTaskProperty> findCrlRequestTaskProperties() {
        List<CrlRequestTaskProperty> crlRequestTaskPropertyList = deviceDataModelService.dataModel().mapper(CrlRequestTaskProperty.class).find();
        return crlRequestTaskPropertyList.isEmpty() ? Optional.empty() : Optional.of(crlRequestTaskPropertyList.get(0));
    }


}
