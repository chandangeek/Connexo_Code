/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Registers {@link ConnectionTypePluggableClass}es.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-09 (14:12)
 */
class ConnectionTypePluggableClassRegistrar extends PluggableClassRegistrar {

    private final ServerProtocolPluggableService protocolPluggableService;
    private final TransactionService transactionService;

    ConnectionTypePluggableClassRegistrar(ServerProtocolPluggableService protocolPluggableService, TransactionService transactionService) {
        super();
        this.protocolPluggableService = protocolPluggableService;
        this.transactionService = transactionService;
    }

    void registerAll(List<ConnectionTypeService> connectionTypeServices) {
        for (ConnectionTypeService connectionTypeService : connectionTypeServices) {
            Collection<PluggableClassDefinition> pluggableClasses = connectionTypeService.getExistingConnectionTypePluggableClasses();
            Iterator<PluggableClassDefinition> pluggableClassDefinitionIterator = pluggableClasses.iterator();
            while (pluggableClassDefinitionIterator.hasNext()) {
                PluggableClassDefinition definition = pluggableClassDefinitionIterator.next();
                try {
                    if (this.connectionTypeDoesNotExist(definition)) {
                        this.createConnectionType(definition);
                        this.created(definition);
                    }
                    else {
                        this.alreadyExists(definition);
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
            this.completed(pluggableClasses.size(), "connection type");
        }
    }

    private ConnectionTypePluggableClass createConnectionType(PluggableClassDefinition definition) {
        return this.transactionService.execute(() -> this.doCreateConnectionType(definition));
    }

    private ConnectionTypePluggableClass doCreateConnectionType(PluggableClassDefinition definition) {
        return this.protocolPluggableService.newConnectionTypePluggableClass(
                definition.getName(),
                definition.getProtocolTypeClass().getName());
    }

    private boolean connectionTypeDoesNotExist(PluggableClassDefinition definition) {
        return this.protocolPluggableService.findConnectionTypePluggableClassByClassName(definition.getProtocolTypeClass().getName()).isEmpty();
    }

    @Override
    protected void alreadyExists(final PluggableClassDefinition definition) {
        super.alreadyExists(definition);
        long start = Instant.now().toEpochMilli();
        this.transactionService.execute(() -> this.registerConnectionTypePluggableClassAsCustomPropertySet(definition));
        long stop = Instant.now().toEpochMilli();
        long registrationTime = stop - start;
        if (registrationTime > 1000) {
            this.logWarning(() -> "Registration of custom property set for connection type " + definition.getProtocolTypeClass().getName() + " took excessively long: " + registrationTime + " (ms)");
        }
    }

    private PluggableClassDefinition registerConnectionTypePluggableClassAsCustomPropertySet(PluggableClassDefinition definition) {
        this.protocolPluggableService.registerConnectionTypePluggableClassAsCustomPropertySet(definition.getProtocolTypeClass().getName());
        return definition;
    }

}