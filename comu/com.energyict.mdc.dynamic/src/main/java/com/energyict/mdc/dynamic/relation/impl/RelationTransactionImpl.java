package com.energyict.mdc.dynamic.relation.impl;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.dynamic.relation.CanLock;
import com.energyict.mdc.dynamic.relation.Constraint;
import com.energyict.mdc.dynamic.relation.ConstraintViolationException;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationTransaction;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.dynamic.relation.impl.legacy.PersistentIdObject;

import com.google.common.collect.Range;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelationTransactionImpl implements RelationTransaction, DynamicAttributeOwner {

    private final Clock clock;
    private final RelationTypeImpl relationType;
    private Instant from;
    private Instant to;
    private Map<RelationAttributeType, Object> dynamicAttributes = new HashMap<>();
    private int flags;
    private boolean forcedClose = false;

    public RelationTransactionImpl(Clock clock, RelationTypeImpl type) {
        this.clock = clock;
        this.relationType = type;
    }

    public RelationTransactionImpl(Relation relation, Clock clock) {
        this.clock = clock;
        this.relationType = (RelationTypeImpl) relation.getRelationType();
        this.from = relation.getFrom();
        this.to = relation.getTo();
        Map<RelationAttributeType, Object> map = relation.getAttributeMap();
        for (RelationAttributeType relationAttributeType : map.keySet()) {
            this.dynamicAttributes.put(relationAttributeType, map.get(relationAttributeType));
        }
        this.flags = relation.getFlags();
    }

    private RelationTransactionImpl(RelationTransactionImpl transaction, Clock clock) {
        this.clock = clock;
        this.relationType = transaction.relationType;
        this.from = transaction.getFrom();
        this.to = transaction.getTo();
        this.dynamicAttributes = transaction.getAttributeMap();
        this.flags = transaction.getFlags();
    }

    @Override
    public Relation execute() throws BusinessException, SQLException {
        try {
            return this.perform();
        }
        catch (LocalBusinessException e) {
            throw e.getCause();
        }
        catch (LocalSQLException e) {
            throw e.getCause();
        }
    }

    private void lock() {
        RelationAttributeType lockAttrib = this.relationType.getLockAttributeType();
        Object attributeValue = this.get(lockAttrib);
        if (attributeValue instanceof PersistentIdObject) {
            this.lockIdBusinessObject((PersistentIdObject) attributeValue);
        }
        else if (attributeValue instanceof CanLock) {
            CanLock canLock = (CanLock) attributeValue;
            canLock.lock();
        }
        else {
            throw new IllegalArgumentException("Values of a relation's lock attribute should implement the IdBusinessObject or CanLock interface");
        }
    }

    private void lockIdBusinessObject(PersistentIdObject persistentIdObject) {
        if (persistentIdObject != null) {
            IdBusinessObjectFactory factory = (IdBusinessObjectFactory) persistentIdObject.getFactory();
            PersistentIdObject.lock(persistentIdObject, factory.getTableName());
        }
    }

    @Override
    public Relation perform() {
        lock();
        try {
            validate();
            checkViolations();
            return insertRelation();
        }
        catch (BusinessException e) {
            throw new LocalBusinessException(e);
        }
        catch (SQLException e) {
            throw new LocalSQLException(e);
        }
    }

    private void validate() throws BusinessException {
        if (from == null) {
            throw new BusinessException("emptyFromDateNotAllowed", "An empty from date is not allowed");
        }
    }

    private void checkViolations() throws ConstraintViolationException {
        for (Object o : relationType.getRejectViolationConstraints()) {
            Constraint constraint = (Constraint) o;
            if (!constraint.getViolatedRelations(this).isEmpty()) {
                throw new ConstraintViolationException(constraint);
            }
        }
    }

    /**
     * Inserts a new relation, different cases will be handled for affected relations
     * - affected is identical, do nothing and return affected
     * - affected is covered by new one, remove affected
     * - affected is similar and connects to either the beginning or the end
     * - affected needs to be split by the new relation
     *
     * @return the new created relation
     * @throws SQLException
     * @throws BusinessException
     */
    private Relation insertRelation() throws SQLException, BusinessException {
        Relation result = null;
        boolean extended = false;
        // Fix for mantis 6199, round from and to to second resolution, because we will compare with database times that are also rounded to second resolution
        Range<Instant> interval = this.rangeWithSecondPrecision(from, to);
        List affectedItems = relationType.getAffectedRelations(this);
        if (forcedClose) {  // used when a new relation with open todate is offered to the transaction,
            // the todate will be set to the first from date of an affected relation having a fromdate
            // after the transactions fromdate and therefore closes the relation
            List newAffectedItems = new ArrayList();
            for (Object affectedItem : affectedItems) {
                Relation relation = (Relation) affectedItem;
                if (relation.getFrom().isAfter(this.getFrom())) {
                    this.to = relation.getFrom();
                    break;
                } else {
                    newAffectedItems.add(relation);
                }
            }
            affectedItems = newAffectedItems;
        }
        for (Object affectedItem : affectedItems) {
            Relation relation = (Relation) affectedItem;
            if (relation.getPeriod().equals(interval) && relation.matchAttributes(dynamicAttributes)) { // affected relation is equal
                if (extended) {
                    // if a previous affected relation was extended, then make this one obsolete
                    relation.makeObsolete();
                } else {
                    // else just return this relation as it is an exact copy of what we wanted
                    return relation;
                }
            } else if ((relation.getTo() != null) && from.equals(relation.getTo())) { // connects at the end (adjacent)
                if (relation.matchAttributes(dynamicAttributes)) { // if it appears to be the same, extend existing
                    if (!extended) {
                        relation.extendBy(this);
                        result = relation; // store the return value and continue processing the other affected items
                        extended = true;
                    }
                }
            } else if ((relation.getFrom().equals(to))) { // connects before (adjacent)
                if (relation.matchAttributes(dynamicAttributes)) { // if it has the same content
                    if (!extended) {
                        RelationTransaction transaction = new RelationTransactionImpl(this, clock);
                        transaction.setTo(relation.getTo());
                        relation.makeObsolete();
                        return basicCreateRelation(transaction);
                    }
                }
            } else { // we have an overlap
                Range<Instant> intersect = relation.getPeriod().intersection(interval);
                if (intersect != null && !intersect.isEmpty()) {
                    if (intersect.equals(relation.getPeriod())) { // Affected relation is entirely covered by new one, make affected obsolete
                        relation.makeObsolete();
                    } else {                                      // Affected relation is partly covered and may need to be split in two parts
                        if (!extended && intersect.equals(this.getPeriod())) {   // receiver is completely covered within the affected item
                            // note: do not do this during extension of a previous version
                            // since the extension will have created a partial overlap
                            if (relation.matchAttributes(dynamicAttributes)) // if attributes are equal, do nothing
                            {
                                return relation;
                            }
                        }
                        boolean keepRelation = false;
                        Instant relationTo = relation.getTo();
                        if (before(relation.getFrom(), from)) {
                            relation.close(from);
                            keepRelation = true;
                        }
                        if (after(relationTo, to)) {
                            RelationTransaction transaction = new RelationTransactionImpl(relation, clock);
                            transaction.setFrom(to);
                            transaction.setTo(relationTo);
                            basicCreateRelation(transaction);
                            if (!keepRelation && !relation.isObsolete()) {
                                relation.makeObsolete();
                            }
                            break;
                        }
                    }
                }
            }
        }
        return result == null ? basicCreateRelation(this) : result;
    }

    private Relation basicCreateRelation(RelationTransaction transaction) throws BusinessException, SQLException {
        return new RelationFactory(this.relationType, this.clock).create(transaction);
    }

    @Override
    public Instant getFrom() {
        return from;
    }

    @Override
    public void setFrom(Instant from) {
        this.from = from;
    }

    @Override
    public int getFlags() {
        return flags;
    }

    @Override
    public void setFlags(int flags) {
        this.flags = flags;
    }

    @Override
    public RelationType getRelationType() {
        return relationType;
    }

    @Override
    public Instant getTo() {
        return to;
    }

    @Override
    public void setTo(Instant to) {
        this.to = to;
    }

    @Override
    public Map<RelationAttributeType, Object> getAttributeMap() {
        return dynamicAttributes;
    }

    @Override
    public void set(RelationAttributeType attrib, Object value) {
        dynamicAttributes.put(attrib, value);
    }

    @Override
    public void set(String attribKey, Object value) {
        RelationAttributeType attrib = relationType.getAttributeType(attribKey);
        if (attrib == null) {
            throw new ApplicationException("Undefined attribute: " + attribKey);
        }
        set(attrib, value);
    }

    @Override
    public Object get(RelationAttributeType attrib) {
        return dynamicAttributes.get(attrib);
    }

    @Override
    public Object get(String attribKey) {
        RelationAttributeType attrib = relationType.getAttributeType(attribKey);
        return get(attrib);
    }

    @Override
    public Range<Instant> getPeriod() {
        if (from == null) {
            if (to == null) {
                return Range.all();
            }
            else {
                return Range.lessThan(to);
            }
        }
        else {
            if (to == null) {
                return Range.atLeast(from);
            }
            else {
                return Range.closedOpen(from, to);
            }
        }
    }

    @Override
    public boolean isEmpty() {
        for (Object val : dynamicAttributes.values()) {
            if (val != null) {
                return false;
            }
        }
        return true;
    }

    private boolean before(Instant date1, Instant date2) {
        if (date1 == null) {
            return false;
        }
        if (date2 == null) {
            return true;
        } else {
            return date1.isBefore(date2);
        }
    }

    private boolean after(Instant date1, Instant date2) {
        if (date2 == null) {
            return false;
        }
        if (date1 == null) {
            return true;
        } else {
            return date1.isAfter(date2);
        }
    }

    private Range<Instant> rangeWithSecondPrecision(Instant from, Instant to) {
        if (from == null) {
            if (to == null) {
                return Range.all();
            }
            else {
                return Range.lessThan(instantWithSecondPrecision(to));
            }
        }
        else {
            if (to == null) {
                return Range.atLeast(instantWithSecondPrecision(from));
            }
            else {
                return Range.closedOpen(instantWithSecondPrecision(from), instantWithSecondPrecision(to));
            }
        }
    }

    /**
     * Returns a new date that has its milliseconds trunked to 0
     *
     * @param date The date to floor to its seconds
     * @return the floored date
     */
    private Instant instantWithSecondPrecision(Instant date) {
        return Instant.ofEpochSecond(date.getEpochSecond());
    }

    private class LocalSQLException extends RuntimeException {
        private LocalSQLException(SQLException cause) {
            super(cause);
        }

        @Override
        public SQLException getCause() {
            return (SQLException) super.getCause();
        }
    }

    private class LocalBusinessException extends RuntimeException {
        private LocalBusinessException(BusinessException cause) {
            super(cause);
        }

        @Override
        public BusinessException getCause() {
            return (BusinessException) super.getCause();
        }
    }

}
