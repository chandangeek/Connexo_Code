package com.energyict.protocols.mdc.adapter.cps;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 2/01/2017 - 14:05
 */
public class SecurityCustomPropertySetNameDetective {

    public static final String MAPPING_PROPERTIES_FILE_NAME = "security-custom-property-set-mapping.properties";
    private static final Logger LOGGER = Logger.getLogger(SecurityCustomPropertySetNameDetective.class.getName());
    private final Map<String, String> customPropertySetClassNameMap = new ConcurrentHashMap<>();

    public SecurityCustomPropertySetNameDetective() {
        super();
        this.loadCustomPropertySetNameMapping();
    }

    private void loadCustomPropertySetNameMapping() {
        Properties mappings = new Properties();
        try (InputStream inputStream = this.getClass().getResourceAsStream(MAPPING_PROPERTIES_FILE_NAME)) {
            if (inputStream == null) {
                LOGGER.severe("CustomPropertySetNameMapping properties file location is probably not correct " + MAPPING_PROPERTIES_FILE_NAME);
                throw new IllegalArgumentException("CustomPropertySetNameMapping - Could not load the properties from " + MAPPING_PROPERTIES_FILE_NAME);
            }
            mappings.load(inputStream);
            mappings.entrySet().forEach(entry -> this.customPropertySetClassNameMap.put((String) entry.getKey(), (String) entry.getValue()));
        } catch (IOException e) {
            LOGGER.severe("Could not load the properties from " + MAPPING_PROPERTIES_FILE_NAME);
            throw new IllegalArgumentException("CustomPropertySetNameMapping - Could not load the properties from " + MAPPING_PROPERTIES_FILE_NAME);
        }
    }

    public String securityCustomPropertySetClassNameFor(Class deviceProtocolClass) {
            /* Would be nice to use computeIfAbsent (especially because this is a ConcurrentHashMap.
             * However: the function that calculates the value if it is absent is a recursive call.
             * A ConcurrentHashMap deadlocks itself in that case. */
        String customPropertySetClassName = this.customPropertySetClassNameMap.get(deviceProtocolClass.getName());
        if (customPropertySetClassName == null) {
            return this.customPropertySetClassNameForSuperclass(deviceProtocolClass);
        } else if (customPropertySetClassName.startsWith("@")) {
            return this.customPropertySetClassNameForReferencedClass(deviceProtocolClass, customPropertySetClassName.substring(1));
        } else {
            return customPropertySetClassName;
        }
    }

    private String customPropertySetClassNameForSuperclass(Class deviceProtocolClass) {
        Class superclass = deviceProtocolClass.getSuperclass();
        if (superclass != null) {
            String customPropertyClassName = this.securityCustomPropertySetClassNameFor(superclass);
            // Cache the class name at this level of the class hierarchy
            this.customPropertySetClassNameMap.put(deviceProtocolClass.getName(), customPropertyClassName);
            return customPropertyClassName;
        } else {
            throw new IllegalArgumentException("Unable to determine custom property set class name for protocol class " + deviceProtocolClass.getName());
        }
    }

    private String customPropertySetClassNameForReferencedClass(Class deviceProtocolClass, String referencedClassName) {
        try {
            Class<?> referencedClass = Class.forName(referencedClassName);
            String customPropertyClassName = this.securityCustomPropertySetClassNameFor(referencedClass);
            // Cache the class name for the class that references another
            this.customPropertySetClassNameMap.put(deviceProtocolClass.getName(), customPropertyClassName);
            return customPropertyClassName;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to determine custom property set class name for protocol class " + deviceProtocolClass.getName() + " because referenced class " + referencedClassName + " could not be found", e);
        }
    }
}