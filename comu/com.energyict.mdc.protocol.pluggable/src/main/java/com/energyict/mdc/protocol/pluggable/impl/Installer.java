package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.Translation;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceCapabilityAdapterMappingImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceCapabilityMapping;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMapping;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMapping;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Installs the protocol pluggable bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-23 (15:39)
 */
public class Installer {

    private static final Logger LOGGER = Logger.getLogger(Installer.class.getName());
    private static final String securityPropertyAdapterMappingLocation = "/securitymappings.properties";
    private static final String messageAdapterMappingLocation = "/messagemappings.properties";
    private static final String capabilityMappingsLocation = "/capabilitymappings.properties";

    private final DataModel dataModel;
    private final EventService eventService;
    private final Thesaurus thesaurus;

    public Installer(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.thesaurus = thesaurus;
    }

    public void install(boolean executeDdl, boolean createMasterData) {
        try {
            this.dataModel.install(executeDdl, true);
            if (createMasterData) {
                this.createMasterData();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        createSecurityAdapterMappings();
        createMessageAdapterMappings();
        createCapabilityMappings();
        createEventTypes();
        createTranslations();
    }

    private void createCapabilityMappings() {
        Properties properties = loadProperties(capabilityMappingsLocation);
        List<DeviceCapabilityMapping> existingMappings = dataModel.mapper(DeviceCapabilityMapping.class).find();
        for (DeviceCapabilityMapping existingMapping : existingMappings) {
            Object existingCapability = properties.get(existingMapping.getDeviceProtocolJavaClassName());
            if(existingCapability != null){
                properties.remove(existingMapping.getDeviceProtocolJavaClassName());
            }
        }
        List<DeviceCapabilityMapping> capabilityAdapterMappings = new ArrayList<>(properties.size());
        for (String key : properties.stringPropertyNames()) {
            capabilityAdapterMappings.add(new DeviceCapabilityAdapterMappingImpl(key, Integer.valueOf(properties.getProperty(key))));
        }
        dataModel.mapper(DeviceCapabilityMapping.class).persist(capabilityAdapterMappings);
    }

    private void createMessageAdapterMappings() {
        Properties properties = loadProperties(messageAdapterMappingLocation);
        List<MessageAdapterMapping> existingMappings = dataModel.mapper(MessageAdapterMapping.class).find();
        for (MessageAdapterMapping existingMapping : existingMappings) {
            Object existingMappingForDeviceProtocol = properties.get(existingMapping.getDeviceProtocolJavaClassName());
            if(existingMappingForDeviceProtocol != null){
                properties.remove(existingMapping.getDeviceProtocolJavaClassName());
            }
        }
        List<MessageAdapterMapping> messageAdapterMappings = new ArrayList<>(properties.size());
        for (String key : properties.stringPropertyNames()) {
            messageAdapterMappings.add(new MessageAdapterMappingImpl(key, properties.getProperty(key)));
        }
        dataModel.mapper(MessageAdapterMapping.class).persist(messageAdapterMappings);
    }


    private void createSecurityAdapterMappings() {
        Properties properties = loadProperties(securityPropertyAdapterMappingLocation);
        List<SecuritySupportAdapterMapping> existingMappings = dataModel.mapper(SecuritySupportAdapterMapping.class).find();
        for (SecuritySupportAdapterMapping existingMapping : existingMappings) {
            Object existingMappingForDeviceProtocol = properties.get(existingMapping.getDeviceProtocolJavaClassName());
            if(existingMappingForDeviceProtocol != null){
                properties.remove(existingMapping.getDeviceProtocolJavaClassName());
            }
        }
        List<SecuritySupportAdapterMapping> securitySupportAdapterMappings = new ArrayList<>(properties.size());
        for (String key : properties.stringPropertyNames()) {
            securitySupportAdapterMappings.add(new SecuritySupportAdapterMappingImpl(key, properties.getProperty(key)));
        }
        dataModel.mapper(SecuritySupportAdapterMapping.class).persist(securitySupportAdapterMappings);
    }

    private Properties loadProperties(String propertiesLocation) {
        Properties mappings = new Properties();
        try (InputStream inputStream = Installer.class.getResourceAsStream(propertiesLocation)) {
            if (inputStream == null) {
                LOGGER.severe("PropertiesFile location is probably not correct :" + propertiesLocation);
            }
            mappings.load(inputStream);
        } catch (IOException e) {
            LOGGER.severe("Could not load the properties from " + propertiesLocation);
        }
        return mappings;
    }

    private void createTranslations() {
        List<Translation> translations = new ArrayList<>(MessageSeeds.values().length);
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            SimpleNlsKey nlsKey = SimpleNlsKey.key(ProtocolPluggableService.COMPONENTNAME, Layer.DOMAIN, messageSeed.getKey()).defaultMessage(messageSeed.getDefaultFormat());
            translations.add(toTranslation(nlsKey, Locale.ENGLISH, messageSeed.getDefaultFormat()));
        }
        thesaurus.addTranslations(translations);
    }

    private Translation toTranslation(SimpleNlsKey nlsKey, Locale locale, String translation) {
        return new SimpleTranslation(nlsKey, locale, translation);
    }

    private void createMasterData() {
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.install(this.eventService);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class SimpleTranslation implements Translation {
        private final SimpleNlsKey nlsKey;
        private final Locale locale;
        private final String translation;

        private SimpleTranslation(SimpleNlsKey nlsKey, Locale locale, String translation) {
            this.nlsKey = nlsKey;
            this.locale = locale;
            this.translation = translation;
        }

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
    }

}