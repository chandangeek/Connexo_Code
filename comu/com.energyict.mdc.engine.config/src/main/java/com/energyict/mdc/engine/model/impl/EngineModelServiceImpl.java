package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.engine.model", service = EngineModelService.class)
public class EngineModelServiceImpl implements EngineModelService, ServiceLocator {

    private volatile OrmClient ormClient;

    @Override
    public ComServer findComServer(String name) {
        return null;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel cem = ormService.newDataModel("CEM", "ComServer Engine Model");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(cem);
        }
        ormService.register(cem);
        this.ormClient = new OrmClientImpl(cem);
    }

    @Activate
    public void activate() {
        Bus.setServiceLocator(this);
    }

    @Deactivate
    public void deactivate() {
        Bus.clearServiceLocator(this);
    }

    @Override
    public OrmClient getOrmClient() {
        return this.ormClient;
    }
}
