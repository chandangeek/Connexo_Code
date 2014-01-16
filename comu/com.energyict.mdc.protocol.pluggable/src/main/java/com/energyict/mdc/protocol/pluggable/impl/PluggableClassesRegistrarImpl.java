package com.energyict.mdc.protocol.pluggable.impl;

import com.energyict.mdc.common.license.License;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.LicensedProtocol;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.PluggableClassesRegistrar;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.logging.Logger;

/**
 * Provides an implementation for the {@link PluggableClassesRegistrar} interface.
 *
 * @author bvn
 */
@Component(name = "com.energyict.mdc.protocol.pluggable.pluggableclassesregistrar")
public class PluggableClassesRegistrarImpl implements PluggableClassesRegistrar {
    private static final Logger LOGGER = Logger.getLogger(PluggableClassesRegistrarImpl.class.getName());

    private volatile LicensedProtocolService licensedProtocolService;
    private volatile ConnectionTypeService connectionTypeService;
    private volatile InboundDeviceProtocolService inboundDeviceProtocolService;
    private volatile ProtocolPluggableService protocolPluggableService;

    @Reference
    public void setLicensedProtocolService(LicensedProtocolService licensedProtocolService) {
        this.licensedProtocolService = licensedProtocolService;
    }

    @Reference
    public void setConnectionTypeService(ConnectionTypeService connectionTypeService) {
        this.connectionTypeService = connectionTypeService;
    }

    @Reference
    public void setInboundDeviceProtocolService(InboundDeviceProtocolService inboundDeviceProtocolService) {
        this.inboundDeviceProtocolService = inboundDeviceProtocolService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    public void start(License license) {
        LOGGER.fine("Registering pluggable classes");
        registerInboundDeviceProtocolPluggableClasses();
        registerDeviceProtocolPluggableClasses(license);
        registerConnectionTypePluggableClasses();
    }

    private void registerInboundDeviceProtocolPluggableClasses() {
        try {
            for (PluggableClassDefinition definition : inboundDeviceProtocolService.getExistingInboundDeviceProtocolPluggableClasses()) {
                try {
                    if (this.protocolPluggableService.findInboundDeviceProtocolPluggableClassByClassName(definition.getProtocolTypeClass().getName()) != null) {
                        InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass =
                                this.protocolPluggableService.newInboundDeviceProtocolPluggableClass(
                                        definition.getName(),
                                        definition.getProtocolTypeClass().getName());
                        inboundDeviceProtocolPluggableClass.save();
                        LOGGER.fine("Created pluggable class for " + definition.getProtocolTypeClass().getSimpleName());
                    }
                    else {
                        LOGGER.fine("Skipping " + definition.getProtocolTypeClass().getName() + ": already exists");
                    }
                }
                catch (RuntimeException e) {
                    if (e.getCause() != null) {
                        this.handleCreationException(definition, e.getCause());
                    }
                    else {
                        this.handleCreationException(definition, e);
                    }
                }
                catch (Exception e) {
                    this.handleCreationException(definition, e);
                }
            }
        }
        catch (Exception e) {
            LOGGER.severe("Failed to register any inbound device protocol pluggable class: " + e);
        }
    }

    private void registerDeviceProtocolPluggableClasses(License license) {
        try {
            for (LicensedProtocol licensedProtocolRule : this.licensedProtocolService.getAllLicensedProtocols(license)) {
                try {
                    if (this.protocolPluggableService.findDeviceProtocolPluggableClass(licensedProtocolRule.getClassName()) == null) {
                        this.protocolPluggableService.newDeviceProtocolPluggableClass(licensedProtocolRule.getClassName());
                        LOGGER.fine("Created pluggable class for " + licensedProtocolRule.getClassName());
                    }
                    else {
                        LOGGER.fine("Skipping " + licensedProtocolRule.getClassName() + ": already exists");
                    }
                }
                catch (RuntimeException e) {
                    if (e.getCause() != null) {
                        this.handleCreationException(licensedProtocolRule.getClassName(), e.getCause());
                    }
                    else {
                        this.handleCreationException(licensedProtocolRule.getClassName(), e);
                    }
                }
                catch (Exception e) {
                    this.handleCreationException(licensedProtocolRule.getClassName(), e);
                }
            }
        }
        catch (Exception e) {
            LOGGER.severe("Failed to register any device protocol: " + e);
        }
    }

    private void registerConnectionTypePluggableClasses() {
        try {
            for (PluggableClassDefinition definition : this.connectionTypeService.getExistingConnectionTypePluggableClasses()) {
                try {
                    if (this.protocolPluggableService.findConnectionTypePluggableClassByClassName(definition.getProtocolTypeClass().getName()) != null) {
                        ConnectionTypePluggableClass connectionTypePluggableClass =
                                this.protocolPluggableService.newConnectionTypePluggableClass(
                                        definition.getName(),
                                        definition.getProtocolTypeClass().getName());
                        connectionTypePluggableClass.save();
                        LOGGER.fine("Created pluggable class for " + definition.getProtocolTypeClass().getSimpleName());
                    }
                    else {
                        LOGGER.fine("Skipping " + definition.getProtocolTypeClass().getName() + ": already exists");
                    }
                }
                catch (RuntimeException e) {
                    if (e.getCause() != null) {
                        this.handleCreationException(definition, e.getCause());
                    }
                    else {
                        this.handleCreationException(definition, e);
                    }
                }
                catch (Exception e) {
                    this.handleCreationException(definition, e);
                }
            }
        }
        catch (Exception e) {
            LOGGER.severe("Failed to register any connection type pluggable class: " + e);
        }
    }

    private void handleCreationException(PluggableClassDefinition definition, Throwable e) {
        this.handleCreationException(definition.getProtocolTypeClass().getName(), e);
    }

    private void handleCreationException(String className, Throwable e) {
        LOGGER.warning("Failed to create pluggable class for " + className + ": " + e);
    }

    @Deactivate
    public void stop() throws Exception {
    }

}