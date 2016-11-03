package com.elster.jupiter.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroAction;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroAction;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointMicroActionFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component(name = "UsagePointMicroActionFactoryImpl",
        service = {UsagePointMicroActionFactory.class},
        immediate = true)
public class UsagePointMicroActionFactoryImpl implements UsagePointMicroActionFactory {

    private DataModel dataModel;
    private Thesaurus thesaurus;

    private final Map<String, Class<? extends MicroAction>> microActionMapping = new HashMap<>();

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
        addMicroActionMapping(CancelAllServiceCallsAction.class);
    }

    private void addMicroActionMapping(Class<? extends ExecutableMicroAction> clazz) {
        this.microActionMapping.put(clazz.getSimpleName(), clazz);
    }

    @Override
    public Optional<MicroAction> from(String microActionKey) {
        return Optional.ofNullable(this.microActionMapping.get(microActionKey))
                .map(this.dataModel::getInstance);
    }
}
