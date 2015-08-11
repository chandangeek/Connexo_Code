package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.DataModel;

import java.time.Instant;
import java.util.Optional;

/**
 * Models a {@link CustomPropertySet} that registered on the whiteboard
 * and that was registered in the database and is therefore currently active.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-10 (14:54)
 */
class ActiveCustomPropertySet {

    private final CustomPropertySet customPropertySet;
    private final DataModel customPropertySetDataModel;
    private final RegisteredCustomPropertySet registeredCustomPropertySet;

    ActiveCustomPropertySet(CustomPropertySet customPropertySet, DataModel customPropertySetDataModel, RegisteredCustomPropertySet registeredCustomPropertySet) {
        super();
        this.customPropertySet = customPropertySet;
        this.customPropertySetDataModel = customPropertySetDataModel;
        this.registeredCustomPropertySet = registeredCustomPropertySet;
    }

    CustomPropertySet getCustomPropertySet() {
        return customPropertySet;
    }

    RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet;
    }

    @SuppressWarnings("unchecked")
    public <T extends PersistentDomainExtension<D>, D> Optional<T> getValuesEntityFor(D businessObject) {
        return this.customPropertySetDataModel
                .mapper(this.customPropertySet.getPersistenceSupport().getPersistenceClass())
                .getOptional(
                        businessObject,
                        this.registeredCustomPropertySet);
    }

    @SuppressWarnings("unchecked")
    public <T extends PersistentDomainExtension<D>, D> Optional<T> getValuesEntityFor(D businessObject, Instant effectiveTimestamp) {
        return this.customPropertySetDataModel
                .mapper(this.customPropertySet.getPersistenceSupport().getPersistenceClass())
                .getOptional(
                        businessObject,
                        this.registeredCustomPropertySet,
                        effectiveTimestamp);
    }

}