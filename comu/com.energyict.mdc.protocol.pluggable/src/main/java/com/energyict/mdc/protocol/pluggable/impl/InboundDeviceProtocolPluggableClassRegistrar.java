/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Registers {@link InboundDeviceProtocolPluggableClass}es.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-09 (13:56)
 */
public class InboundDeviceProtocolPluggableClassRegistrar extends PluggableClassRegistrar {

    private final ProtocolPluggableService protocolPluggableService;
    private final TransactionService transactionService;

    public InboundDeviceProtocolPluggableClassRegistrar(ProtocolPluggableService protocolPluggableService, TransactionService transactionService) {
        super();
        this.protocolPluggableService = protocolPluggableService;
        this.transactionService = transactionService;
    }

    public void registerAll(List<InboundDeviceProtocolService> inboundDeviceProtocolServices) {
        for (InboundDeviceProtocolService inboundDeviceProtocolService : inboundDeviceProtocolServices) {
            Collection<PluggableClassDefinition> pluggableClasses = inboundDeviceProtocolService.getExistingInboundDeviceProtocolPluggableClasses();
            Iterator<PluggableClassDefinition> pluggableClassDefinitionIterator = pluggableClasses.iterator();
            while (pluggableClassDefinitionIterator.hasNext()) {
                PluggableClassDefinition definition = pluggableClassDefinitionIterator.next();
                try {
                    if (this.inboundDeviceProtocolDoesNotExist(definition)) {
                        this.createInboundDeviceProtocol(definition);
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
            this.completed(pluggableClasses.size(), "discovery protocol");
        }
    }

    private boolean inboundDeviceProtocolDoesNotExist(PluggableClassDefinition definition) {
        return this.protocolPluggableService.findInboundDeviceProtocolPluggableClassByClassName(definition.getProtocolTypeClass().getName()).isEmpty();
    }

    private InboundDeviceProtocolPluggableClass createInboundDeviceProtocol(PluggableClassDefinition definition) {
        return this.transactionService.execute(() -> this.doCreateInboundDeviceProtocol(definition));
    }

    private InboundDeviceProtocolPluggableClass doCreateInboundDeviceProtocol(PluggableClassDefinition definition) {
        InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass =
                protocolPluggableService.newInboundDeviceProtocolPluggableClass(
                        definition.getName(),
                        definition.getProtocolTypeClass().getName());
        inboundDeviceProtocolPluggableClass.save();
        return inboundDeviceProtocolPluggableClass;
    }

}