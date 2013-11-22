package com.energyict.mdc.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

public class ApplicationContext implements FactoryFinder {

    private static Logger logger = Logger.getLogger(ApplicationContext.class.getName());

    private final List<ApplicationComponent> components = new ArrayList<>();
    private Locale locale;
    private final MainComponent mainComponent;
    private Translator translator;
    private FormatPreferences formatPreferences;
    private final boolean global;

    public ApplicationContext(MainComponent mainComponent, boolean global) {
        this(mainComponent, Locale.getDefault(), global);
    }

    public ApplicationContext(MainComponent mainComponent, Locale locale, boolean global) {
        this.global = global;
        this.locale = locale;
        this.mainComponent = mainComponent;
        addComponent(mainComponent);

    }

    public BusinessEventManager createEventManager() {
        return mainComponent.createEventManager();
    }

    public BusinessObjectFactory findFactory(String name) {
        BusinessObjectFactory result = null;
        for (ApplicationComponent each : components) {
            try {
                result = each.findFactory(name);
            } catch (Exception ex) {
                logger.finest("Did not find factory for " + name + " in module " + each.getName() + ". Looking in next module...");
            }
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public List<ProtectableFactory> getProtectableFactories() {
        List<ProtectableFactory> factories = new ArrayList<>();
        for (ApplicationComponent component : components) {
            factories.addAll(component.getProtectableFactories());
        }
        return factories;
    }

    public List<BusinessObjectFactory> getReferenceableFactories() {
        List<BusinessObjectFactory> factories = new ArrayList<>();
        for (ApplicationComponent component : components) {
            factories.addAll(component.getReferenceableFactories());
        }
        return factories;
    }

    public Translator getTranslator() {
        if (translator == null) {
            translator = newTranslator();
        }
        return translator;
    }

    private Translator newTranslator() {
        MultiBundleTranslator result = new MultiBundleTranslator(locale);
        for (ApplicationComponent each : components) {
            result.addLabelBundle(each.getLabelBundleName());
            result.addErrorBundle(each.getErrorBundleName());
            result.addCustomBundle(each.getCustomBundleName());
            result.addErrorCodesBundle(each.getErrorCodeBundleName());
        }
        return result;
    }

    public BusinessObjectFactory findFactory(int factoryId) {
        BusinessObjectFactory result;
        for (ApplicationComponent each : components) {
            result = each.findFactory(factoryId);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public Locale getLocale() {
        return locale;
    }

    public void addComponent(ApplicationComponent component) {
        for (ApplicationComponent each : components) {
            if (each.getName().equals(component.getName())) {
                throw new ApplicationException("Duplicate Component " + component.getName());
            }
        }
        components.add(component);
        component.setApplicationContext(this);
        translator = null;
    }

    public List<ApplicationComponent> getComponents() {
        return components;
    }

    public ApplicationComponent getComponent(String name) {
        for (ApplicationComponent each : components) {
            if (each.getName().equals(name)) {
                return each;
            }
        }
        return null;
    }

    public FormatPreferences getFormatPreferences() {
        if (formatPreferences == null) {
            formatPreferences = mainComponent.getFormatPreferences();
        }
        return formatPreferences;
    }

    public void preferencesChange () {
        translator = null;
        formatPreferences = null;
        locale = mainComponent.getLocale();
        if (global) {
            updateVmSettings();
        }
    }

    public boolean isGlobal() {
        return global;
    }

    private void updateVmSettings() {
        if (!locale.equals(Locale.getDefault())) {
            Locale.setDefault(locale);
        }
    }

    public <T> List<T> getModulesImplementing(Class<T> t) {
        List<T> result = new ArrayList<>();
        for (ApplicationComponent each : components) {
            if (t.isAssignableFrom(each.getClass())) {
                result.add((T) each);
            }
        }
        return result;
    }

}