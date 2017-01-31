/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.LicensedProtocol;
import com.energyict.mdc.protocol.api.tasks.support.DeviceDescriptionSupport;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Registers {@link DeviceProtocolPluggableClass}es.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-09 (13:21)
 */
public class DeviceProtocolPluggableClassRegistrar extends PluggableClassRegistrar {

    private final ServerProtocolPluggableService protocolPluggableService;
    private final TransactionService transactionService;
    private final MeteringService meteringService;

    public DeviceProtocolPluggableClassRegistrar(ServerProtocolPluggableService protocolPluggableService, TransactionService transactionService, MeteringService meteringService) {
        super();
        this.protocolPluggableService = protocolPluggableService;
        this.transactionService = transactionService;
        this.meteringService = meteringService;
    }

    public void registerAll(List<LicensedProtocol> licensedProtocols) {
        Iterator<LicensedProtocol> licensedProtocolIterator = licensedProtocols.iterator();
        boolean registerNext = true;
        while (registerNext && licensedProtocolIterator.hasNext()) {
            LicensedProtocol licensedProtocol = licensedProtocolIterator.next();
            try {
                if (this.deviceProtocolDoesNotExist(licensedProtocol)) {
                    this.createDeviceProtocol(licensedProtocol);
                    this.created(licensedProtocol);
                }
                else {
                    this.alreadyExists(licensedProtocol);
                }
            }
            catch (NoServiceFoundThatCanLoadTheJavaClass e) {
                this.logWarning(() -> e.getMessage() + "; will retry later");
                registerNext = false;
            }
            catch (RuntimeException e) {
                this.creationFailed(licensedProtocol);
                if (e.getCause() != null) {
                    handleCreationException(licensedProtocol.getClassName(), e.getCause());
                }
                else {
                    handleCreationException(licensedProtocol.getClassName(), e);
                }
            }
            catch (Exception e) {
                this.logError(() -> "Failure to register device protocol " + toLogMessage(licensedProtocol) + "see error message below:");
                handleCreationException(licensedProtocol.getClassName(), e);
            }
        }
        this.completed(licensedProtocols.size(), "device protocol");
    }

    private boolean deviceProtocolDoesNotExist(LicensedProtocol licensedProtocolRule) {
        return this.protocolPluggableService.findDeviceProtocolPluggableClassesByClassName(licensedProtocolRule.getClassName()).isEmpty();
    }

    private DeviceProtocolPluggableClass createDeviceProtocol(LicensedProtocol licensedProtocol) {
        return this.transactionService.execute(() -> doCreateDeviceProtocol(licensedProtocol));
    }

    private DeviceProtocolPluggableClass doCreateDeviceProtocol(LicensedProtocol licensedProtocol) {
        Object protocol = protocolPluggableService.createProtocol(licensedProtocol.getClassName());
        String description;
        if (protocol instanceof DeviceDescriptionSupport) {
            description = ((DeviceDescriptionSupport) protocol).getProtocolDescription();
        } else {
            description = licensedProtocol.getName();
        }

        DeviceProtocolPluggableClass pluggableClass = this.protocolPluggableService.newDeviceProtocolPluggableClass(description, licensedProtocol.getClassName());
        pluggableClass.getDeviceProtocol().supportedEventTypes().stream().forEach(this::findOrCreateEndDeviceEventType);
        return pluggableClass;
    }

    private void findOrCreateEndDeviceEventType(String eventTypeMRID) {
        Optional<EndDeviceEventType> eventType = this.meteringService.getEndDeviceEventType(eventTypeMRID);
        if (!eventType.isPresent()) {
            this.meteringService.createEndDeviceEventType(eventTypeMRID);
        }
    }

    @Override
    protected void alreadyExists(LicensedProtocol licensedProtocol) {
        super.alreadyExists(licensedProtocol);
        long start = Instant.now().toEpochMilli();
        this.protocolPluggableService.registerDeviceProtocolPluggableClassAsCustomPropertySet(licensedProtocol.getClassName());
        long stop = Instant.now().toEpochMilli();
        long registrationTime = stop - start;
        if (registrationTime > 1000) {
            this.logWarning(() -> "Registration of custom property set for device protocol " + licensedProtocol.getClassName() + " took excessively long: " +  registrationTime + " (ms)");
        }
    }

}