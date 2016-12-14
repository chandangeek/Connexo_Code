package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileService;
import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Provides an implementation for the {@link LoadProfileService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (13:06)
 */
@Component(name = "com.energyict.mdc.device.data.impl.LoadProfileServiceImpl", service = LoadProfileService.class, immediate = true)
public class LoadProfileServiceImpl implements ServerLoadProfileService {

    private volatile DeviceDataModelService deviceDataModelService;

    //Only testing purposes
    public LoadProfileServiceImpl() {
    }

    @Inject
    public LoadProfileServiceImpl(DeviceDataModelService deviceDataModelService) {
        super();
        this.deviceDataModelService = deviceDataModelService;
    }

    @Override
    public Optional<LoadProfile> findById(long id) {
        return this.deviceDataModelService.dataModel().mapper(LoadProfile.class).getOptional(id);
    }

    @Override
    public Optional<LoadProfile> findAndLockLoadProfileByIdAndVersion(long id, long version) {
        return this.deviceDataModelService.dataModel().mapper(LoadProfile.class).lockObjectIfVersion(version, id);
    }
}