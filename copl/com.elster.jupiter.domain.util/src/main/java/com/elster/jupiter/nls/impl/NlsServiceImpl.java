package com.elster.jupiter.nls.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.SimpleTranslation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Locale;

@Component(name = "com.elster.jupiter.nls", service = {NlsService.class, InstallService.class}, property = {"name=" + NlsService.COMPONENTNAME, "osgi.command.scope=nls", "osgi.command.function=addTranslation"})
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
        if (!dataModel.isInstalled()) {
            dataModel.install(true, true);
        }
    }

    @Override
    public Thesaurus getThesaurus(String componentName, Layer layer) {
        return dataModel.getInstance(ThesaurusImpl.class).init(componentName, layer);
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

    public void addTranslation(String componentName, String layerName, String key, String defaultMessage) {
        try {
            Layer layer = Layer.valueOf(layerName);
            Thesaurus thesaurus = getThesaurus(componentName, layer);
            SimpleNlsKey nlsKey = SimpleNlsKey.key(componentName, layer, key).defaultMessage(defaultMessage);
            Translation translation = SimpleTranslation.translation(nlsKey, Locale.ENGLISH, defaultMessage);
            thesaurus.addTranslations(Arrays.asList(translation));
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void addTranslation(Object... args) {
        System.out.println("Usage : \n\n addTranslation componentName layerName key defaultMessage");
    }
}
