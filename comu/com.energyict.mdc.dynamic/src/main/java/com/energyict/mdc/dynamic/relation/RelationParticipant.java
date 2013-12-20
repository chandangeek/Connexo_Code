package com.energyict.mdc.dynamic.relation;

import com.elster.jupiter.util.time.Interval;

import java.util.Date;
import java.util.List;

/**
 * An object participates to a <code>Relation</code> when one of the values for the relation's attribute
 * is the concerned object.
 */
public interface RelationParticipant {

    /**
     * returns a list of relation types where this object can play the role of participant
     *
     * @return the relation id
     */
    public List<RelationType> getAvailableRelationTypes();

    /**
     * returns a list of relations for the given attribute type valid on the given date
     *
     * @param attrib          the relation attribute type
     * @param date            the date when the relation should be valid
     * @param includeObsolete boolean indicating whether obsolete versions should be returned
     * @return the list of Relation objects
     */
    public List<Relation> getRelations(RelationAttributeType attrib, Date date, boolean includeObsolete);

    /**
     * returns a list of relations for the given attribute type valid on the given date
     *
     * @param attrib          the relation attribute type
     * @param date            the date when the relation should be valid
     * @param includeObsolete boolean indicating whether obsolete versions should be returned
     * @param fromRow         start row to be returned
     * @param toRow           end row to be returned
     * @return the list of Relation objects
     */
    public List<Relation> getRelations(RelationAttributeType attrib, Date date, boolean includeObsolete, int fromRow, int toRow);

    /**
     * returns a list of all relations for the given attribute type
     *
     * @param attrib the relation attribute type
     * @return the list of Relation objects
     */
    public List<Relation> getAllRelations(RelationAttributeType attrib);

    /**
     * returns a list of all relations for the given attribute type valid in the given period
     *
     * @param attrib The relation attribute type
     * @param interval The time Interval when the relation should be valid
     * @param includeObsolete boolean indicating whether obsolete versions should be returned
     * @return the list of Relation objects
     */
    public List<Relation> getRelations(RelationAttributeType attrib, Interval interval, boolean includeObsolete);

}
