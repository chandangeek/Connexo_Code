package com.energyict.mdc.scheduling.extjs;

import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 10/02/14
 * Time: 10:15
 */
@Component(name="com.energyict.dcs.ui",service={InstallService.class},property = {"name=DCS"}, immediate = true)
public class DcsUiInstaller implements InstallService {

    public static String COMPONENTNAME = "DCS";

    private volatile Thesaurus thesaurus;
    private volatile Activator activator;

    public DcsUiInstaller() {
    }

    @Activate
    public void activate(BundleContext context) {
        try {
            activator = new Activator();
            activator.start(context);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Deactivate
    public void deactivate() {
        if (activator != null) {
            try {
                activator.stop(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Inject
    public DcsUiInstaller(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setThesaurus(NlsService nlsService){
        this.thesaurus = nlsService.getThesaurus("DCS", Layer.REST);
    }

    @Override
    public void install() {
        createTranslations();
    }

    private void createTranslations() {
        Properties prop = new Properties();
        InputStream input = null;
        List<Translation> translations = new ArrayList<>();
        try {
            input = this.getClass().getClassLoader().getResourceAsStream("i18n.properties");
            prop.load(input);
            for (Map.Entry<Object, Object> translationProp : prop.entrySet()) {
                SimpleNlsKey nlsKey = SimpleNlsKey.key(COMPONENTNAME, Layer.REST, (String)translationProp.getKey()).defaultMessage((String)translationProp.getValue());
                translations.add(toTranslation(nlsKey, Locale.ENGLISH,(String)translationProp.getValue()));
            }
            thesaurus.addTranslations(translations);

        } catch (UnderlyingSQLFailedException | IOException e) {
            e.printStackTrace();
        }
    }

    private Translation toTranslation(final SimpleNlsKey nlsKey, final Locale locale, final String translation) {
        return new Translation() {
            @Override
            public NlsKey getNlsKey() {
                return nlsKey;
            }

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public String getTranslation() {
                return translation;
            }
        };
    }
}
