package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.HardCodedFieldNames;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.elster.jupiter.util.conditions.Where.where;

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

    @SuppressWarnings("unchecked")
    <T extends PersistentDomainExtension<D>, D> Optional<T> getValuesEntityFor(D businessObject) {
        Condition condition =    where(this.customPropertySet.getPersistenceSupport().domainFieldName()).isEqualTo(businessObject)
                            .and(where(HardCodedFieldNames.CUSTOM_PROPERTY_SET.javaName()).isEqualTo(this.registeredCustomPropertySet));
        return this.getValuesEntityFor(condition, () -> "There should only be one set of property values for custom property set " + this.customPropertySet.getId() + " against business object " + businessObject);
    }

    @SuppressWarnings("unchecked")
    <T extends PersistentDomainExtension<D>, D> Optional<T> getValuesEntityFor(D businessObject, Instant effectiveTimestamp) {
        Condition condition =    where(this.customPropertySet.getPersistenceSupport().domainFieldName()).isEqualTo(businessObject)
                            .and(where(HardCodedFieldNames.CUSTOM_PROPERTY_SET.javaName()).isEqualTo(this.registeredCustomPropertySet))
                            .and(where(HardCodedFieldNames.INTERVAL.javaName()).isEffective(effectiveTimestamp));
        return this.getValuesEntityFor(condition, () -> "There should only be one set of property values for custom property set " + this.customPropertySet.getId() + " at " + effectiveTimestamp + " against business object " + businessObject);
    }

    @SuppressWarnings("unchecked")
    <T extends PersistentDomainExtension<D>, D> Optional<T> getValuesEntityFor(Condition condition, Supplier<String> errorMessageSupplier) {
        List<T> extensions =
                this.customPropertySetDataModel
                    .mapper(this.customPropertySet.getPersistenceSupport().persistenceClass())
                    .select(condition);
        if (extensions.isEmpty()) {
            return Optional.empty();
        }
        else if (extensions.size() > 1) {
            throw new IllegalStateException(errorMessageSupplier.get());
        }
        else {
            return Optional.of(extensions.get(0));
        }
    }

    <T extends PersistentDomainExtension<D>, D> void setValuesEntityFor(D businessObject, CustomPropertySetValues values) {
        Optional<T> domainExtension = this.getValuesEntityFor(businessObject);
        if (domainExtension.isPresent()) {
            this.updateExtension(domainExtension.get(), businessObject, values);
        }
        else {
            this.createExtension(businessObject, values);
        }
    }

    private <T extends PersistentDomainExtension<D>, D> void updateExtension(T domainExtension, D businessObject, CustomPropertySetValues values) {
        domainExtension.copyFrom(businessObject, values);
        Save.UPDATE.validate(this.customPropertySetDataModel, domainExtension);
        this.customPropertySetDataModel.update(domainExtension);
    }

    @SuppressWarnings("unchecked")
    private <T extends PersistentDomainExtension<D>, D> void createExtension(D businessObject, CustomPropertySetValues values) {
        T domainExtension = (T) this.customPropertySetDataModel.getInstance(this.customPropertySet.getPersistenceSupport().persistenceClass());
        DomainExtensionAccessor.setRegisteredCustomPropertySet(domainExtension, this.registeredCustomPropertySet);
        domainExtension.copyFrom(businessObject, values);
        Save.CREATE.validate(this.customPropertySetDataModel, domainExtension);
        this.customPropertySetDataModel.persist(domainExtension);
    }

    @SuppressWarnings("unchecked")
    private <T extends PersistentDomainExtension<D>, D> void createExtension(D businessObject, CustomPropertySetValues values, Instant effectiveTimestamp) {
        T domainExtension = (T) this.customPropertySetDataModel.getInstance(this.customPropertySet.getPersistenceSupport().persistenceClass());
        DomainExtensionAccessor.setRegisteredCustomPropertySet(domainExtension, this.registeredCustomPropertySet);
        domainExtension.copyFrom(businessObject, values);
        DomainExtensionAccessor.setInterval(domainExtension, Interval.startAt(effectiveTimestamp));
        Save.CREATE.validate(this.customPropertySetDataModel, domainExtension);
        this.customPropertySetDataModel.persist(domainExtension);
    }

    <T extends PersistentDomainExtension<D>, D> void setValuesEntityFor(D businessObject, CustomPropertySetValues values, Instant effectiveTimestamp) {
        Optional<T> domainExtension = this.getValuesEntityFor(businessObject, effectiveTimestamp);
        if (domainExtension.isPresent()) {
            Interval interval = DomainExtensionAccessor.getInterval(domainExtension.get());
            Range<Instant> range = interval.toClosedOpenRange();
            // Currently only support for editing the current value, so no changes on historical values that was supported by EIServer folder or relation technology
            if (range.hasUpperBound() || !range.contains(effectiveTimestamp)) {
                throw new UnsupportedOperationException(MessageSeeds.EDIT_HISTORICAL_VALUES_NOT_SUPPORTED.getDefaultFormat());
            }
            Interval updatedInterval = interval.withEnd(effectiveTimestamp);
            DomainExtensionAccessor.setInterval(domainExtension.get(), updatedInterval);
            this.customPropertySetDataModel.update(businessObject, HardCodedFieldNames.INTERVAL.javaName());
        }
        this.createExtension(businessObject, values, effectiveTimestamp);
    }

}