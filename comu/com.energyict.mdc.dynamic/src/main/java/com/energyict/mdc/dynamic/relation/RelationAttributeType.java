package com.energyict.mdc.dynamic.relation;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.properties.ValueFactory;
import com.google.common.collect.Range;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.NamedBusinessObject;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

/**
 * RelationAttributeType describes a dynamic attribute of a <code>Relation</code>
 */
@ProviderType
public interface RelationAttributeType extends NamedBusinessObject {

    public ValueFactory getValueFactory();

    public Object valueFromDb(Object object) throws SQLException;

    public Object valueToDb(Object object);

    public int getJdbcType();

    public String getDbType();

    public Class getValueType();

    public boolean requiresIndex();

    public String getIndexType();

    /**
     * returns true if this attribute is a referencing attribute, false otherwise
     *
     * @return the resulting boolean value
     */
    public boolean isReference();

    /**
     * returns true if this attribute must have a non null value assigned to it
     *
     * @return the resulting boolean value
     */
    public boolean isRequired();

    public boolean getRequired();

    /**
     * returns true if this attribute is the the default one used by a FolderType
     *
     * @return the resulting boolean value
     */
    public boolean isDefault();

    public void clearDefaultFlag();

    /**
     * returns the specific value type in case of references (eg Folder), while getValueType() returns
     * the generalized type (eg IdBusinessObject)
     *
     * @return the Class of the values
     */
    public Class getSpecificValueType();

    /**
     * returns true if this attribute is not available (hidden) on the GUI
     *
     * @return the resulting boolean value
     */
    public boolean isHidden();

    /**
     * Returns the relation type the receiver belongs to
     *
     * @return the relation type
     */
    public RelationType getRelationType();

    /**
     * Returns a shadow object initialized by the receiver
     *
     * @return a shadow
     */
    public RelationAttributeTypeShadow getShadow();

    /**
     * updates the receiver with the information in the argument
     *
     * @param shadow contains the new attribute values
     */
    public void update(RelationAttributeTypeShadow shadow);

    /**
     * Returns the relation type id the receiver belongs to
     *
     * @return the relation type id
     */
    public int getRelationTypeId();

    /**
     * Gets all {@link Relation}s (including obsoletes) for the given participant.
     *
     * @param participant The participant
     * @return the result list with Relation objects
     */
    public List<Relation> getAllRelations(RelationParticipant participant);

    /**
     * Gets all the {@link Relation}s for the given participant,
     * including obsoletes if specified.
     *
     * @param participant The participant
     * @param includeObsolete The flag that indicates if obsolete Relations are desired
     * @return the resulting list with Relation objects
     * @see Relation#isObsolete()
     */
    public List<Relation> getRelations(RelationParticipant participant, boolean includeObsolete);

    /**
     * Gets the {@link Relation}s between the specified from-to row interval,
     * valid on the given timestamp for the participant,
     * including obsoletes if specified.
     *
     * @param participant The participant
     * @param when
     * @param includeObsolete The flag that indicates if obsolete Relations are desired
     * @param from
     * @param to
     * @return the resulting list with Relation objects
     * @see Relation#isObsolete()
     */
    public List<Relation> getRelations(RelationParticipant participant, Instant when, boolean includeObsolete, int from, int to);

    /**
     * Gets all {@link Relation}s (including obsoletes) for the given participant.
     *
     * @param participant The participant
     * @param period
     * @param includeObsolete The flag that indicates if obsolete Relations are desired
     * @return the result list with Relation objects
     * @see Relation#isObsolete()
     */
    public List<Relation> getRelations(RelationParticipant participant, Range<Instant> period, boolean includeObsolete);

    /**
     * returns true if this attribute is navigatable
     *
     * @return the resulting boolean value
     */
    public boolean isNavigatable();

    /**
     * returns the role name of the receiver
     *
     * @return the role name
     */
    public String getRoleName();

    /**
     * returns true if this attribute type is used in a constraint on the relation type
     *
     * @return the boolean result
     */
    public boolean hasConstraint();

    /**
     * updates the value to indicate whether the attribute is required (true) or not (false)
     *
     * @param isRequired boolean indicating whether attribute is required (true) or not (false)
     * @throws BusinessException if a business exception occurs
     * @throws SQLException      if a database error occurs
     */
    public void updateRequired(boolean isRequired) throws BusinessException, SQLException;

}