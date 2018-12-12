/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.favorites.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.favorites.DeviceLabel;
import com.energyict.mdc.favorites.FavoriteDeviceGroup;
import com.energyict.mdc.favorites.FavoritesService;
import com.energyict.mdc.favorites.LabelCategory;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static com.elster.jupiter.orm.Version.version;

@Component(name = "com.elster.jupiter.favorites", service = { FavoritesService.class, MessageSeedProvider.class }, property = "name=" + FavoritesService.COMPONENTNAME, immediate = true)
public class FavoritesServiceImpl implements FavoritesService, MessageSeedProvider {
    private static final Logger LOGGER = Logger.getLogger(FavoritesServiceImpl.class.getName());

    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile Clock clock;
    private volatile UpgradeService upgradeService;

    public FavoritesServiceImpl() {
    }

    @Inject
    public FavoritesServiceImpl(Clock clock, OrmService ormService, NlsService nlsService, UpgradeService upgradeService) {
        this();
        setClockService(clock);
        setOrmService(ormService);
        setNlsService(nlsService);
        setUpgradeService(upgradeService);
        activate();
    }

    @Activate
    public void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(FavoritesService.class).toInstance(FavoritesServiceImpl.this);
                bind(DataModel.class).toInstance(dataModel);
            }
        });
        upgradeService.register(InstallIdentifier.identifier("MultiSense", FavoritesService.COMPONENTNAME), dataModel, Installer.class, ImmutableMap.of(version(10, 2), UpgraderV10_2.class));
    }

    static class Installer implements FullInstaller {

        private final DataModel dataModel;

        @Inject
        Installer(DataModel dataModel) {
            this.dataModel = dataModel;
        }

        @Override
        public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
            dataModelUpgrader.upgrade(dataModel, Version.latest());
        }
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(FavoritesService.COMPONENTNAME, "Favorites");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setClockService(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(FavoritesService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        //Required for initialization data models in correct sequence
    }

    @Reference
    public void setMeteringGroupService(MeteringGroupsService meteringGroupsService) {
        //Required for initialization data models in correct sequence
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Override
    public List<LabelCategory> getLabelCategories() {
        return dataModel.mapper(LabelCategory.class).find();
    }

    @Override
    public Optional<LabelCategory> findLabelCategory(String name) {
        return dataModel.mapper(LabelCategory.class).getUnique("name", name);
    }

    @Override
    public LabelCategory createLabelCategory(String name) {
        LabelCategoryImpl labelCategory = dataModel.getInstance(LabelCategoryImpl.class);
        labelCategory.init(name);
        Save.CREATE.save(dataModel, labelCategory);
        return labelCategory;
    }

    @Override
    public List<DeviceLabel> getDeviceLabels(Device device, User user) {
        return dataModel.mapper(DeviceLabel.class).find("device", device, "user", user);
    }

    @Override
    public List<DeviceLabel> getDeviceLabelsOfCategory(User user, LabelCategory category) {
        return dataModel.mapper(DeviceLabel.class).find("user", user, "labelCategory", category);
    }

    @Override
    public Optional<DeviceLabel> findDeviceLabel(Device device, User user, LabelCategory category) {
        return dataModel.mapper(DeviceLabel.class).getUnique(
                new String[] { "device", "user", "labelCategory" },
                new Object[] { device, user, category });
    }

    @Override
    public DeviceLabel findOrCreateDeviceLabel(Device device, User user, LabelCategory category, String comment) {
        Optional<DeviceLabel> existingDeviceLabel = findDeviceLabel(device, user, category);
        if (existingDeviceLabel.isPresent()) {
            return existingDeviceLabel.get();
        }
        DeviceLabel deviceLabel = new DeviceLabelImpl(device, user, category, comment, Instant.now(clock));
        Save.CREATE.save(dataModel, deviceLabel);
        return deviceLabel;
    }

    @Override
    public void removeDeviceLabel(DeviceLabel deviceLabel) {
        dataModel.remove(deviceLabel);
    }

    @Override
    public List<FavoriteDeviceGroup> getFavoriteDeviceGroups(User user) {
        return dataModel.mapper(FavoriteDeviceGroup.class).find("user", user);
    }

    @Override
    public Optional<FavoriteDeviceGroup> findFavoriteDeviceGroup(EndDeviceGroup deviceGroup, User user) {
        return dataModel.mapper(FavoriteDeviceGroup.class).getUnique("endDeviceGroup", deviceGroup, "user", user);
    }

    @Override
    public FavoriteDeviceGroup findOrCreateFavoriteDeviceGroup(EndDeviceGroup deviceGroup, User user) {
        Optional<FavoriteDeviceGroup> existingFavoriteDeviceGroup = findFavoriteDeviceGroup(deviceGroup, user);
        return existingFavoriteDeviceGroup.orElseGet(() -> {
            FavoriteDeviceGroup favoriteDeviceGroup = new FavoriteDeviceGroupImpl(deviceGroup, user);
            Save.CREATE.save(dataModel, favoriteDeviceGroup);
            return favoriteDeviceGroup;
        });
    }

    @Override
    public void removeFavoriteDeviceGroup(FavoriteDeviceGroup favoriteDeviceGroup) {
        dataModel.remove(favoriteDeviceGroup);
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }
}
