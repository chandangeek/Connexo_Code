package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.cache.CacheService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.orm.callback.InstallService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.validation", service = {InstallService.class}, property = "name=" + Bus.COMPONENTNAME, immediate = true)
public class ValidationServiceImpl implements InstallService, ServiceLocator{

    private volatile OrmClient ormClient;
    private volatile ComponentCache componentCache;
    private volatile EventService eventService;

    @Activate
    public void activate(BundleContext context) {
        Bus.setServiceLocator(this);
    }

    @Deactivate
    public void deactivate(BundleContext context) {
        Bus.clearServiceLocator(this);
    }

    @Override
    public void install() {
        new InstallerImpl().install(true, true);
    }

    @Override
    public OrmClient getOrmClient() {
        return ormClient;
    }

    @Override
    public ComponentCache getComponentCache() {
        return componentCache;
    }

    @Override
    public EventService getEventService() {
        return eventService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(Bus.COMPONENTNAME, "Validation");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
        this.ormClient = new OrmClientImpl(dataModel);
    }

    @Reference(name = "ZCacheService")
    public void setCacheService(CacheService cacheService) {
        this.componentCache = cacheService.createComponentCache(ormClient.getDataModel());
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }
}
