package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.nls")
public class NlsServiceImpl implements NlsService {

    private volatile DataModel dataModel;

    @Activate
    public void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(DataModel.class).toInstance(dataModel);
            }
        });
    }

    @Deactivate
    public void deactivate() {

    }

    public NlsServiceImpl() {
    }

    @Inject
    public NlsServiceImpl(OrmService ormService) {
        setOrmService(ormService);
        activate();
        dataModel.install(true, true);
    }

    @Override
    public Thesaurus getThesaurus(String componentName, Layer layer) {
        //TODO automatically generated method body, provide implementation.
        return null;
    }

    @Override
    public Thesaurus newThesaurus(String componentName, Layer layer) {
        //TODO automatically generated method body, provide implementation.
        return null;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "CIM Metering");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    DataModel getDataModel() {
        return dataModel;
    }
}
