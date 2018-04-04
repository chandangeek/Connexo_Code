package com.energyict.mdc.device.data.impl.crlrequest;

import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.tasks.RecurrentTask;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskPropertiesService;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class CrlRequestTaskPropertiesServiceImpl implements CrlRequestTaskPropertiesService {
    private volatile DeviceDataModelService deviceDataModelService;

    @Inject
    public CrlRequestTaskPropertiesServiceImpl(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
    }

    @Override
    public List<CrlRequestTaskProperty> findCrlRequestTaskProperties() {
        List<CrlRequestTaskProperty> crlRequestTaskProperties = new ArrayList<>();
        List<CrlRequestTaskPropertyImpl> crlRequestTaskPropertyList = deviceDataModelService.dataModel().mapper(CrlRequestTaskPropertyImpl.class).find();
        crlRequestTaskPropertyList.forEach(crlRequestTaskProperty -> crlRequestTaskProperties.add(crlRequestTaskProperty));
        return crlRequestTaskProperties;
    }

    @Override
    public void createCrlRequestTaskPropertiesForCa(RecurrentTask recurrentTask, SecurityAccessor securityAccessor, String caName) {
        CrlRequestTaskProperty crlRequestTaskProperty = deviceDataModelService.dataModel().getInstance(CrlRequestTaskPropertyImpl.class);
        crlRequestTaskProperty.setRecurrentTask(recurrentTask);
        crlRequestTaskProperty.setSecurityAccessor(securityAccessor);
        crlRequestTaskProperty.setCaName(caName);
        crlRequestTaskProperty.save();
    }

    @Override
    public void updateCrlRequestTaskPropertiesForCa(RecurrentTask recurrentTask, SecurityAccessor securityAccessor, String caName) {
        CrlRequestTaskProperty property = deviceDataModelService.dataModel().mapper(CrlRequestTaskPropertyImpl.class).lock(recurrentTask.getId());
        property.setRecurrentTask(recurrentTask);
        property.setSecurityAccessor(securityAccessor);
        property.setCaName(caName);
        property.update();
    }

    @Override
    public void deleteCrlRequestTaskPropertiesForCa(RecurrentTask recurrentTask) {
        Optional<CrlRequestTaskProperty> crlRequestTaskProperty = getCrlRequestTaskPropertiesForCa(recurrentTask);
        crlRequestTaskProperty.ifPresent(CrlRequestTaskProperty::delete);
    }

    @Override
    public Optional<CrlRequestTaskProperty> getCrlRequestTaskPropertiesForCa(String caName) {
        return findCrlRequestTaskProperties().stream().filter(property -> property.getCaName().equalsIgnoreCase(caName)).findAny();
    }

    @Override
    public Optional<CrlRequestTaskProperty> getCrlRequestTaskPropertiesForCa(RecurrentTask recurrentTask) {
        return findCrlRequestTaskProperties().stream().filter(property -> property.getRecurrentTask().getId() == recurrentTask.getId()).findAny();
    }

}
