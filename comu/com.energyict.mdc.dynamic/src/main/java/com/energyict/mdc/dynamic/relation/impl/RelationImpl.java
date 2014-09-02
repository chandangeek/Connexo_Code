package com.energyict.mdc.dynamic.relation.impl;

import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.BusinessObject;
import com.energyict.mdc.common.BusinessObjectFactory;
import com.energyict.mdc.common.BusinessObjectProxy;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.TypeId;
import com.energyict.mdc.dynamic.relation.DefaultRelationParticipant;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationTransaction;
import com.energyict.mdc.dynamic.relation.RelationType;

import org.joda.time.DateTimeConstants;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Date obsoleteDate;
    private Date creationDate;
    private Date modificationDate;

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

    public int getRelationTypeId() {
        return relationTypeId;
    }

    public Interval getPeriod() {
        return period;
    }

    public int getFlags() {
        return flags;
    }

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
    public Map<RelationAttributeType, Object> getAttributeMap() {
        HashMap<RelationAttributeType, Object> baseMap = (HashMap<RelationAttributeType, Object>) basicGetAttributeMap();
        unproxyMap(baseMap);
        return (Map<RelationAttributeType, Object>) baseMap.clone();
    }

    public Map<RelationAttributeType, Object> basicGetAttributeMap() {
        if (dynamicAttributes == null) {
            dynamicAttributes = doGetAttributeMap();
        }
        return dynamicAttributes;
    }

    protected Map<RelationAttributeType, Object> doGetAttributeMap() {
        RelationType relationType = getRelationType();
        return relationType == null ? new HashMap<RelationAttributeType, Object>() : relationType.getAttributes(this);
    }

    public void init(RelationTransaction transaction) throws SQLException, BusinessException {
        this.validateNew(transaction);
        this.copyNew(transaction);
    }

    protected void copy(RelationTransaction transaction) {
        period = new Interval(transaction.getFrom(), transaction.getTo());
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

    protected void copyUpdate(RelationTransaction transaction) {
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

    public Date getFrom() {
        return period.getStart();
    }

    public Date getTo() {
        return period.getEnd();
    }

    public Date getCreDate() {
        return creationDate;
    }

    public Date getModDate() {
        return modificationDate;
    }

    private java.sql.Date asDate(java.util.Date in) {
        if (in == null) {
            return null;
        }
        else {
            return new java.sql.Date(in.getTime());
        }
    }

    private Date asDate(long seconds) {
        if (seconds == 0) {
            return null;
        }
        else {
            return new Date(seconds * DateTimeConstants.MILLIS_PER_SECOND);
        }
    }

    protected void doLoad(ResultSet resultSet) throws SQLException {
        int offset = 1;
        this.id = resultSet.getInt(offset++);
        if (!relationType.hasTimeResolution()) {
            period = new Interval(resultSet.getDate(offset++), resultSet.getDate(offset++));
        }
        else {
            period = new Interval(
                    asDate(resultSet.getLong(offset++)),
                    asDate(resultSet.getLong(offset++)));
        }
        obsoleteDate = resultSet.getTimestamp(offset++);
        flags = resultSet.getInt(offset++);
        creationDate = resultSet.getTimestamp(offset++);
        modificationDate = resultSet.getTimestamp(offset++);
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

    List<RelationAttributeType> getAttributeTypes() {
        return getRelationType().getAttributeTypes();
    }

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

    protected String getDynamicAttributeTableName() {
        return getRelationType().getDynamicAttributeTableName();
    }

    public boolean isObsolete() {
        return obsoleteDate != null;
    }

    public Date getObsoleteDate() {
        return obsoleteDate;
    }

    public void close(Date newTo) throws SQLException, BusinessException {
        updateTo(newTo);
    }

    public void makeObsolete() throws BusinessException, SQLException {
        if (isObsolete()) {
            throw new BusinessException("relationIsAlreadyObsolete", "This relation is already obsolete");
        }
        this.obsoleteDate = new Date();
        this.factory.makeObsolete(this);
    }

    public boolean matchAttributes(Map testMap) {
        // looks like hashMap.equals() does not
        // behave as expected, so we have to test for equality on our own
        Map<RelationAttributeType, Object> attributes = basicGetAttributeMap();
        //unproxyMap(attributes);
        for (Map.Entry<RelationAttributeType, Object> relationAttributeTypeObjectEntry : attributes.entrySet()) {
            Object key = relationAttributeTypeObjectEntry.getKey();
            Object current = relationAttributeTypeObjectEntry.getValue();
            Object test = testMap.get(key);
            // use Utils areEqual instead of current.equals(test)
            // to handle BigDecimals
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


    public boolean includes(Date date) {
        return period.contains(date, Interval.EndpointBehavior.OPEN_CLOSED);
    }

    private void updateTo(Date newTo) throws BusinessException, SQLException {
        if (isObsolete()) {
            throw new BusinessException("relationIsObsolete", "This relation is obsolete");
        }
        if (newTo != null && !newTo.after(getFrom())) {
            throw new BusinessException(
                    "invalidToDateX",
                    "Invalid 'To date' {0}",
                    getRelationType().hasTimeResolution() ?
                            Environment.DEFAULT.get().getFormatPreferences().getDateTimeFormat(true).format(newTo) :
                            Environment.DEFAULT.get().getFormatPreferences().getDateFormat().format(newTo));
        }
        this.period = new Interval(getFrom(), newTo);
        this.factory.updateTo(this);
    }

    public void extendBy(RelationTransaction transaction) throws BusinessException, SQLException {
        if (getTo() != null) {
            if (transaction.getTo() == null) {
                updateTo(null);
            }
            else {
                if (transaction.getTo().after(getTo())) {
                    updateTo(transaction.getTo());
                }
            }
        }
    }

    public void clearFlag(int bitPos) throws SQLException {
        flags = flags & (~(1 << bitPos));
        this.factory.updateFlags(this, flags);
    }

    public void setFlag(int bitPos) throws SQLException {
        flags = flags | (1 << bitPos);
        this.factory.updateFlags(this, flags);
    }

    public void updateFlags(int flags) throws SQLException {
        this.flags = flags;
        this.factory.updateFlags(this, flags);
    }

    @Override
    public boolean canDelete() {
        return true;
    }

    @Override
    public void delete() throws BusinessException, SQLException {
        this.factory.delete(this);
    }

    public boolean startsBefore(Date testDate) {
        return period.startsBefore(testDate);
    }

    public boolean startsAfter(Date testDate) {
        return period.startsAfter(testDate);
    }

    public boolean endsBefore(Date testDate) {
        return period.endsBefore(testDate);
    }

    public boolean endsAfter(Date testDate) {
        return period.endsAfter(testDate);
    }

    protected void doSetRelationType(RelationType type) {
        this.relationType = type;
    }

    public RelationTransaction getTransaction() {
        return new RelationTransactionImpl(this);
    }

    @Override
    public boolean proxies(BusinessObject obj) {
        return this.equals(obj);
    }

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

    public int hashCode() {
        return getId() ^ getRelationTypeId();
    }

    @Override
    public String displayString() {
        return this.toString();
    }

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