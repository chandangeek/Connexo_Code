package com.energyict.mdc.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ApplicationContext {

    private final List<ApplicationComponent> components = new ArrayList<>();
    private Locale locale;
    private final MainComponent mainComponent;
    private Translator translator;
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

    public boolean isGlobal() {
        return global;
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