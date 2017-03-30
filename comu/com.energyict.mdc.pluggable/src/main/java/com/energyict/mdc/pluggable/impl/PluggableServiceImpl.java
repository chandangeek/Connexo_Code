/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeCheckList;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.pluggable.PluggableService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link PluggableService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-20 (17:49)
 */
@Component(name = "com.energyict.mdc.pluggable", service = {PluggableService.class, MessageSeedProvider.class}, property = "name=" + PluggableService.COMPONENTNAME)
public class PluggableServiceImpl implements PluggableService, MessageSeedProvider {

    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile UpgradeService upgradeService;

    public PluggableServiceImpl() {
        super();
    }

    @Inject
    public PluggableServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService, Clock clock, UpgradeService upgradeService) {
        this();
        setOrmService(ormService);
        setEventService(eventService);
        setNlsService(nlsService);
        setClock(clock);
        setUpgradeService(upgradeService);
        activate();
    }

    @Override
    public PluggableClass newPluggableClass(PluggableClassType type, String name, String javaClassName) {
        return PluggableClassImpl.from(this.dataModel, type, name, javaClassName);
    }

    @Override
    public Optional<PluggableClass> findByTypeAndName(PluggableClassType type, String name) {
        return this.dataModel.mapper(PluggableClass.class).
                getUnique("pluggableType", PersistentPluggableClassType.forActualType(type),
                        "name", name);
    }

    @Override
    public List<PluggableClass> findByTypeAndClassName(PluggableClassType type, String javaClassName) {
        return this.dataModel.mapper(PluggableClass.class).
                find("pluggableType", PersistentPluggableClassType.forActualType(type),
                        "javaClassName", javaClassName);
    }

    @Override
    public Optional<PluggableClass> findByTypeAndId(PluggableClassType type, long id) {
        Optional<PluggableClass> pluggableClass = this.dataModel.mapper(PluggableClass.class).getOptional(id);
        return pluggableClass.filter(pc -> pc.getPluggableClassType().equals(type));
    }

    @Override
    public Optional<PluggableClass> findAndLockPluggableClassByIdAndVersion(PluggableClassType type, long id, long version) {
        Optional<PluggableClass> pluggableClass = this.dataModel.mapper(PluggableClass.class).lockObjectIfVersion(version, id);
        return pluggableClass.filter(pc -> pc.getPluggableClassType().equals(type));
    }

    @Override
    public Finder<PluggableClass> findAllByType(PluggableClassType type) {
        return DefaultFinder.of(PluggableClass.class, Where.where("pluggableType").isEqualTo(PersistentPluggableClassType.forActualType(type)), this.dataModel).defaultSortColumn("name");
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENTNAME, "ComServer pluggable classes");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
    }

    public EventService getEventService() {
        return eventService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public Clock getClock() {
        return clock;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(Clock.class).toInstance(clock);
            }
        };
    }

    @Reference(target ="(com.elster.jupiter.checklist=MultiSense)")
    public void setCheckList(UpgradeCheckList upgradeCheckList) {
        // just depend explicitly
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());

        upgradeService.register(InstallIdentifier.identifier("MultiSense", PluggableService.COMPONENTNAME), dataModel, Installer.class, Collections.emptyMap());
    }

}