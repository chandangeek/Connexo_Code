package com.energyict.mdc.device.data.finders;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.data.LoadProfile;
import com.google.common.base.Optional;

/**
 * Serves as a <i>Finder</i> factory for {@link com.energyict.mdc.device.data.LoadProfile LoadProfiles}
 * <p/>
 * Copyrights EnergyICT
 * Date: 27/03/14
 * Time: 16:33
 */
public class LoadProfileFinder implements CanFindByLongPrimaryKey<LoadProfile> {

    private final DataModel dataModel;

    public LoadProfileFinder(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public FactoryIds registrationKey() {
        return FactoryIds.LOADPROFILE;
    }

    @Override
    public Class<LoadProfile> valueDomain() {
        return LoadProfile.class;
    }

    @Override
    public Optional<LoadProfile> findByPrimaryKey(long id) {
        return dataModel.mapper(LoadProfile.class).getUnique("id", id);
    }
}