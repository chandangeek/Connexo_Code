package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.license.License;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
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

import java.util.logging.Level;
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
    private volatile TransactionService transactionService;

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

    public TransactionService getTransactionService() {
        return transactionService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void start(License license) {
        LOGGER.info("Registering pluggable classes...");
        registerInboundDeviceProtocolPluggableClasses();
        registerDeviceProtocolPluggableClasses(license);
        registerConnectionTypePluggableClasses();
        LOGGER.info("Finished registering pluggable classes...");

    }

    private void registerInboundDeviceProtocolPluggableClasses() {
        try {
            for (PluggableClassDefinition definition : inboundDeviceProtocolService.getExistingInboundDeviceProtocolPluggableClasses()) {
                if (this.inboundDeviceProtocolDoesNotExist(definition)) {
                    this.createInboundDeviceProtocol(definition);
                    LOGGER.fine("Created pluggable class for " + definition.getProtocolTypeClass().getSimpleName());
                }
                else {
                    LOGGER.fine("Skipping " + definition.getProtocolTypeClass().getName() + ": already exists");
                }
            }
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to register any inbound device protocol pluggable class: " + e, e);
        }
    }

    private boolean inboundDeviceProtocolDoesNotExist(PluggableClassDefinition definition) {
        return this.protocolPluggableService.findInboundDeviceProtocolPluggableClassByClassName(definition.getProtocolTypeClass().getName()).isEmpty();
    }

    private InboundDeviceProtocolPluggableClass createInboundDeviceProtocol(final PluggableClassDefinition definition) {
        return this.transactionService.execute(new Transaction<InboundDeviceProtocolPluggableClass>() {
            @Override
            public InboundDeviceProtocolPluggableClass perform() {
                try {
                    InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass =
                            protocolPluggableService.newInboundDeviceProtocolPluggableClass(
                                    definition.getName(),
                                    definition.getProtocolTypeClass().getName());
                    inboundDeviceProtocolPluggableClass.save();
                    return inboundDeviceProtocolPluggableClass;
                }
                catch (RuntimeException e) {
                    if (e.getCause() != null) {
                        handleCreationException(definition, e.getCause());
                    }
                    else {
                        handleCreationException(definition, e);
                    }
                    return null;
                }
                catch (Exception e) {
                    handleCreationException(definition, e);
                    return null;
                }
            }
        });
    }

    private void registerDeviceProtocolPluggableClasses(License license) {
        try {
            for (LicensedProtocol licensedProtocolRule : this.licensedProtocolService.getAllLicensedProtocols(license)) {
                if (this.deviceProtocolDoesNotExist(licensedProtocolRule)) {
                    this.createDeviceProtocol(licensedProtocolRule);
                    LOGGER.fine("Created pluggable class for " + licensedProtocolRule.getClassName());
                }
                else {
                    LOGGER.fine("Skipping " + licensedProtocolRule.getClassName() + ": already exists");
                }
            }
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to register any device protocol: " + e, e);
        }
    }

    private boolean deviceProtocolDoesNotExist(LicensedProtocol licensedProtocolRule) {
        return this.protocolPluggableService.findDeviceProtocolPluggableClass(licensedProtocolRule.getClassName()).isEmpty();
    }

    private DeviceProtocolPluggableClass createDeviceProtocol(final LicensedProtocol licensedProtocolRule) {
        return this.transactionService.execute(new Transaction<DeviceProtocolPluggableClass>() {
            @Override
            public DeviceProtocolPluggableClass perform() {
                try {
                    return protocolPluggableService.newDeviceProtocolPluggableClass(licensedProtocolRule.getName(), licensedProtocolRule.getClassName());
                }
                catch (RuntimeException e) {
                    if (e.getCause() != null) {
                        handleCreationException(licensedProtocolRule.getClassName(), e.getCause());
                    }
                    else {
                        handleCreationException(licensedProtocolRule.getClassName(), e);
                    }
                    return null;
                }
                catch (Exception e) {
                    handleCreationException(licensedProtocolRule.getClassName(), e);
                    return null;
                }
            }
        });
    }

    private void registerConnectionTypePluggableClasses() {
        try {
            for (PluggableClassDefinition definition : this.connectionTypeService.getExistingConnectionTypePluggableClasses()) {
                if (this.connectionTypeDoesNotExist(definition)) {
                    this.createConnectionType(definition);
                    LOGGER.fine("Created pluggable class for " + definition.getProtocolTypeClass().getSimpleName());
                }
                else {
                    LOGGER.fine("Skipping " + definition.getProtocolTypeClass().getName() + ": already exists");
                }
            }
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to register any connection type pluggable class: " + e, e);
        }
    }

    private ConnectionTypePluggableClass createConnectionType(final PluggableClassDefinition definition) {
        return this.transactionService.execute(new Transaction<ConnectionTypePluggableClass>() {
            @Override
            public ConnectionTypePluggableClass perform() {
                try {
                    ConnectionTypePluggableClass connectionTypePluggableClass =
                            protocolPluggableService.newConnectionTypePluggableClass(
                                    definition.getName(),
                                    definition.getProtocolTypeClass().getName());
                    connectionTypePluggableClass.save();
                    return connectionTypePluggableClass;
                }
                catch (RuntimeException e) {
                    if (e.getCause() != null) {
                        handleCreationException(definition, e.getCause());
                    }
                    else {
                        handleCreationException(definition, e);
                    }
                    return null;
                }
                catch (Exception e) {
                    handleCreationException(definition, e);
                    return null;
                }
            }
        });
    }

    private boolean connectionTypeDoesNotExist(PluggableClassDefinition definition) {
        return this.protocolPluggableService.findConnectionTypePluggableClassByClassName(definition.getProtocolTypeClass().getName()).isEmpty();
    }

    private void handleCreationException(PluggableClassDefinition definition, Throwable e) {
        this.handleCreationException(definition.getProtocolTypeClass().getName(), e);
    }

    private void handleCreationException(String className, Throwable e) {
        LOGGER.log(Level.SEVERE, "Failed to create pluggable class for " + className + ": " + e, e);
    }

    @Deactivate
    public void stop() throws Exception {
    }

}