package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.IComPortPoolProperty;
import com.energyict.mdc.engine.config.InboundComPortPool;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;

import com.google.inject.Inject;

import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Optional;

@ComportPoolPropertyMustHaveSpec(groups = {Save.Create.class, Save.Update.class})
@ComportPoolPropertyValueHasCorrectType(groups = {Save.Create.class, Save.Update.class})
public class ComPortPoolPropertyImpl implements IComPortPoolProperty {

    private final DataModel dataModel;

    private Reference<ComPortPool> comPortPool = ValueReference.absent();
    @Size(max = 255, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MDC_FIELD_TOO_LONG + "}")
    private String name;
    @Size(max = 4000, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MDC_FIELD_TOO_LONG + "}")
    private String value;
    private transient Object objectValue;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Inject
    ComPortPoolPropertyImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    static ComPortPoolPropertyImpl from(DataModel dataModel, ComPortPool comPortPool, String name, Object value) {
        ComPortPoolPropertyImpl comPortPoolProperty = new ComPortPoolPropertyImpl(dataModel);
        comPortPoolProperty.comPortPool.set(comPortPool);
        comPortPoolProperty.name = name;
        comPortPoolProperty.setValue(value);
        return comPortPoolProperty;
    }

    private Object getValueObjectFromStringValue(String propertyStringValue) {
        Optional<PropertySpec> propertySpec = this.getPropertySpec();
        if (propertySpec.isPresent()) {
            return propertySpec.get().getValueFactory().fromStringValue(propertyStringValue);
        }
        return null;
    }

    private String asStringValue(Object value) {
        Optional<PropertySpec> propertySpec = this.getPropertySpec();
        if (propertySpec.isPresent()) {
            ValueFactory valueFactory = propertySpec.get().getValueFactory();
            if (valueFactory.getValueType().isInstance(value)) {
                return valueFactory.toStringValue(value);
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getValue() {
        if (objectValue == null) {
            objectValue = this.getValueObjectFromStringValue(value);
        }
        return objectValue;
    }

    public ComPortPool getComPortPool() {
        return comPortPool.get();
    }

    void setValue(Object value) {
        objectValue = value;
        this.value = asStringValue(value);
    }

    boolean isRequired() {
        Optional<PropertySpec> propertySpec = this.getPropertySpec();
        return propertySpec.isPresent() && propertySpec.get().isRequired();
    }

    private Optional<PropertySpec> getPropertySpec() {
        if(this.getComPortPool().isInbound()) {
            InboundComPortPool inboundPool = (InboundComPortPool) this.comPortPool.get();
            return inboundPool.getDiscoveryProtocolPluggableClass().getInboundDeviceProtocol().getPropertySpec(name);
        }
        return Optional.empty();
    }

    @Override
    public void save() {
        dataModel.mapper(ComPortPoolPropertyImpl.class).update(this);
    }
}
