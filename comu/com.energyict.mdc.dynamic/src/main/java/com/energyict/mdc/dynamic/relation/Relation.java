package com.energyict.mdc.dynamic.relation;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.orm.associations.Effectivity;
import com.google.common.collect.Range;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.dynamic.ReadOnlyDynamicAttributeOwner;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

/**
 * The values for a number of attributes defined by a <code>RelationType</code> within a
 * defined period
 */
@ProviderType
public interface Relation extends IdBusinessObject, ReadOnlyDynamicAttributeOwner, Effectivity {

    /**
     * returns the relation id this version belongs to
     *
     * @return the relation id
     */
    public int getRelationTypeId();

    /**
     * returns the version start date.
     *
     * @return the start date
     */
    public Instant getFrom();

    /**
     * returns the version end date
     *
     * @return the version end date or null
     */
    public Instant getTo();

    /**
     * Returns the time interval.
     *
     * @return the Interval
     */
    public Range<Instant> getPeriod();

    /**
     * return the relation's creation date
     *
     * @return the creation date
     */
    public Instant getCreDate();

    /**
     * returns the <code>RelationType</code> of the receiver
     *
     * @return the <code>RelationType</code>
     */
    public RelationType getRelationType();

    /**
     * returns the attribute values
     *
     * @return The keys are the attribute type,
     *         the values are the attribute values.
     */
    public Map<RelationAttributeType, Object> getAttributeMap();

    /**
     * For internal use only; Not part of the public API
     * Returns the attribute values. References to BusinessObjects may be proxies.
     *
     * @return The keys are the attribute type,
     *         the values are the attribute values.
     */
    public Map<RelationAttributeType, Object> basicGetAttributeMap();

    /**
     * returns the processing flags for this relation
     *
     * @return the processing flags.
     */
    public int getFlags();

    /**
     * tests if this version is obsolete.
     *
     * @return true if obsolete, false otherwise
     */
    public boolean isObsolete();

    /**
     * return the Date when this relation became obsolete
     *
     * @return the Date
     */
    public Instant getObsoleteDate();

    /**
     * test if the version attributes are equal to the argument
     *
     * @param testMap the keys contain the attribute names,
     *                and the values contain the attribute values
     * @return true if equal , false otherwise.
     */
    public boolean matchAttributes(Map testMap);

    /**
     * test if the argument is covered by this version
     *
     * @param date the test date
     * @return true if the argument is in the receiver's validity period.
     *         false otherwise
     */
    public boolean includes(Instant date);

    /**
     * tests if the relation starts before the argument
     *
     * @param testDate the test date
     * @return true if version starts before the test date.
     */
    public boolean startsBefore(Instant testDate);

    /**
     * tests if the relation starts after the argument
     *
     * @param testDate the test date
     * @return true if version starts after the test date.
     */
    public boolean startsAfter(Instant testDate);

    /**
     * tests if the relation ends before the given date
     *
     * @param testDate the test date
     * @return true if version ends before the test date.
     */
    public boolean endsBefore(Instant testDate);

    /**
     * tests if the version ends after the argument
     *
     * @param testDate the test date
     * @return true if version ends after the test date.
     */
    public boolean endsAfter(Instant testDate);

    /**
     * extends this version
     * not part of the API
     *
     * @param transaction contains the extended version validity period
     * @throws SQLException      if a database exception occurred
     * @throws BusinessException if a business exception occurred.
     */
    public void extendBy(RelationTransaction transaction) throws SQLException, BusinessException;

    /**
     * closes the version at the given date.
     * reserved for future use
     *
     * @param toDate the close date
     * @throws SQLException      if a database error occurred
     * @throws BusinessException if a business exception occurred
     */
    public void close(Instant toDate) throws SQLException, BusinessException;

    /**
     * makes this relation obsolete.
     * not part of the API
     *
     * @throws BusinessException if a business exception occurred.
     * @throws SQLException      if a database error occurred.
     */
    public void makeObsolete() throws BusinessException, SQLException;

    /**
     * sets the folder relation's flags to value
     *
     * @param value value to be set
     * @throws SQLException if a database error occurred
     */
    public void updateFlags(int value) throws SQLException;

    /**
     * sets the indicated processing flag
     *
     * @param bitPos zero based flag index
     * @throws SQLException if a database error occurred
     */
    public void setFlag(int bitPos) throws SQLException;

    /**
     * clears the indicated processing flag
     *
     * @param bitPos zero based flag index
     * @throws SQLException if a database error occurred
     */
    public void clearFlag(int bitPos) throws SQLException;

    /**
     * Returns the receiver's last modification date
     *
     * @return the last modification date
     */
    public Instant getModDate();

    /**
     * return the Object corresponding to the given attribute
     *
     * @return the resulting Object
     */
    public Object get(RelationAttributeType attrType);

    /**
     * Return a relation transaction that models the current relation.
     * usefull for cloning
     *
     * @return a RelationTransaction
     */
    public RelationTransaction getTransaction();

    /**
     * Returns true when this relation is a default relation for the given participant
     *
     * @param participant the participant
     * @return the boolean result
     */
    public boolean isDefaultRelationFor(DefaultRelationParticipant participant);

}

