package com.energyict.mdc.engine.offline;

import com.energyict.mdc.engine.offline.core.Translator;

import java.util.*;

public class MultiBundleTranslator implements Translator {

    public static final String MISSING_RESOURCE_PREFIX = "MR";
    public static final String NO_RESOURCE_PREFIX = "NR";

    final private Locale locale;
    final private List<ResourceBundle> labelBundles = new ArrayList<>();
    final private List<ResourceBundle> errorBundles = new ArrayList<>();
    final private Set<ResourceBundle> customBundles = new HashSet<>();
    final private List<ResourceBundle> errorCodes = new ArrayList<>();

    MultiBundleTranslator(Locale locale) {
        this.locale = locale;
    }

    void addLabelBundle(String name) {
        addResourceBundleToCollection(name, labelBundles);
    }

    void addErrorBundle(String name) {
        addResourceBundleToCollection(name, errorBundles);
    }

    void addCustomBundle(String name) {
        addResourceBundleToCollection(name, customBundles);
    }

    void addErrorCodesBundle(String name) {
        if (name != null && !"".equals(name.trim())) {
            try {
                errorCodes.add(ResourceBundle.getBundle(name, Locale.ROOT));
            } catch (MissingResourceException ex) {
                // silently ignore
            }
        }
    }

    private void addResourceBundleToCollection(String name, Collection<ResourceBundle> bundleList) {
        if (name != null && !"".equals(name.trim())) {
            try {
                bundleList.add(ResourceBundle.getBundle(name, locale));
            } catch (MissingResourceException ex) {
                // silently ignore
            }
        }
    }

    public String getErrorMsg(String key) {
        for (ResourceBundle each : errorBundles) {
            try {
                return each.getString(key);
            } catch (MissingResourceException ex) {
                // silently ignore
            }
        }
        return MISSING_RESOURCE_PREFIX + key;
    }

    @Override
    public String getErrorCode(String messageId) {
        for (ResourceBundle errorCode : errorCodes) {
            try {
                return errorCode.getString(messageId);
            } catch (MissingResourceException ex) {
                // silently ignore
            }
        }
        return "EIS-UNKNOWN-" + messageId.toUpperCase();
    }

    public String getTranslation(String key) {
        return getTranslation(key, MISSING_RESOURCE_PREFIX + key);
    }

    public String getTranslation(String key, boolean flagError) {
        return getTranslation(key, flagError ? MISSING_RESOURCE_PREFIX + key : key);
    }

    public String getTranslation(String key, String defaultValue) {
        for (ResourceBundle each : labelBundles) {
            try {
                return each.getString(key);
            } catch (MissingResourceException ex) {
                // silently ignore
            }
        }
        return defaultValue;
    }

    public boolean hasTranslation(String key) {
        String translation = getTranslation(key);
        return !translation.startsWith(MISSING_RESOURCE_PREFIX) && !translation.startsWith(NO_RESOURCE_PREFIX);
    }

    public String getCustomTranslation(String key) {
        for (ResourceBundle each : customBundles) {
            try {
                return each.getString(key);
            } catch (MissingResourceException ex) {
                // silently ignore
            }
        }
        return key;
    }

    public Locale getLocale() {
        return locale;
    }

}
