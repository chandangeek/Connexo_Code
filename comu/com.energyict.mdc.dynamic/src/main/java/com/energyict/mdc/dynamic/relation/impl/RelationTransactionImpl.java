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

import com.elster.jupiter.util.time.Interval;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelationTransactionImpl implements RelationTransaction, DynamicAttributeOwner {

    private RelationTypeImpl relationType;
    private Date from;
    private Date to;
    private Map<RelationAttributeType, Object> dynamicAttributes = new HashMap<>();
    private int flags;
    private boolean forcedClose = false;

    public RelationTransactionImpl(RelationTypeImpl type) {
        this.relationType = type;
    }

    public RelationTransactionImpl(Relation relation) {
        this.relationType = (RelationTypeImpl) relation.getRelationType();
        this.from = relation.getFrom();
        this.to = relation.getTo();
        Map<RelationAttributeType, Object> map = relation.getAttributeMap();
        for (RelationAttributeType relationAttributeType : map.keySet()) {
            this.dynamicAttributes.put(relationAttributeType, map.get(relationAttributeType));
        }
        this.flags = relation.getFlags();
    }

    private RelationTransactionImpl(RelationTransactionImpl transaction) {
        this.relationType = transaction.relationType;
        this.from = transaction.getFrom();
        this.to = transaction.getTo();
        this.dynamicAttributes = transaction.getAttributeMap();
        this.flags = transaction.getFlags();
    }

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
        from = round(from);
        to = round(to);
        Interval interval = new Interval(from, to);
        List affectedItems = relationType.getAffectedRelations(this);
        if (forcedClose) {  // used when a new relation with open todate is offered to the transaction,
            // the todate will be set to the first from date of an affected relation having a fromdate
            // after the transactions fromdate and therefore closes the relation
            List newAffectedItems = new ArrayList();
            for (Object affectedItem : affectedItems) {
                Relation relation = (Relation) affectedItem;
                if (relation.getFrom().after(this.getFrom())) {
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
                        RelationTransaction transaction = new RelationTransactionImpl(this);
                        transaction.setTo(relation.getTo());
                        relation.makeObsolete();
                        return basicCreateRelation(transaction);
                    }
                }
            } else { // we have an overlap
                Interval intersect = relation.getPeriod().intersection(interval);
                if (intersect != null && !intersect.isEmpty()) {
                    if (intersect.equals(relation.getPeriod())) // Affected relation is entirely covered by new one, make affected obsolete
                    {
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
                        Date relationTo = relation.getTo();
                        if (before(relation.getFrom(), from)) {
                            relation.close(from);
                            keepRelation = true;
                        }
                        if (after(relationTo, to)) {
                            RelationTransaction transaction = new RelationTransactionImpl(relation);
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
        return new RelationFactory(this.relationType).create(transaction);
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public Map<RelationAttributeType, Object> getAttributeMap() {
        return dynamicAttributes;
    }

    public void set(RelationAttributeType attrib, Object value) {
        dynamicAttributes.put(attrib, value);
    }

    public void set(String attribKey, Object value) {
        RelationAttributeType attrib = relationType.getAttributeType(attribKey);
        if (attrib == null) {
            throw new ApplicationException("Undefined attribute: " + attribKey);
        }
        set(attrib, value);
    }

    public Object get(RelationAttributeType attrib) {
        return dynamicAttributes.get(attrib);
    }

    public Object get(String attribKey) {
        RelationAttributeType attrib = relationType.getAttributeType(attribKey);
        return get(attrib);
    }

    public Interval getPeriod() {
        return new Interval(from, to);
    }

    public boolean isEmpty() {
        for (Object val : dynamicAttributes.values()) {
            if (val != null) {
                return false;
            }
        }
        return true;
    }

    private boolean before(Date date1, Date date2) {
        if (date1 == null) {
            return false;
        }
        if (date2 == null) {
            return true;
        } else {
            return date1.before(date2);
        }
    }

    private boolean after(Date date1, Date date2) {
        if (date2 == null) {
            return false;
        }
        if (date1 == null) {
            return true;
        } else {
            return date1.after(date2);
        }
    }

    /**
     * Returns a new date that has its milliseconds trunked to 0
     *
     * @param date The date to floor to its seconds
     * @return the floored date
     */
    private Date round(Date date) {
        if (date != null) {
            return new Date(date.getTime() / 1000 * 1000);
        } else {
            return null;
        }
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
