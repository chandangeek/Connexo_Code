package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

@Component(name = "com.elster.jupiter.nls", service = {NlsService.class, InstallService.class}, property = "name=" + NlsService.COMPONENTNAME)
public class NlsServiceImpl implements NlsService, InstallService {

    private volatile DataModel dataModel;

    private volatile ThreadPrincipalService threadPrincipalService;

    @Activate
    public void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(ThreadPrincipalService.class).toInstance(threadPrincipalService);
            }
        });
    }

    @Deactivate
    public void deactivate() {

    }

    public NlsServiceImpl() {
    }

    @Inject
    public NlsServiceImpl(OrmService ormService, ThreadPrincipalService threadPrincipalService) {
        setOrmService(ormService);
        setThreadPrincipalService(threadPrincipalService);
        activate();
        dataModel.install(true, true);
    }

    @Override
    public Thesaurus getThesaurus(String componentName, Layer layer) {
        return dataModel.getInstance(ThesaurusImpl.class).init(componentName, getNlsKeys(componentName, layer));
    }

    private List<NlsKeyImpl> getNlsKeys(String componentName, Layer layer) {
        Condition condition = Operator.EQUAL.compare("layer", layer).and(Operator.EQUAL.compare("componentName", componentName));
        return dataModel.query(NlsKeyImpl.class, NlsEntry.class).select(condition);
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

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public void install() {
        dataModel.install(true, true);
    }
}
