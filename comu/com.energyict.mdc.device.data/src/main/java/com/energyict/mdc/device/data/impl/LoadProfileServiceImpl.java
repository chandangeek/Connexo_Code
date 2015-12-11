package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link LoadProfileService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (13:06)
 */
public class LoadProfileServiceImpl implements ServerLoadProfileService, LoadProfileFactory {

    private final DeviceDataModelService deviceDataModelService;

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

    @Override
    public BaseLoadProfile findLoadProfileById(int loadProfileId) {
        return this.findById(loadProfileId).orElse(null);
    }

    @Override
    public List<BaseLoadProfile<BaseChannel>> findLoadProfilesByDevice(BaseDevice<BaseChannel, BaseLoadProfile<BaseChannel>, BaseRegister> device) {
        return device.getLoadProfiles();
    }

}