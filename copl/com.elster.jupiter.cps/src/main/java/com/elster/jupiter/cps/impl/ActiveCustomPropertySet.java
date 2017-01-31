/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.HardCodedFieldNames;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.SqlFragment;
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

    DataModel getDataModel() {
        return this.customPropertySetDataModel;
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
                    .filter(pkColumn -> !(HardCodedFieldNames.INTERVAL.javaName() + ".start").equals(pkColumn.getFieldName()))
                    .filter(pkColumn -> !pkColumn.getName().equals(this.customPropertySet.getPersistenceSupport().domainColumnName()));
        }
        else {
            return Stream.empty();
        }
    }

    private Condition addAdditionalPrimaryKeyColumnConditionsTo(Condition mainCondition, Object... additionalPrimaryKeyColumnValues) {
        Iterator<Object> pkValueIterator = Iterators.forArray(additionalPrimaryKeyColumnValues);
        return mainCondition.and(
                this.getAdditionalPrimaryKeyColumns()
                        .map(pkColumn -> where(this.fieldNameFor(pkColumn)).isEqualTo(pkValueIterator.next()))
                        .reduce(
                            Condition.TRUE,
                            Condition::and));
    }

    private String fieldNameFor(Column pkColumn) {
        String fieldName = pkColumn.getFieldName();
        if (fieldName == null) {
            // No field name configured on the column, maybe this column is also be part of a foreign key
            return pkColumn
                        .getTable()
                        .getForeignKeyConstraints()
                        .stream()
                        .filter(fkc -> fkc.hasColumn(pkColumn))
                        .findAny()
                        .map(ForeignKeyConstraint::getFieldName)
                        .orElseThrow(() -> new IllegalStateException("Additional primary key column " + pkColumn.getName() + " has no mapped field name and is not part of a foreign key constraint that provides a mapped field name"));
        }
        else {
            return fieldName;
        }
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
        return this.getVersionedValuesEntityFor(businessObject, false, effectiveTimestamp, additionalPrimaryKeyColumnValues);
    }

    @SuppressWarnings("unchecked")
    <T extends PersistentDomainExtension<D>, D> Optional<T> getVersionedValuesEntityFor(D businessObject, boolean ignorePrivileges, Instant effectiveTimestamp, Object... additionalPrimaryKeyColumnValues) {
        if (ignorePrivileges || this.registeredCustomPropertySet.isViewableByCurrentUser()) {
            this.validateAdditionalPrimaryKeyValues(additionalPrimaryKeyColumnValues);
            Condition condition =
                    this.addAdditionalPrimaryKeyColumnConditionsTo(
                                 where(this.customPropertySet.getPersistenceSupport().domainFieldName()).isEqualTo(businessObject)
                            .and(where(HardCodedFieldNames.CUSTOM_PROPERTY_SET.javaName()).isEqualTo(this.registeredCustomPropertySet))
                            .and(where(HardCodedFieldNames.INTERVAL.javaName()).isEffective(effectiveTimestamp)),
                            additionalPrimaryKeyColumnValues);
            return this.getValuesEntityFor(
                    condition,
                    () -> "There should only be one set of property values for custom property set " + this.customPropertySet.getId() + " at " + effectiveTimestamp + " against business object " + businessObject);
        }
        else {
            return Optional.empty();
        }
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

    @SuppressWarnings("unchecked")
    <T extends PersistentDomainExtension<D>, D> List<T> getAllNonVersionedValuesEntitiesFor(D businessObject, Object... additionalPrimaryKeyColumnValues) {
        if (this.registeredCustomPropertySet.isViewableByCurrentUser()) {
            return this.getAllValuesEntityFor(businessObject, additionalPrimaryKeyColumnValues);
        }
        else {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    <T extends PersistentDomainExtension<D>, D> List<T> getAllValuesEntityFor(D businessObject, Object... additionalPrimaryKeyColumnValues) {
        this.validateAdditionalPrimaryKeyValues(additionalPrimaryKeyColumnValues);
        Condition condition =
                this.addAdditionalPrimaryKeyColumnConditionsTo(
                        where(this.customPropertySet.getPersistenceSupport().domainFieldName()).isEqualTo(businessObject)
                                .and(where(HardCodedFieldNames.CUSTOM_PROPERTY_SET.javaName()).isEqualTo(this.registeredCustomPropertySet)),
                        additionalPrimaryKeyColumnValues);
        return this.getMapper().select(condition, Order.ascending(HardCodedFieldNames.INTERVAL.javaName() + ".start"));
    }

    @SuppressWarnings("unchecked")
    <T extends PersistentDomainExtension<D>, D> SqlFragment getRawValuesSql(List<String> propertyColumnNames, D businessObject, Object... additionalPrimaryKeyColumnValues) {
        this.validateAdditionalPrimaryKeyValues(additionalPrimaryKeyColumnValues);
        PersistenceSupport persistenceSupport = this.customPropertySet.getPersistenceSupport();
        Condition condition =
                this.addAdditionalPrimaryKeyColumnConditionsTo(
                        where(persistenceSupport.domainFieldName()).isEqualTo(businessObject)
                            .and(where(HardCodedFieldNames.CUSTOM_PROPERTY_SET.javaName()).isEqualTo(this.registeredCustomPropertySet)),
                        additionalPrimaryKeyColumnValues);
        String[] columnNames = propertyColumnNames.toArray(new String[propertyColumnNames.size()]);
        return this.customPropertySetDataModel
                    .query(persistenceSupport.persistenceClass())
                    .asFragment(condition, columnNames);
    }

    @SuppressWarnings("unchecked")
    <T extends PersistentDomainExtension<D>, D> SqlFragment getRawValuesSql(List<String> propertyColumnNames, D businessObject, Range<Instant> effectiveInterval, Object... additionalPrimaryKeyColumnValues) {
        this.validateAdditionalPrimaryKeyValues(additionalPrimaryKeyColumnValues);
        PersistenceSupport persistenceSupport = this.customPropertySet.getPersistenceSupport();
        Condition condition =
                this.addAdditionalPrimaryKeyColumnConditionsTo(
                        where(persistenceSupport.domainFieldName()).isEqualTo(businessObject)
                            .and(where(HardCodedFieldNames.CUSTOM_PROPERTY_SET.javaName()).isEqualTo(this.registeredCustomPropertySet))
                            .and(where(HardCodedFieldNames.INTERVAL.javaName()).isEffective(effectiveInterval)),
                        additionalPrimaryKeyColumnValues);
        String[] columnNames = propertyColumnNames.toArray(new String[propertyColumnNames.size() + 2]);
        columnNames[propertyColumnNames.size()] = "starttime";
        columnNames[propertyColumnNames.size() + 1] = "endtime";
        return this.customPropertySetDataModel
                    .query(persistenceSupport.persistenceClass())
                    .asFragment(condition, columnNames);
    }

    <T extends PersistentDomainExtension<D>, D> void setNonVersionedValuesEntityFor(D businessObject, CustomPropertySetValues values, Object... additionalPrimaryKeyColumns) {
        Optional<T> domainExtension = this.getNonVersionedValuesEntityFor(businessObject, additionalPrimaryKeyColumns);
        if (domainExtension.isPresent()) {
            this.updateExtension(domainExtension.get(), businessObject, values, additionalPrimaryKeyColumns);
        }
        else {
            this.createExtension(businessObject, values, additionalPrimaryKeyColumns);
        }
    }

    private <T extends PersistentDomainExtension<D>, D> void updateExtension(T domainExtension, D businessObject, CustomPropertySetValues values, Object... additionalPrimaryKeyValues) {
        domainExtension.copyFrom(businessObject, values, additionalPrimaryKeyValues);
        Save.UPDATE.validate(this.customPropertySetDataModel, domainExtension);
        this.customPropertySetDataModel.update(domainExtension);
    }

    private <T extends PersistentDomainExtension<D>, D> void updateInterval(T domainExtension, Range<Instant> range) {
        DomainExtensionAccessor.setInterval(domainExtension, Interval.of(range));
        Save.UPDATE.validate(this.customPropertySetDataModel, domainExtension);
        this.customPropertySetDataModel.update(domainExtension);
    }

    @SuppressWarnings("unchecked")
    private <T extends PersistentDomainExtension<D>, D> void createExtension(D businessObject, CustomPropertySetValues values, Object... additionalPrimaryKeyValues) {
        T domainExtension = (T) this.customPropertySetDataModel.getInstance(this.customPropertySet.getPersistenceSupport().persistenceClass());
        DomainExtensionAccessor.setRegisteredCustomPropertySet(domainExtension, this.registeredCustomPropertySet);
        domainExtension.copyFrom(businessObject, values, additionalPrimaryKeyValues);
        Save.CREATE.validate(this.customPropertySetDataModel, domainExtension);
        this.customPropertySetDataModel.persist(domainExtension);
    }

    @SuppressWarnings("unchecked")
    private <T extends PersistentDomainExtension<D>, D> void createExtension(D businessObject, CustomPropertySetValues values, Instant effectiveTimestamp, Object... additionalPrimaryKeyValues) {
        T domainExtension = (T) this.customPropertySetDataModel.getInstance(this.customPropertySet.getPersistenceSupport().persistenceClass());
        DomainExtensionAccessor.setRegisteredCustomPropertySet(domainExtension, this.registeredCustomPropertySet);
        domainExtension.copyFrom(businessObject, values, additionalPrimaryKeyValues);
        DomainExtensionAccessor.setInterval(domainExtension, Interval.startAt(effectiveTimestamp));
        Save.CREATE.validate(this.customPropertySetDataModel, domainExtension);
        this.customPropertySetDataModel.persist(domainExtension);
    }

    @SuppressWarnings("unchecked")
    private <T extends PersistentDomainExtension<D>, D> void createExtension(D businessObject, CustomPropertySetValues values, Range<Instant> range, Object... additionalPrimaryKeyValues) {
        T domainExtension = (T) this.customPropertySetDataModel.getInstance(this.customPropertySet.getPersistenceSupport().persistenceClass());
        DomainExtensionAccessor.setRegisteredCustomPropertySet(domainExtension, this.registeredCustomPropertySet);
        domainExtension.copyFrom(businessObject, values, additionalPrimaryKeyValues);
        DomainExtensionAccessor.setInterval(domainExtension, Interval.of(range));
        Save.CREATE.validate(this.customPropertySetDataModel, domainExtension);
        this.customPropertySetDataModel.persist(domainExtension);
    }

    @SuppressWarnings("unchecked")
    private <T extends PersistentDomainExtension<D>, D> void createExtension(T domainExtension) {
        Save.CREATE.validate(this.customPropertySetDataModel, domainExtension);
        this.customPropertySetDataModel.persist(domainExtension);
    }

    <T extends PersistentDomainExtension<D>, D> void setVersionedValuesEntityFor(D businessObject, CustomPropertySetValues values, Instant effectiveTimestamp, Object... additionalPrimaryKeyValues) {
        Optional<T> domainExtension = this.getVersionedValuesEntityFor(businessObject, effectiveTimestamp, additionalPrimaryKeyValues);
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
        this.createExtension(businessObject, values, effectiveTimestamp,additionalPrimaryKeyValues);
    }

    @SuppressWarnings("unchecked")
    public <T extends PersistentDomainExtension<D>, D> void deleteExtensions(D businessObject, Object... additionalPrimaryKeyValues) {
        if (this.customPropertySet.isVersioned()) {
            this.getMapper().remove(this.getAllValuesEntityFor(businessObject, additionalPrimaryKeyValues));
        }
        else {
            this.getNonVersionedValuesEntityFor(businessObject, additionalPrimaryKeyValues).ifPresent(this::delete);
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

    <T extends PersistentDomainExtension<D>, D> void setValuesEntityFor(D businessObject, CustomPropertySetValues values, Instant effectiveTimestamp, Range<Instant> range, Object... additionalPrimaryKeyValues) {
        Optional<T> domainExtension = this.getVersionedValuesEntityFor(businessObject, effectiveTimestamp, additionalPrimaryKeyValues);
        if (domainExtension.isPresent()) {
            DomainExtensionAccessor.setInterval(domainExtension.get(), Interval.of(range));
            this.customPropertySetDataModel.remove(domainExtension.get());
        }
        this.createExtension(businessObject, values, range, additionalPrimaryKeyValues);
    }

    @SuppressWarnings("unchecked")
    public <T extends PersistentDomainExtension<D>, D> void validateValuesEntity(CustomPropertySetValues values) {
        T domainExtension = (T) this.customPropertySetDataModel.getInstance(this.customPropertySet.getPersistenceSupport()
                .persistenceClass());
        DomainExtensionAccessor.setRegisteredCustomPropertySet(domainExtension, this.registeredCustomPropertySet);
        domainExtension.copyFrom(null, values);
        Save.CREATE.validate(this.customPropertySetDataModel, domainExtension);
    }

    <T extends PersistentDomainExtension<D>, D> void alignTimeSlicedValues(D businessObject, Instant effectiveTimestamp, Range<Instant> range, Object... additionalPrimaryKeyValues) {
        Optional<T> domainExtension = this.getVersionedValuesEntityFor(businessObject, effectiveTimestamp, additionalPrimaryKeyValues);
        if (domainExtension.isPresent()) {
            Instant oldStartTime;
            if (range.hasLowerBound()) {
                oldStartTime = range.lowerEndpoint();
            }
            else {
                oldStartTime = Instant.EPOCH;
            }
            if (effectiveTimestamp.equals(oldStartTime)) {
                this.updateInterval(domainExtension.get(), range);
            }
            else {
                this.customPropertySetDataModel.remove(domainExtension.get());
                DomainExtensionAccessor.setInterval(domainExtension.get(), Interval.of(range));
                this.createExtension(domainExtension.get());
            }
        }
    }

    <T extends PersistentDomainExtension<D>, D> void removeTimeSlicedValues(D businessObject, Instant effectiveTimestamp, Object... additionalPrimaryKeyValues) {
        Optional<T> domainExtension = this.getVersionedValuesEntityFor(businessObject, effectiveTimestamp, additionalPrimaryKeyValues);
        if (domainExtension.isPresent()) {
            this.customPropertySetDataModel.remove(domainExtension.get());
        }
    }

}
