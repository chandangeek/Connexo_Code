package com.energyict.mdc.common;

import com.energyict.mdc.common.impl.EnvironmentImpl;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

public class UserEnvironment implements Translator {
    private static UserEnvironment soleInstance;

    private Map<String, Object> namedObjects = new HashMap<>();
    private Map<String, ResourceBundle> languageBundles = new HashMap<>();

    public void put (String key, Object value) {
        namedObjects.put(key, value);
    }

    public Object get (String key) {
        return namedObjects.get(key);
    }

    private Translator getTranslator () {
        return EnvironmentImpl.getDefault();
    }

    public Locale getLocale () {
        return EnvironmentImpl.getDefault().getLocale();
    }

    public String getTranslation (String key) {
        return getTranslator().getTranslation(key);
    }

    public String getTranslation (String key, boolean errorIndication) {
        return getTranslator().getTranslation(key, errorIndication);
    }

    public String getErrorMsg (String key) {
        return getTranslator().getErrorMsg(key);
    }

    public String getCustomTranslation (String key) {
        return getTranslator().getCustomTranslation(key);
    }

    @Override
    public String getErrorCode (String messageId) {
        return getTranslator().getErrorCode(messageId);
    }

    public String getTranslation (String key, String defaultValue) {
        return getTranslator().getTranslation(key, defaultValue);
    }

    public boolean hasTranslation (String key) {
        return getTranslator().hasTranslation(key);
    }

    public synchronized ResourceBundle getLanguageBundle (String bundleName) {
        ResourceBundle bundle = (languageBundles.get(bundleName));
        if (bundle == null) {
            try {
                bundle = ResourceBundle.getBundle(bundleName, getLocale());
            }
            catch (MissingResourceException ex) {
                bundle = new EmptyResourceBundle();
            }
            languageBundles.put(bundleName, bundle);
        }
        return bundle;
    }

    // Get the translation for a specified bundle
    private String doGetMsg (String bundleName, String aKey) {
        if (bundleName == null) {
            return null;
        }
        try {
            return getLanguageBundle(bundleName).getString(aKey);
        }
        catch (MissingResourceException ex) {
            return null;
        }
    }

    public String getMsg (String bundleName, String aKey) {
        return getMsg(bundleName, aKey, false);
    }

    public String getMsg (String bundleName, String aKey, boolean errorIndication) {
        String result = doGetMsg(bundleName, aKey);
        if (result == null) {
            return (errorIndication ? "MR" : "") + aKey;
        }
        else {
            return result;
        }
    }

    public static synchronized UserEnvironment getDefault () {
        if (soleInstance == null) {
            initDefault();
        }
        return soleInstance;
    }

    private static void initDefault () {
        soleInstance = new UserEnvironment();
    }

    public static void setDefault (UserEnvironment environment) {
        soleInstance = environment;
    }

    private class EmptyResourceBundle extends ResourceBundle {

        private EmptyResourceBundle () {
        }

        protected Object handleGetObject (String key) {
            return null;
        }

        public Enumeration getKeys () {
            return new Vector().elements();
        }
    }

}
