package com.energyict.mdc.engine.offline;

import com.energyict.mdc.engine.offline.core.Translator;

import java.util.*;

public class UserEnvironment {
    private static UserEnvironment soleInstance;

    synchronized public static UserEnvironment getDefault() {
        if (soleInstance == null) {
            initDefault();
        }
        return soleInstance;
    }

    private static void initDefault() {
        soleInstance = new UserEnvironment();
    }

    public static void setDefault(UserEnvironment environment) {
        soleInstance = environment;
    }

    public UserEnvironment() {
        com.enterprisedt.util.license.License.setLicenseDetails("EnergyICT", "326-1363-8168-7486");
    }

    // Translator interface methods

    public Locale getLocale() {
        return Locale.getDefault();
    }

    private Map<String, Object> namedObjects = new HashMap<String, Object>();
    private Map<String, Object> globalNamedObjects = Collections.synchronizedMap(new HashMap<String, Object>());

    public Object get(String name) {
        Object result = get(name, false);
        if (result == null) {
            result = get(name, true);
        }
        return result;
    }

    public Object get(String name, boolean global) {
        if (global) {
            return globalNamedObjects.get(name);
        } else {
            return namedObjects.get(name);
        }
    }

    public void put(String name, Object value) {
        put(name, value, false);
    }

    public void put(String name, Object value, boolean global) {
        if (global) {
            globalNamedObjects.put(name, value);
        } else {
            namedObjects.put(name, value);
        }
    }

    private Map languageBundles = new HashMap();

    public synchronized ResourceBundle getLanguageBundle(String bundleName) {
        ResourceBundle bundle = (ResourceBundle) (languageBundles.get(bundleName));
        if (bundle == null) {
            try {
                bundle = ResourceBundle.getBundle(bundleName, getLocale());
            } catch (MissingResourceException ex) {
                bundle = new EmptyResourceBundle();
            }
            languageBundles.put(bundleName, bundle);
        }
        return bundle;
    }

    // Get the translation for a specified bundle
    private String doGetMsg(String bundleName, String aKey) {
        if (bundleName == null) {
            return null;
        }
        try {
            return getLanguageBundle(bundleName).getString(aKey);
        } catch (MissingResourceException ex) {
            return null;
        }
    }

    public String getMsg(String bundleName, String aKey) {
        return getMsg(bundleName, aKey, false);
    }

    public String getMsg(String bundleName, String aKey, boolean errorIndication) {
        String result = doGetMsg(bundleName, aKey);
        if (result == null) {
            return (errorIndication ? "MR" : "") + aKey;
        } else {
            return result;
        }
    }

    private class EmptyResourceBundle extends ResourceBundle {

        private EmptyResourceBundle() {
        }

        protected Object handleGetObject(String key) {
            return null;
        }

        public Enumeration getKeys() {
            return new Vector().elements();
        }
    }

}
