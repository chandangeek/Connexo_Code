package com.energyict.mdc.pluggable;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationParticipant;
import com.energyict.mdc.dynamic.relation.RelationType;

import java.util.Date;
import java.util.List;

/**
 * Models the behavior of a component that provides access
 * to {@link Relation}s that hold values for properties
 * that are provided by a {@link PluggableClass}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-17 (15:25)
 */
public interface RelationSupport {

    /**
     * Gets the default {@link RelationAttributeType} that will reference
     * the owner of the properties, i.e. the instance of {@link RelationParticipant}.
     *
     * @return The AttributeType or <code>null</code> if no properties are specified
     */
    public RelationAttributeType getDefaultAttributeType ();

    /**
     * Gets the {@link Relation} that holds the properties of {@link RelationParticipant}
     * that are active on the specified Date.
     *
     * @param relationParticipant The instance of RelationParticipant
     * @param date The Date
     * @return The Relations
     */
    public Relation getRelation (RelationParticipant relationParticipant, Date date);

    /**
     * Gets the {@link Relation}s that hold the attributes of the {@link RelationParticipant}
     * that are active during the specified TimePeriod.
     *
     * @param relationParticipant The ConnectionTask
     * @param period The TimePeriod
     * @return The Relations
     */
    public List<Relation> getRelations (RelationParticipant relationParticipant, Interval period);

}