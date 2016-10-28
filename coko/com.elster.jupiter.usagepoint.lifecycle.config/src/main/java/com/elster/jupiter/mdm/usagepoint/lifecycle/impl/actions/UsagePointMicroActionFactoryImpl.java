package com.elster.jupiter.mdm.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroAction;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointMicroActionFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.upgrade.UpgradeService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.EnumMap;
import java.util.Map;

@Component(name = "com.elster.jupiter.mdm.usagepoint.lifecycle.impl.actions.UsagePointMicroActionFactoryImpl",
        service = {UsagePointMicroActionFactory.class},
        immediate = true)
public class UsagePointMicroActionFactoryImpl implements UsagePointMicroActionFactory {

    private DataModel dataModel;
    private Thesaurus thesaurus;

    private final Map<MicroAction.Key, Class<? extends MicroAction>> microActionMapping = new EnumMap<>(MicroAction.Key.class);

    @SuppressWarnings("unused") // OSGI
    public UsagePointMicroActionFactoryImpl() {
    }

    @Inject
    public UsagePointMicroActionFactoryImpl(UpgradeService upgradeService,
                                            NlsService nlsService) {
        setUpgradeService(upgradeService);
        setNlsService(nlsService);
        activate();
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.dataModel = upgradeService.newNonOrmDataModel();
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(UsagePointLifeCycleService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Activate
    public void activate() {
        this.dataModel.register(getModule());
        addMicroActionMappings();
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
            }
        };
    }

    private void addMicroActionMappings() {
        this.microActionMapping.put(MicroAction.Key.CANCEL_ALL_SERVICE_CALLS, CancelAllServiceCallsAction.class);
    }

    @Override
    public MicroAction from(MicroAction.Key key) {
        Class<? extends MicroAction> implClass = this.microActionMapping.get(key);
        if (implClass == null) {
            throw new IllegalArgumentException("Unknown micro action key");
        }
        return this.dataModel.getInstance(implClass);
    }
}
