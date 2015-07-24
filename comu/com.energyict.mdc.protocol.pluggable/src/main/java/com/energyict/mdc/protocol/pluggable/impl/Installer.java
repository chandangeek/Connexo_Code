package com.energyict.mdc.protocol.pluggable.impl;

import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceCapabilityAdapterMappingImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceCapabilityMapping;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMapping;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMapping;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingImpl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    public Installer(DataModel dataModel, EventService eventService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
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
        List<DeviceCapabilityMapping> capabilityAdapterMappings =
                properties.stringPropertyNames()
                    .stream()
                    .map(key -> new DeviceCapabilityAdapterMappingImpl(key, Integer.valueOf(properties.getProperty(key))))
                    .collect(Collectors.toList());
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
        List<MessageAdapterMapping> messageAdapterMappings =
                properties
                        .stringPropertyNames()
                        .stream()
                        .map(key -> new MessageAdapterMappingImpl(key, properties.getProperty(key)))
                        .collect(Collectors.toList());
        dataModel.mapper(MessageAdapterMapping.class).persist(messageAdapterMappings);
    }


    private void createSecurityAdapterMappings() {
        Properties properties = loadProperties(securityPropertyAdapterMappingLocation);
        List<SecuritySupportAdapterMapping> existingMappings = dataModel.mapper(SecuritySupportAdapterMapping.class).find();
        for (SecuritySupportAdapterMapping existingMapping : existingMappings) {
            Object existingMappingForDeviceProtocol = properties.get(existingMapping.getDeviceProtocolJavaClassName());
            if (existingMappingForDeviceProtocol != null) {
                properties.remove(existingMapping.getDeviceProtocolJavaClassName());
            }
        }
        List<SecuritySupportAdapterMapping> securitySupportAdapterMappings =
                properties.stringPropertyNames()
                    .stream()
                    .map(key -> new SecuritySupportAdapterMappingImpl(key, properties.getProperty(key)))
                    .collect(Collectors.toList());
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

}