package com.energyict.mdc.engine.config.impl;

import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.engine.config.ComPortPoolProperty;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.config.InboundComPortPool} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-26 (10:21)
 */
@ComPortPoolTypeMatchesComPortType(groups = {Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_COM_PORT_TYPE_OF_COM_PORT_DOES_NOT_MATCH_WITH_COM_PORT_POOL+"}")
public final class InboundComPortPoolImpl extends ComPortPoolImpl implements InboundComPortPool {

    public static final String FIELD_DISCOVEYPROTOCOL = "discoveryProtocolPluggableClassId";

    private final EngineConfigurationService engineConfigurationService;
    private final ProtocolPluggableService pluggableService;
    @Min(value =1, groups = {Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
    private long discoveryProtocolPluggableClassId;
    @Valid
    private List<ComPortPoolPropertyImpl> properties = new ArrayList<>();
    private List<String> addedOrRemovedRequiredProperties = new ArrayList<>();

    @Inject
    protected InboundComPortPoolImpl(DataModel dataModel, EventService eventService, EngineConfigurationService engineConfigurationService, Thesaurus thesaurus, ProtocolPluggableService pluggableService) {
        super(dataModel, thesaurus, eventService);
        this.engineConfigurationService = engineConfigurationService;
        this.pluggableService = pluggableService;
    }

    InboundComPortPoolImpl initialize(String name, ComPortType comPortType, InboundDeviceProtocolPluggableClass discoveryProtocol) {
        this.setName(name);
        this.setComPortType(comPortType);
        this.setDiscoveryProtocolPluggableClass(discoveryProtocol);
        return this;
    }

    @Override
    public boolean isInbound() {
        return true;
    }

    @Override
    public List<InboundComPort> getComPorts() {
        return engineConfigurationService.findInboundInPool(this);
    }

    @Override
    public InboundDeviceProtocolPluggableClass getDiscoveryProtocolPluggableClass() {
        return pluggableService.findInboundDeviceProtocolPluggableClass(discoveryProtocolPluggableClassId).get();
    }

    @Override
    public void setDiscoveryProtocolPluggableClass(InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass) {
        if (inboundDeviceProtocolPluggableClass !=null) {
            this.discoveryProtocolPluggableClassId = inboundDeviceProtocolPluggableClass.getId();
        } else {
            this.discoveryProtocolPluggableClassId = 0;
        }
    }

    @Override
    public List<ComPortPoolProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    @Override
    public void setProperty(String key, Object value) {
        for (ComPortPoolPropertyImpl property : properties) {
            if (property.getName().equals(key)) {
                property.setValue(value);
                if (this.getId() != 0) {
                    Save.UPDATE.validate(dataModel, property);
                    property.save();
                }
                return;
            }
        }
        ComPortPoolPropertyImpl property = ComPortPoolPropertyImpl.from(dataModel, this, key, value);
        Save.CREATE.validate(dataModel, property);
        properties.add(property);
        if (property.isRequired()) {
            if (this.addedOrRemovedRequiredProperties.contains(key)) {
                // The property was removed in the same edit session
                this.addedOrRemovedRequiredProperties.remove(key);
            }
            else {
                this.addedOrRemovedRequiredProperties.add(key);
            }
        }
    }

    @Override
    public void clearProperties() {
        properties.clear();
    }

    @Override
    public TypedProperties getTypedProperties() {
        TypedProperties typedProperties = TypedProperties.inheritingFrom(getDiscoveryProtocolPluggableClass().getProperties(getDiscoveryProtocolPluggableClass().getInboundDeviceProtocol().getPropertySpecs()));
        this.getProperties()
                .stream()
                .filter(p -> p.getValue() != null)
                .forEach(p -> typedProperties.setProperty(p.getName(), p.getValue()));
        return typedProperties.getUnmodifiableView();
    }

    public void removeProperty(String key) {
        for (Iterator<ComPortPoolPropertyImpl> iterator = properties.iterator(); iterator.hasNext(); ) {
            ComPortPoolPropertyImpl property = iterator.next();
            if (property.getName().equals(key)) {
                if (property.isRequired()) {
                    if (this.addedOrRemovedRequiredProperties.contains(key)) {
                        this.addedOrRemovedRequiredProperties.remove(key);
                    }
                    else {
                        this.addedOrRemovedRequiredProperties.add(key);
                    }
                }
                iterator.remove();
                return;
            }
        }
    }

    @Override
    public void save() {
        super.save();
        this.addedOrRemovedRequiredProperties.clear();
    }

    protected void validate() {
        super.validate();
        this.validateDiscoveryProtocolPluggableClass(this.discoveryProtocolPluggableClassId);
    }

    private void validateDiscoveryProtocolPluggableClass(long discoveryProtocolPluggableClassId) {
        if (discoveryProtocolPluggableClassId == 0) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.MUST_HAVE_DISCOVERY_PROTOCOL);
        } else {
            pluggableService
                    .findDeviceProtocolPluggableClass(discoveryProtocolPluggableClassId)
                    .orElseThrow(() -> new TranslatableApplicationException(thesaurus, MessageSeeds.NO_SUCH_PLUGGABLE_CLASS));
        }
    }

    protected void validateDelete() {
        super.validateDelete();
        this.validateNotUsedByComPorts();
        clearProperties();
    }

    @Override
    protected void validateMakeObsolete() {
        super.validateMakeObsolete();
        this.validateNotUsedByComPorts();
    }

    private void validateNotUsedByComPorts() {
        List<InboundComPort> comPorts = this.getComPorts();
        if (!comPorts.isEmpty()) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.COMPORTPOOL_STILL_REFERENCED);
        }
    }

    @Override
    protected void makeMembersObsolete() {
        /* Can only be made obsolete if there are no members
         * so nothing to do here. */
    }

}