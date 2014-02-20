package com.elster.jupiter.unifyingjs;

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

/**
 * Copyrights EnergyICT
 * Date: 10/02/14
 * Time: 10:15
 */
@Component(name="com.energyict.uni.ui",service={InstallService.class},property = {"name=UNI"}, immediate = true)
public class UniUiInstaller implements InstallService {

    public static String COMPONENTNAME = "UNI";

    private volatile Thesaurus thesaurus;
    private volatile Activator activator;

    public UniUiInstaller() {
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
    public UniUiInstaller(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setThesaurus(NlsService nlsService){
        this.thesaurus = nlsService.getThesaurus("UNI", Layer.REST);
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
                translations.add(toTranslation(nlsKey, new Locale("stars"),"**********************************************************************************************************************************".substring(0,translationProp.getValue().toString().length())));
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
