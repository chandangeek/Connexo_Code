package com.energyict.mdc.device.data.impl.crlrequest;

import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.tasks.RecurrentTask;
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
    public Optional<CrlRequestTaskProperty> findCrlRequestTaskProperties() {
        List<CrlRequestTaskPropertyImpl> crlRequestTaskPropertyList = deviceDataModelService.dataModel().mapper(CrlRequestTaskPropertyImpl.class).find();
        return crlRequestTaskPropertyList.isEmpty() ? Optional.empty() : Optional.of(crlRequestTaskPropertyList.get(0));
    }

    @Override
    public void createCrlRequestTaskProperties(RecurrentTask recurrentTask, SecurityAccessor securityAccessor, String caName) {
        CrlRequestTaskProperty crlRequestTaskProperty = deviceDataModelService.dataModel().getInstance(CrlRequestTaskPropertyImpl.class);
        crlRequestTaskProperty.setRecurrentTask(recurrentTask);
        crlRequestTaskProperty.setSecurityAccessor(securityAccessor);
        crlRequestTaskProperty.setCaName(caName);
        crlRequestTaskProperty.save();
    }

    @Override
    public void updateCrlRequestTaskProperties(RecurrentTask recurrentTask, SecurityAccessor securityAccessor, String caName) {
        if (findCrlRequestTaskProperties().isPresent()) {
            CrlRequestTaskProperty crlRequestTaskProperty = findCrlRequestTaskProperties().get();
            if (crlRequestTaskProperty.getRecurrentTask().getId() == recurrentTask.getId()) {
                crlRequestTaskProperty.setSecurityAccessor(securityAccessor);
                crlRequestTaskProperty.setCaName(caName);
                crlRequestTaskProperty.update();
            }
        }

    }

    @Override
    public void deleteCrlRequestTaskProperties() {
        if (findCrlRequestTaskProperties().isPresent()) {
            findCrlRequestTaskProperties().get().delete();
        }
    }

}
