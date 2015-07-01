package com.energyict.mdc.dynamic.relation;

import aQute.bnd.annotation.ConsumerType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

/**
 * An object participates to a <code>Relation</code> when one of the values for the relation's attribute
 * is the concerned object.
 */
@ConsumerType
public interface RelationParticipant {

    /**
     * Gets the {@link Relation}s for the given attribute type valid on the given timestamp.
     *
     * @param attrib          the relation attribute type
     * @param when            the when when the relation should be valid
     * @param includeObsolete boolean indicating whether obsolete versions should be returned
     * @return the list of Relation objects
     */
    public List<Relation> getRelations(RelationAttributeType attrib, Instant when, boolean includeObsolete);

    /**
     * Gets the {@link Relation}s for the given attribute type valid on the given timestamp.
     *
     * @param attrib          the relation attribute type
     * @param when            the when when the relation should be valid
     * @param includeObsolete boolean indicating whether obsolete versions should be returned
     * @param fromRow         start row to be returned
     * @param toRow           end row to be returned
     * @return the list of Relation objects
     */
    public List<Relation> getRelations(RelationAttributeType attrib, Instant when, boolean includeObsolete, int fromRow, int toRow);

    /**
     * Gets the {@link Relation}s for the given attribute type.
     *
     * @param attrib the relation attribute type
     * @return the list of Relation objects
     */
    public List<Relation> getAllRelations(RelationAttributeType attrib);

    /**
     * Gets the {@link Relation}s for the given attribute type valid during the specified Range.
     *
     * @param attrib The relation attribute type
     * @param interval The time Interval when the relation should be valid
     * @param includeObsolete boolean indicating whether obsolete versions should be returned
     * @return the list of Relation objects
     */
    public List<Relation> getRelations(RelationAttributeType attrib, Range<Instant> interval, boolean includeObsolete);

}