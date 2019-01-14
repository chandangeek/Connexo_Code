/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.zone.EndDeviceZone;
import com.elster.jupiter.metering.zone.EndDeviceZoneBuilder;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.Zone;
import com.elster.jupiter.metering.zone.ZoneBuilder;
import com.elster.jupiter.metering.zone.ZoneFilter;
import com.elster.jupiter.metering.zone.ZoneType;
import com.elster.jupiter.metering.zone.ZoneTypeBuilder;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.V10_6SimpleUpgrader;
import com.elster.jupiter.util.conditions.Order;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.util.conditions.Where.where;

@Component(
        name = "com.elster.jupiter.metering.zone",
        service = {MeteringZoneService.class, TranslationKeyProvider.class},
        property = "name=" + MeteringZoneService.COMPONENTNAME,
        immediate = true)
public class MeteringZoneServiceImpl implements MeteringZoneService, TranslationKeyProvider {

    private volatile DataModel dataModel;
    private volatile UpgradeService upgradeService;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;

    public MeteringZoneServiceImpl() {
    }

    @Inject
    public MeteringZoneServiceImpl(OrmService ormService, NlsService nlsService, UpgradeService upgradeService, MeteringService meteringService) {
        this();
        setOrmService(ormService);
        setNlsService(nlsService);
        setUpgradeService(upgradeService);
        setMeteringService(meteringService);
        activate();
    }

    @Activate
    public void activate() {
        try {
            dataModel.register(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Thesaurus.class).toInstance(thesaurus);
                    bind(MessageInterpolator.class).toInstance(thesaurus);
                    bind(MeteringZoneService.class).toInstance(MeteringZoneServiceImpl.this);
                    bind(DataModel.class).toInstance(dataModel);
                    bind(MeteringService.class).toInstance(meteringService);
                }
            });
            upgradeService.register(InstallIdentifier.identifier("Pulse", COMPONENTNAME),
                    dataModel,
                    Installer.class,
                    ImmutableMap.of(
                            version(10, 6), V10_6SimpleUpgrader.class
                    ));

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Deactivate
    public void deactivate() {

    }

    @Override
    public String getComponentName() {
        return COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>();
        translationKeys.addAll(Arrays.asList(MessageSeeds.values()));
        return translationKeys;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "Zones");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(MeteringZoneService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Override
    public List<ZoneType> getZoneTypes(String application) {
        return dataModel.mapper(ZoneType.class).select(where("application").isEqualToIgnoreCase(application), Order.ascending("name"));
    }

    @Override
    public Finder<Zone> getZones(String application, ZoneFilter zoneFilter) {
        return DefaultFinder.of(Zone.class,
                where("zoneType.application")
                        .isEqualToIgnoreCase(application)
                        .and(zoneFilter.toCondition()), dataModel, ZoneType.class)
                .sorted("zoneType.typeName", true)
                .sorted("name", true);
    }

    @Override
    public Optional<ZoneType> getZoneType(String name, String application) {
        return dataModel.mapper(ZoneType.class).select(where("typeName").isEqualToIgnoreCase(name)
                .and(where("application").isEqualToIgnoreCase(application)), Order.ascending("application"))
                .stream().findFirst();
    }

    @Override
    public Optional<Zone> getZone(long id) {
        return dataModel.mapper(Zone.class).select(where("id").isEqualTo(id)).stream().findFirst();
    }

    @Override
    public Optional<Zone> getAndLockZone(long id, long version) {
        return dataModel.mapper(Zone.class).lockObjectIfVersion(version, id);
    }

    @Override
    public ZoneBuilder newZoneBuilder() {
        return new ZoneBuilderImpl(dataModel);
    }

    @Override
    public ZoneFilter newZoneFilter() {
        return new ZoneFilterImpl();
    }

    @Override
    public ZoneTypeBuilder newZoneTypeBuilder() {
        return new ZoneTypeBuilderImpl(dataModel);
    }

    public Optional<ZoneType> getZoneType(String name) {
        return dataModel.mapper(ZoneType.class).select(where("typeName").isEqualToIgnoreCase(name)).stream().findFirst();
    }

    public Optional<Zone> getZone(String name, String typeName, String application) {
        return dataModel.query(Zone.class, ZoneType.class).select(where("name").isEqualToIgnoreCase(name)
                .and(where("zoneType.typeName").isEqualToIgnoreCase(typeName))
                .and(where("zoneType.application").isEqualToIgnoreCase(application))).stream().findFirst();
    }

    @Override
    public Finder<EndDeviceZone> getByEndDevice(EndDevice endDevice) {
        return DefaultFinder.of(EndDeviceZone.class,
                where("endDeviceId").isEqualTo(endDevice.getId()),
                this.dataModel, Zone.class, ZoneType.class)
                .defaultSortColumn("zone.zoneType.typeName", true);
    }

    @Override
    public Optional<EndDeviceZone> getEndDeviceZone(long endDeviceZoneId) {
        return dataModel.mapper(EndDeviceZone.class).select(where("id").isEqualTo(endDeviceZoneId)).stream().findFirst();
    }

    @Override
    public boolean isZoneInUse(long zoneId) {
        return dataModel.mapper(EndDeviceZone.class).select(where("zoneId").isEqualTo(zoneId)).stream().toArray().length > 0 ? true : false;
    }

    @Override
    public EndDeviceZoneBuilder newEndDeviceZoneBuilder() {
        return new EndDeviceZoneBuilderImpl(dataModel);
    }
}
