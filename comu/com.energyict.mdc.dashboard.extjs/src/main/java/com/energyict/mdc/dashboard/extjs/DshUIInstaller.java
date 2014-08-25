package com.energyict.mdc.dashboard.extjs;

import com.elster.jupiter.nls.*;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.callback.InstallService;
import com.google.inject.Inject;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;

@Component(name = "com.energyict.mdc.dashboard.extjs", service = InstallService.class, property = "name=" + DshUIInstaller.COMPONENT_NAME, immediate = true)
public class DshUIInstaller implements InstallService {
    private static final Logger LOG = Logger.getLogger(DshUIInstaller.class.getName());
    public static final String COMPONENT_NAME = "DSH";
    private volatile Thesaurus thesaurus;
    private volatile Activator activator;

    public DshUIInstaller(){}

    @Activate
    public void activate(BundleContext context) {
        try {
            activator = new Activator();
            activator.start(context);
        } catch (Exception e) {
            LOG.severe("Unable to activate Dashboard ExtJS bundle");
            throw new RuntimeException(e);
        }
    }

    @Deactivate
    public void deactivate() {
        if (activator != null) {
            try {
                activator.stop(null);
            } catch (Exception e) {
                LOG.severe("Unable to deactivate Dashboard ExtJS bundle");
                throw new RuntimeException(e);
            }
        }
    }

    @Inject
    public DshUIInstaller(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setThesaurus(NlsService nlsService){
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Override
    public void install() {
        createTranslations();
    }

    private void createTranslations() {
        Properties properties = new Properties();
        InputStream input = null;
        List<Translation> translations = new ArrayList<>();
        try {
            input = this.getClass().getClassLoader().getResourceAsStream("i18n.properties");
            properties.load(input);
            for (Map.Entry<Object, Object> translationProp : properties.entrySet()) {
                SimpleNlsKey nlsKey = SimpleNlsKey.key(COMPONENT_NAME, Layer.REST, (String)translationProp.getKey()).defaultMessage((String)translationProp.getValue());
                translations.add(SimpleTranslation.translation(nlsKey, Locale.ENGLISH, (String)translationProp.getValue()));
            }
            thesaurus.addTranslations(translations);

        } catch (UnderlyingSQLFailedException | IOException e) {
            LOG.severe("Exception while creation translations for Dashboard ExtJS bundle");
        }
    }
}