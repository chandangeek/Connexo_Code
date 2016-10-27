package com.elster.jupiter.mdm.usagepoint.lifecycle.impl.checks;

import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroCheck;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointMicroCheckFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.upgrade.UpgradeService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.EnumMap;
import java.util.Map;

@Component(name = "com.elster.jupiter.mdm.usagepoint.lifecycle.impl.checks.UsagePointMicroCheckFactoryImpl",
        service = {UsagePointMicroCheckFactory.class},
        immediate = true)
public class UsagePointMicroCheckFactoryImpl implements UsagePointMicroCheckFactory {

    private DataModel dataModel;
    private Thesaurus thesaurus;

    private final Map<MicroCheck.Key, Class<? extends MicroCheck>> microCheckMapping = new EnumMap<>(MicroCheck.Key.class);

    @SuppressWarnings("unused") // OSGI
    public UsagePointMicroCheckFactoryImpl() {
    }

    @Inject
    public UsagePointMicroCheckFactoryImpl(UpgradeService upgradeService,
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

    @Reference
    public void activate() {
        this.dataModel.register(getModule());
        addMicroCheckMappings();
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

    private void addMicroCheckMappings() {
        this.microCheckMapping.put(MicroCheck.Key.ALL_DATA_VALID, AllDataValidCheck.class);
    }

    @Override
    public MicroCheck from(MicroCheck.Key key) {
        Class<? extends MicroCheck> implClass = this.microCheckMapping.get(key);
        if (implClass == null) {
            throw new IllegalArgumentException("Unknown micro check key");
        }
        return this.dataModel.getInstance(implClass);
    }
}
