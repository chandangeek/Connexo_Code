package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.dynamic.NoFinderComponentFoundException;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import java.util.Iterator;
import java.util.List;

/**
 * Registers {@link ConnectionTypePluggableClass}es.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-09 (14:12)
 */
public class ConnectionTypePluggableClassRegistrar extends PluggableClassRegistrar {

    private final ProtocolPluggableService protocolPluggableService;
    private final TransactionService transactionService;

    public ConnectionTypePluggableClassRegistrar(ProtocolPluggableService protocolPluggableService, TransactionService transactionService) {
        super();
        this.protocolPluggableService = protocolPluggableService;
        this.transactionService = transactionService;
    }

    public void registerAll(List<ConnectionTypeService> connectionTypeServices) {
        for (ConnectionTypeService connectionTypeService : connectionTypeServices) {
            boolean registerNext = true;
            Iterator<PluggableClassDefinition> pluggableClassDefinitionIterator = connectionTypeService.getExistingConnectionTypePluggableClasses().iterator();
            while (registerNext && pluggableClassDefinitionIterator.hasNext()) {
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
                catch (NoFinderComponentFoundException e) {
                    this.factoryComponentMissing();
                    registerNext = false;
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

}