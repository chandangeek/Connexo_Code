package com.energyict.mdc.dynamic.relation.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.BusinessObject;
import com.energyict.mdc.common.BusinessObjectFactory;
import com.energyict.mdc.common.BusinessObjectProxy;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.dynamic.relation.DefaultRelationParticipant;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationTransaction;
import com.energyict.mdc.dynamic.relation.RelationType;

import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.Range;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RelationImpl implements Relation {

    private static final String FLAGS_ATTRIBUTE = "flags";
    private static final String FROM_ATTRIBUTE = "from";
    private static final String TO_ATTRIBUTE = "to";
    private static final String CREUSER_ATTRIBUTE = "creUser";
    private static final String MODUSER_ATTRIBUTE = "modUser";
    private static final String CREDATE_ATTRIBUTE = "creDate";
    private static final String MODDATE_ATTRIBUTE = "modDate";

    private int id;
    private int relationTypeId;
    private RelationType relationType;
    private RelationFactory factory;
    private int flags;
    private int creationUserId = 0;
    private int modificationUserId = 0;
    private Interval period;
    private Instant obsoleteDate;
    private Instant creationDate;
    private Instant modificationDate;

    private Map<RelationAttributeType, Object> dynamicAttributes = null;

    public RelationImpl(RelationTypeImpl relationType, int id) {
        super();
        this.id = id;
        this.factory = new RelationFactory(relationType);
        this.relationType = relationType;
        this.relationTypeId = relationType.getId();
    }

    protected RelationImpl(RelationTypeImpl relationType, ResultSet resultSet) throws SQLException {
        this.factory = new RelationFactory(relationType);
        this.relationType = relationType;
        this.relationTypeId = relationType.getId();
        doLoad(resultSet);
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public int getRelationTypeId() {
        return relationTypeId;
    }

    @Override
    public Range<Instant> getPeriod() {
        return period.toClosedOpenRange();
    }

    @Override
    public int getFlags() {
        return flags;
    }

    @Override
    public RelationType getRelationType() {
        return relationType;
    }

    @Override
    public BusinessObject getBusinessObject() {
        return this;
    }

    @Override
    public String getType() {
        return Relation.class.getName();
    }

    private void unproxyMap(Map<RelationAttributeType, Object> map) {
        for (Map.Entry<RelationAttributeType, Object> relationAttributeTypeObjectEntry : map.entrySet()) {
            Object value = relationAttributeTypeObjectEntry.getValue();
            if (value instanceof BusinessObjectProxy) {
                BusinessObjectProxy proxy = (BusinessObjectProxy) value;
                relationAttributeTypeObjectEntry.setValue(proxy.getBusinessObject());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<RelationAttributeType, Object> getAttributeMap() {
        HashMap<RelationAttributeType, Object> baseMap = (HashMap<RelationAttributeType, Object>) basicGetAttributeMap();
        unproxyMap(baseMap);
        return (Map<RelationAttributeType, Object>) baseMap.clone();
    }

    @Override
    public Map<RelationAttributeType, Object> basicGetAttributeMap() {
        if (dynamicAttributes == null) {
            dynamicAttributes = doGetAttributeMap();
        }
        return dynamicAttributes;
    }

    protected Map<RelationAttributeType, Object> doGetAttributeMap() {
        if (this.getRelationType() == null) {
            return new HashMap<>();
        }
        else {
            return getRelationType().getAttributes(this);
        }
    }

    public void init(RelationTransaction transaction) throws SQLException, BusinessException {
        this.validateNew(transaction);
        this.copyNew(transaction);
    }

    protected void copy(RelationTransaction transaction) {
        period = Interval.of(transaction.getFrom(), transaction.getTo());
        flags = transaction.getFlags();
        Map<RelationAttributeType, Object> map = transaction.getAttributeMap();
        dynamicAttributes = new HashMap<>();
        for (RelationAttributeType attributeType : map.keySet()) {
            dynamicAttributes.put(attributeType, map.get(attributeType));
        }
    }

    protected void copyNew(RelationTransaction transaction) {
        copy(transaction);
    }

    protected void validateNew(RelationTransaction transaction) throws BusinessException {
        validate(transaction);
    }

    protected void validateUpdate(RelationTransaction transaction) throws BusinessException {
        validate(transaction);
    }

    protected void validate(RelationTransaction transaction) throws BusinessException {
        for (RelationAttributeType each : this.relationType.getAttributeTypes()) {
            ValueFactory valueFactory = each.getValueFactory();
            if (valueFactory.isReference()) {
                Object reference = transaction.get(each);
                if (reference != null && !valueFactory.isPersistent(reference)) {
                    throw new BusinessException("invalidReferenceObjectForX", "Invalid reference object for \"{0}\"", each.getName());
                }
            }
        }
    }

    @Override
    public Instant getFrom() {
        return period.getStart();
    }

    @Override
    public Instant getTo() {
        return period.getEnd();
    }

    @Override
    public Instant getCreDate() {
        return creationDate;
    }

    @Override
    public Instant getModDate() {
        return modificationDate;
    }

    private Instant asInstant(long seconds) {
        if (seconds == 0) {
            return null;
        }
        else {
            return Instant.ofEpochSecond(seconds);
        }
    }

    protected void doLoad(ResultSet resultSet) throws SQLException {
        int offset = 1;
        this.id = resultSet.getInt(offset++);
        if (!relationType.hasTimeResolution()) {
            period = new Interval(resultSet.getDate(offset++), resultSet.getDate(offset++));
        }
        else {
            period = Interval.of(
                    asInstant(resultSet.getLong(offset++)),
                    asInstant(resultSet.getLong(offset++)));
        }
        obsoleteDate = this.toInstant(resultSet.getTimestamp(offset++));
        flags = resultSet.getInt(offset++);
        creationDate = this.toInstant(resultSet.getTimestamp(offset++));
        modificationDate = this.toInstant(resultSet.getTimestamp(offset++));
        BigDecimal uidObject = resultSet.getBigDecimal(offset++);
        if (uidObject == null) {
            creationUserId = -1;
        }
        else {
            creationUserId = uidObject.intValue();
        }
        uidObject = resultSet.getBigDecimal(offset++);
        if (uidObject == null) {
            modificationUserId = -1;
        }
        else {
            modificationUserId = uidObject.intValue();
        }
        dynamicAttributes = new HashMap<>();
        for (RelationAttributeType each : relationType.getAttributeTypes()) {
            dynamicAttributes.put(each, each.valueFromDb(resultSet.getObject(offset++)));
        }
    }

    private Instant toInstant(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        else {
            return Instant.ofEpochMilli(timestamp.getTime());
        }
    }

    @Override
    public Object get(String key) {
        switch (key) {
            case FROM_ATTRIBUTE:
                return getFrom();
            case TO_ATTRIBUTE:
                return getTo();
            case FLAGS_ATTRIBUTE:
                return getFlags();
            case CREUSER_ATTRIBUTE:
                return null;    // Should return the DCGroup user
            case MODUSER_ATTRIBUTE:
                return null;    // Should return the DCGroup user
            case CREDATE_ATTRIBUTE:
                return getCreDate();
            case MODDATE_ATTRIBUTE:
                return getModDate();
        }
        RelationAttributeType attrType = relationType.getAttributeType(key);
        return get(attrType);
    }

    @Override
    public Object get(RelationAttributeType attrType) {
        Object result = basicGetAttributeMap().get(attrType);
        if (result != null) {
            if (result instanceof BusinessObjectProxy) {
                BusinessObjectProxy businessObjectProxy = (BusinessObjectProxy) result;
                result = businessObjectProxy.getBusinessObject();
                basicGetAttributeMap().put(attrType, result);
            }
        }
        return result;
    }

    @Override
    public boolean isObsolete() {
        return obsoleteDate != null;
    }

    @Override
    public Instant getObsoleteDate() {
        return obsoleteDate;
    }

    @Override
    public void close(Instant newTo) throws SQLException, BusinessException {
        updateTo(newTo);
    }

    @Override
    public void makeObsolete() throws BusinessException, SQLException {
        if (isObsolete()) {
            throw new BusinessException("relationIsAlreadyObsolete", "This relation is already obsolete");
        }
        this.obsoleteDate = Bus.getServiceLocator().clock().instant();
        this.factory.makeObsolete(this);
    }

    @Override
    public boolean matchAttributes(Map testMap) {
        Map<RelationAttributeType, Object> attributes = basicGetAttributeMap();
        for (Map.Entry<RelationAttributeType, Object> relationAttributeTypeObjectEntry : attributes.entrySet()) {
            Object key = relationAttributeTypeObjectEntry.getKey();
            Object current = relationAttributeTypeObjectEntry.getValue();
            Object test = testMap.get(key);
            if (!isEqual(current, test)) {
                return false;
            }
        }
        return true;
    }

    /**
     * compare method that takes into account comparing proxied objects with their non proxied equivalent
     *
     * @param obj1 a first object (may be a proxy)
     * @param obj2 a second object (may be a proxy)
     * @return the boolean compare result
     */
    private boolean isEqual(Object obj1, Object obj2) {
        if (!(obj1 instanceof BusinessObjectProxy) || !(obj2 instanceof BusinessObjectProxy)) {
            return Checks.is(obj1).equalTo(obj2);
        }
        if (obj1 instanceof BusinessObject && obj2 instanceof BusinessObject) {
            return Checks.is(obj1).equalTo(obj2);
        }
        if (obj1 instanceof BusinessObject) {
            try {
                return ((IdBusinessObject) obj1).getId() == ((IdBusinessObject) obj2).getId();
            }
            catch (ClassCastException e) {
                return false;
            }
        }
        if (obj2 instanceof BusinessObject) {
            try {
                return ((IdBusinessObject) obj2).getId() == ((IdBusinessObject) obj1).getId();
            }
            catch (ClassCastException e) {
                return false;
            }
        }
        return Checks.is(obj1).equalTo(obj2);
    }


    @Override
    public boolean includes(Instant date) {
        return isEffectiveAt(Objects.requireNonNull(date));
    }

    private void updateTo(Instant newTo) throws BusinessException, SQLException {
        if (isObsolete()) {
            throw new BusinessException("relationIsObsolete", "This relation is obsolete");
        }
        if (newTo != null && !newTo.isAfter(getFrom())) {
            SimpleDateFormat dateFormatter;
            if (getRelationType().hasTimeResolution()) {
                dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            }
            else {
                dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            }
            throw new BusinessException("invalidToDateX", "Invalid 'To date' {0}", dateFormatter.format(newTo));
        }
        this.period = Interval.of(getFrom(), newTo);
        this.factory.updateTo(this);
    }

    @Override
    public void extendBy(RelationTransaction transaction) throws BusinessException, SQLException {
        if (getTo() != null) {
            if (transaction.getTo() == null) {
                updateTo(null);
            }
            else {
                if (transaction.getTo().isAfter(getTo())) {
                    updateTo(transaction.getTo());
                }
            }
        }
    }

    @Override
    public void clearFlag(int bitPos) throws SQLException {
        flags = flags & (~(1 << bitPos));
        this.factory.updateFlags(this, flags);
    }

    @Override
    public void setFlag(int bitPos) throws SQLException {
        flags = flags | (1 << bitPos);
        this.factory.updateFlags(this, flags);
    }

    @Override
    public void updateFlags(int flags) throws SQLException {
        this.flags = flags;
        this.factory.updateFlags(this, flags);
    }

    @Override
    public void delete() throws BusinessException, SQLException {
        this.factory.delete(this);
    }

    @Override
    public boolean startsBefore(Instant testDate) {
        return period.startsBefore(Objects.requireNonNull(testDate));
    }

    @Override
    public boolean startsAfter(Instant testDate) {
        return period.startsAfter(Objects.requireNonNull(testDate));
    }

    @Override
    public boolean endsBefore(Instant testDate) {
        return period.endsBefore(Objects.requireNonNull(testDate));
    }

    @Override
    public boolean endsAfter(Instant testDate) {
        return period.endsAfter(Objects.requireNonNull(testDate));
    }

    protected void doSetRelationType(RelationType type) {
        this.relationType = type;
    }

    @Override
    public RelationTransaction getTransaction() {
        return new RelationTransactionImpl(this);
    }

    @Override
    public boolean proxies(BusinessObject obj) {
        return this.equals(obj);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof RelationImpl) {
            RelationImpl other = (RelationImpl) o;
            return (this.getId() == other.getId()) && (this.getRelationTypeId() == other.getRelationTypeId());
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getId() ^ getRelationTypeId();
    }

    @Override
    public String displayString() {
        return this.toString();
    }

    @Override
    public boolean isDefaultRelationFor(DefaultRelationParticipant participant) {
        RelationAttributeType attType = participant.getDefaultAttributeType();
        if (attType == null) {
            return false;
        }
        else {
            Object obj = this.get(attType);
            return obj != null && obj.equals(participant);
        }
    }

    @Override
    public Interval getInterval() {
    	return period;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final BusinessObjectFactory getFactory() {
        return this.relationType;
    }

    class SerializebleRelationKey implements Serializable {

        private int relationId;

        private int relationTypeId;

        SerializebleRelationKey(int relationId, int relationTypeId) {
            this.relationId = relationId;
            this.relationTypeId = relationTypeId;
        }

        public int getRelationId() {
            return relationId;
        }

        public int getRelationTypeId() {
            return relationTypeId;
        }
    }

}