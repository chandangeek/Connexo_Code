package com.energyict.mdc.device.data.impl.crlrequest;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.pki.SecurityAccessor;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTask;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskService;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class CrlRequestTaskServiceImpl implements CrlRequestTaskService {

    private final DeviceDataModelService deviceDataModelService;

    @Inject
    public CrlRequestTaskServiceImpl(DeviceDataModelService deviceDataModelService) {
        this.deviceDataModelService = deviceDataModelService;
    }

    @Override
    public CrlRequestTaskBuilder newCrlRequestTask() {
        return new CrlRequestTaskBuilderImpl();
    }

    @Override
    public List<CrlRequestTask> findAllCrlRequestTasks() {
        return deviceDataModelService.dataModel().mapper(CrlRequestTask.class).find();
    }

    @Override
    public Finder<CrlRequestTask> crlRequestTaskFinder() {
        return DefaultFinder.of(CrlRequestTask.class, this.deviceDataModelService.dataModel(),
                EndDeviceGroup.class, SecurityAccessor.class);
    }

    @Override
    public Optional<CrlRequestTask> findCrlRequestTask(long id) {
        return deviceDataModelService.dataModel().mapper(CrlRequestTask.class).getOptional(id);
    }

    private class CrlRequestTaskBuilderImpl implements CrlRequestTaskBuilder {
        private CrlRequestTask crlRequestTask;

        public CrlRequestTaskBuilderImpl() {
            this.crlRequestTask = deviceDataModelService.dataModel().getInstance(CrlRequestTaskImpl.class);
        }

        @Override
        public CrlRequestTaskBuilder withDeviceGroup(EndDeviceGroup deviceGroup) {
            crlRequestTask.setDeviceGroup(deviceGroup);
            return this;
        }

        @Override
        public CrlRequestTaskBuilder withSecurityAccessor(SecurityAccessor securityAccessor) {
            crlRequestTask.setSecurityAccessor(securityAccessor);
            return this;
        }

        @Override
        public CrlRequestTaskBuilder withCaName(String caName) {
            crlRequestTask.setCaName(caName);
            return this;
        }

        @Override
        public CrlRequestTaskBuilder withFrequency(String frequency) {
            crlRequestTask.setFrequency(frequency);
            return this;
        }

        @Override
        public CrlRequestTask save() {
            crlRequestTask.save();
            return crlRequestTask;
        }
    }
}
