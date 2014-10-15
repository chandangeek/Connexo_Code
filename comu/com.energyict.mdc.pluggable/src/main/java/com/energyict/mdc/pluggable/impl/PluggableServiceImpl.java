package com.energyict.mdc.pluggable.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.conditions.Where;
import java.time.Clock;
import com.energyict.mdc.common.services.DefaultFinder;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.pluggable.PluggableService;
import java.util.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * Provides an implemenation for the {@link PluggableService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-20 (17:49)
 */
@Component(name = "com.energyict.mdc.pluggable", service = {PluggableService.class, InstallService.class}, property = "name=" + PluggableService.COMPONENTNAME)
public class PluggableServiceImpl implements PluggableService, InstallService {

    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Clock clock;
    private volatile Thesaurus thesaurus;

    @Inject
    public PluggableServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService, Clock clock) {
        this();
        this.setOrmService(ormService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        this.setClock(clock);
        this.activate();
        this.install();
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
    public PluggableClass findByTypeAndId(PluggableClassType type, long id) {
        List<PluggableClass> pluggableClasses = this.dataModel.mapper(PluggableClass.class).find("pluggableType", PersistentPluggableClassType.forActualType(type), "id", id);
        if (pluggableClasses.isEmpty()) {
            return null;
        } else {
            return pluggableClasses.get(0);
        }
    }

    @Override
    public Finder<PluggableClass> findAllByType(PluggableClassType type) {
        return DefaultFinder.of(PluggableClass.class, Where.where("pluggableType").isEqualTo(PersistentPluggableClassType.forActualType(type)), this.dataModel);
    }

    public PluggableServiceImpl() {
        super();
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

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    @Override
    public void install() {
        new Installer(this.dataModel, this.eventService, this.thesaurus).install(true, true);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "EVT", "NLS");
    }

}