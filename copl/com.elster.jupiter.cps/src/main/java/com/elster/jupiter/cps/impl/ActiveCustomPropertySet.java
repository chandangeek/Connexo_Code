package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.HardCodedFieldNames;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Iterators;
import com.google.common.collect.Range;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
    private final Thesaurus thesaurus;
    private final DataModel customPropertySetDataModel;
    private final RegisteredCustomPropertySet registeredCustomPropertySet;

    ActiveCustomPropertySet(CustomPropertySet customPropertySet, Thesaurus thesaurus, DataModel customPropertySetDataModel, RegisteredCustomPropertySet registeredCustomPropertySet) {
        super();
        this.customPropertySet = customPropertySet;
        this.thesaurus = thesaurus;
        this.customPropertySetDataModel = customPropertySetDataModel;
        this.registeredCustomPropertySet = registeredCustomPropertySet;
    }

    CustomPropertySet getCustomPropertySet() {
        return customPropertySet;
    }

    private DataMapper getMapper() {
        return this.customPropertySetDataModel.mapper(this.customPropertySet.getPersistenceSupport().persistenceClass());
    }

    @SuppressWarnings("unchecked")
    <T extends PersistentDomainExtension<D>, D> Optional<T> getNonVersionedValuesEntityFor(D businessObject, Object... additionalPrimaryKeyColumnValues) {
        if (this.registeredCustomPropertySet.isViewableByCurrentUser()) {
            this.validateAdditionalPrimaryKeyValues(additionalPrimaryKeyColumnValues);
            Condition condition =
                    this.addAdditionalPrimaryKeyColumnConditionsTo(
                                 where(this.customPropertySet.getPersistenceSupport().domainFieldName()).isEqualTo(businessObject)
                            .and(where(HardCodedFieldNames.CUSTOM_PROPERTY_SET.javaName()).isEqualTo(this.registeredCustomPropertySet)),
                            additionalPrimaryKeyColumnValues);
            return this.getValuesEntityFor(condition, () -> "There should only be one set of property values for custom property set " + this.customPropertySet.getId() + " against business object " + businessObject);
        }
        else {
            return Optional.empty();
        }
    }

    private void validateAdditionalPrimaryKeyValues(Object... additionalPrimaryKeyValues) {
        long requiredNumberOfAdditionalPrimaryKeyValues = this.getAdditionalPrimaryKeyColumns().count();
        if (requiredNumberOfAdditionalPrimaryKeyValues != additionalPrimaryKeyValues.length) {
            throw new IllegalArgumentException(MessageFormat.format("Custom property set " + this.customPropertySet.getId() + " has {0} additional primay key column(s), you must specify a value for all but got only {1}", requiredNumberOfAdditionalPrimaryKeyValues, additionalPrimaryKeyValues.length));
        }
    }

    private Stream<? extends Column> getAdditionalPrimaryKeyColumns() {
        Table<?> table = this.customPropertySetDataModel.getTable(this.customPropertySet.getPersistenceSupport().tableName());
        if (table != null) {
            return table
                    .getPrimaryKeyColumns()
                    .stream()
                    .filter(pkColumn -> !HardCodedFieldNames.CUSTOM_PROPERTY_SET.databaseName().equals(pkColumn.getName()))
                    .filter(pkColumn -> this.customPropertySet.isVersioned() && !(HardCodedFieldNames.INTERVAL.javaName() + ".start").equals(pkColumn.getFieldName()))
                    .filter(pkColumn -> !pkColumn.getName().equals(this.customPropertySet.getPersistenceSupport().domainColumnName()));
        }
        else {
            return Stream.empty();
        }
    }

    private Condition addAdditionalPrimaryKeyColumnConditionsTo(Condition mainCondition, Object... additionalPrimaryKeyColumnValues) {
        Iterator<Object> pkValueInterator = Iterators.forArray(additionalPrimaryKeyColumnValues);
        return mainCondition.and(
                this.getAdditionalPrimaryKeyColumns()
                        .map(pkColumn -> where(pkColumn.getFieldName()).isEqualTo(pkValueInterator.next()))
                        .reduce(
                                Condition.TRUE,
                                Condition::and));
    }

    @SuppressWarnings("unchecked")
    <T extends PersistentDomainExtension<D>, D> List<T> getNonVersionedValuesEntityFor(Condition condition) {
        if (this.registeredCustomPropertySet.isViewableByCurrentUser()) {
            return this.getMapper().select(condition);
        }
        else {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    <T extends PersistentDomainExtension<D>, D> List<T> getVersionedValuesEntityFor(Condition condition, Instant effectiveTimestamp) {
        if (this.registeredCustomPropertySet.isViewableByCurrentUser()) {
            return this.getMapper().select(condition.and(where(HardCodedFieldNames.INTERVAL.javaName()).isEffective(effectiveTimestamp)));
        }
        else {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    <T extends PersistentDomainExtension<D>, D> Optional<T> getVersionedValuesEntityFor(D businessObject, Instant effectiveTimestamp, Object... additionalPrimaryKeyColumnValues) {
        if (this.registeredCustomPropertySet.isViewableByCurrentUser()) {
            this.validateAdditionalPrimaryKeyValues(additionalPrimaryKeyColumnValues);
            Condition condition =
                    this.addAdditionalPrimaryKeyColumnConditionsTo(
                                 where(this.customPropertySet.getPersistenceSupport().domainFieldName()).isEqualTo(businessObject)
                            .and(where(HardCodedFieldNames.CUSTOM_PROPERTY_SET.javaName()).isEqualTo(this.registeredCustomPropertySet))
                            .and(where(HardCodedFieldNames.INTERVAL.javaName()).isEffective(effectiveTimestamp)),
                            additionalPrimaryKeyColumnValues);
            return this.getValuesEntityFor(condition, () -> "There should only be one set of property values for custom property set " + this.customPropertySet.getId() + " at " + effectiveTimestamp + " against business object " + businessObject);
        }
        else {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    <T extends PersistentDomainExtension<D>, D> List<T> getAllValuesEntityFor(D businessObject) {
        Condition condition =    where(this.customPropertySet.getPersistenceSupport().domainFieldName()).isEqualTo(businessObject)
                            .and(where(HardCodedFieldNames.CUSTOM_PROPERTY_SET.javaName()).isEqualTo(this.registeredCustomPropertySet));
        return this.getMapper().select(condition);
    }

    @SuppressWarnings("unchecked")
    <T extends PersistentDomainExtension<D>, D> Optional<T> getValuesEntityFor(Condition condition, Supplier<String> errorMessageSupplier) {
        List<T> extensions = this.getMapper().select(condition);
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
        Optional<T> domainExtension = this.getNonVersionedValuesEntityFor(businessObject);
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
        Optional<T> domainExtension = this.getVersionedValuesEntityFor(businessObject, effectiveTimestamp);
        if (domainExtension.isPresent()) {
            Interval interval = DomainExtensionAccessor.getInterval(domainExtension.get());
            Range<Instant> range = interval.toClosedOpenRange();
            // Currently only support for editing the current value, so no changes on historical values that was supported by EIServer folder or relation technology
            if (range.hasUpperBound() || !range.contains(effectiveTimestamp)) {
                throw new UnsupportedOperationException(MessageSeeds.EDIT_HISTORICAL_VALUES_NOT_SUPPORTED.getDefaultFormat());
            }
            Interval updatedInterval = interval.withEnd(effectiveTimestamp);
            DomainExtensionAccessor.setInterval(domainExtension.get(), updatedInterval);
            this.customPropertySetDataModel.update(domainExtension.get(), HardCodedFieldNames.INTERVAL.javaName() + ".end");
        }
        this.createExtension(businessObject, values, effectiveTimestamp);
    }

    @SuppressWarnings("unchecked")
    public <T extends PersistentDomainExtension<D>, D> void deleteExtensions(D businessObject) {
        if (this.customPropertySet.isVersioned()) {
            this.getMapper().remove(this.getAllValuesEntityFor(businessObject));
        }
        else {
            this.getNonVersionedValuesEntityFor(businessObject).ifPresent(this::delete);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends PersistentDomainExtension<D>, D> void delete(T extension) {
        this.getMapper().remove(extension);
    }

    public void validateCurrentUserIsAllowedToEdit() {
        if (!this.registeredCustomPropertySet.isEditableByCurrentUser()) {
            throw new CurrentUserIsNotAllowedToEditValuesOfCustomPropertySetException(this.thesaurus);
        }
    }

}