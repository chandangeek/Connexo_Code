package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileService;
import com.energyict.mdc.device.data.impl.finders.LoadProfileFinder;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;

import com.google.common.base.Optional;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link LoadProfileService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (13:06)
 */
public class LoadProfileServiceImpl implements LoadProfileService, ReferencePropertySpecFinderProvider {

    private final DeviceDataModelService deviceDataModelService;

    @Inject
    public LoadProfileServiceImpl(DeviceDataModelService deviceDataModelService) {
        super();
        this.deviceDataModelService = deviceDataModelService;
    }

    @Override
    public List<CanFindByLongPrimaryKey<? extends HasId>> finders() {
        List<CanFindByLongPrimaryKey<? extends HasId>> finders = new ArrayList<>();
        finders.add(new LoadProfileFinder(this.deviceDataModelService.dataModel()));
        return finders;
    }

    @Override
    public Optional<LoadProfile> findById(long id) {
        return this.deviceDataModelService.dataModel().mapper(LoadProfile.class).getOptional(id);
    }

}