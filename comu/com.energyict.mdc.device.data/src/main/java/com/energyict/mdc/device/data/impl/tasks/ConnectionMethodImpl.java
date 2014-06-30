package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.impl.CreateEventType;
import com.energyict.mdc.device.data.impl.DeleteEventType;
import com.energyict.mdc.device.data.impl.IdPluggableClassUsageImpl;
import com.energyict.mdc.device.data.impl.UpdateEventType;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskProperty;
import com.energyict.mdc.device.data.tasks.ConnectionTaskPropertyProvider;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import static com.energyict.mdc.protocol.pluggable.ConnectionTypePropertyRelationAttributeTypeNames.CONNECTION_METHOD_ATTRIBUTE_NAME;

/**
 * Provides an implementation for the {@link ConnectionMethod} interface.
 * When a ConnectionMethod is being constructed from the owning
 * ConnectionTask, then the ConnectionTask is injected with the constructor.
 * Each ConnectionMethod is referenced by a {@link ConnectionTask}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-31 (08:54)
 */
@ComPortPoolIsCompatibleWithConnectionType(groups = {Save.Create.class, Save.Update.class})
public class ConnectionMethodImpl extends IdPluggableClassUsageImpl<ConnectionMethod, ConnectionType, ConnectionTaskProperty>
        implements
            ConnectionMethod,
            ConnectionTaskPropertyProvider {

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.CONNECTION_METHOD_PLUGGABLE_CLASS_REQUIRED_KEY + "}")
    private ConnectionTypePluggableClass pluggableClass;
    private Reference<ConnectionTask<?,?>> connectionTask = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.CONNECTION_METHOD_COMPORT_POOL_REQUIRED_KEY + "}")
    private Reference<ComPortPool> comPortPool = ValueReference.absent();

    private ProtocolPluggableService protocolPluggableService;

    @Inject
    public ConnectionMethodImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, RelationService relationService, Clock clock, ProtocolPluggableService protocolPluggableService) {
        super(ConnectionMethod.class, dataModel, eventService, thesaurus, relationService, clock);
        this.protocolPluggableService = protocolPluggableService;
    }

    public ConnectionMethodImpl initialize (ConnectionTask connectionTask, ConnectionTypePluggableClass pluggableClass, ComPortPool comPortPool) {
        this.connectionTask.set(connectionTask);
        this.pluggableClass = pluggableClass;
        this.setPluggableClassId(pluggableClass.getId());
        this.comPortPool.set(comPortPool);
        return this;
    }

    @Override
    public void save () {
        this.getPluggableClass();   // Force load for the @NotNull annotation
        super.save();
    }

    @Override
    public void saveAllProperties() {
        // Make superclass' method public
        super.saveAllProperties();
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.CONNECTIONMETHOD;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.CONNECTIONMETHOD;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.CONNECTIONMETHOD;
    }

    @Override
    protected void validateDelete() {
        // Nothing to validate for now
    }

    @Override
    public void makeObsolete() {
        this.obsoleteAllProperties();
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        // Make the superclass' method public
        super.setProperty(propertyName, value);
    }

    @Override
    public void removeProperty(String propertyName) {
        // Make the superclass' method public
        super.removeProperty(propertyName);
    }

    @Override
    protected String getDefaultAttributeName() {
        return CONNECTION_METHOD_ATTRIBUTE_NAME;
    }

    @Override
    public ConnectionTaskProperty newProperty(String name, Object value, Date activeDate) {
        ConnectionTaskPropertyImpl property = new ConnectionTaskPropertyImpl(name);
        property.setValue(value);
        property.setActivePeriod(new Interval(activeDate, null));
        return property;
    }

    @Override
    protected ConnectionTaskProperty newPropertyFor(Relation relation, RelationAttributeType attributeType) {
        return new ConnectionTaskPropertyImpl(relation, attributeType.getName(), this.getPluggableClass());
    }

    @Override
    protected ConnectionTaskProperty newInheritedPropertyFor(String propertyName, Object propertyValue) {
        return new ConnectionTaskPropertyImpl(propertyName, propertyValue, this.always(), this.getPluggableClass());
    }

    @Override
    public TypedProperties getTypedProperties() {
        return super.getTypedProperties();
    }

    public ConnectionTask getConnectionTask() {
        return this.connectionTask.orNull();
    }

    @Override
    public ConnectionTypePluggableClass getPluggableClass() {
        if (this.pluggableClass == null) {
            this.loadPluggableClass();
        }
        return pluggableClass;
    }

    private void loadPluggableClass() {
        this.pluggableClass = this.findConnectionTypePluggableClass(this.getPluggableClassId());
    }

    @Override
    protected RelationType findRelationType() {
        return this.getPluggableClass().findRelationType();
    }

    private ConnectionTypePluggableClass findConnectionTypePluggableClass(long connectionTypePluggableClassId) {
        return this.protocolPluggableService.findConnectionTypePluggableClass(connectionTypePluggableClassId);
    }

    @Override
    protected List<PropertySpec> getPluggablePropetySpecs() {
        return this.getPluggableClass().getConnectionType().getPropertySpecs();
    }

    @Override
    public boolean allowsSimultaneousConnections() {
        return this.getPluggableClass().getConnectionType().allowsSimultaneousConnections();
    }

    @Override
    public ComPortPool getComPortPool() {
        return this.comPortPool.get();
    }

    @Override
    public boolean hasComPortPool() {
        return this.comPortPool.isPresent();
    }

    @Override
    public void setComPortPool(ComPortPool comPortPool) {
        this.comPortPool.set(comPortPool);
    }

}