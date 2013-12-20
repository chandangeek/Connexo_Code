package com.energyict.mdc.dynamic.relation;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.NamedBusinessObject;
import com.energyict.mdc.dynamic.ValueFactory;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * RelationAttributeType describes a dynamic attribute of a <code>Relation</code>
 */
public interface RelationAttributeType extends NamedBusinessObject {

    public String getValueFactoryClassName();

    public ValueFactory getValueFactory();

    public Object valueFromDb(Object object) throws SQLException;

    public Object valueToDb(Object object);

    public int getJdbcType();

    public String getStructType();

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
     * @throws BusinessException if a business exception occurred
     * @throws SQLException      if a database error occurred
     */
    public void update(RelationAttributeTypeShadow shadow) throws BusinessException, SQLException;

    /**
     * Returns the relation type id the receiver belongs to
     *
     * @return the relation type id
     */
    public int getRelationTypeId();

    /**
     * returns all relations (including obsoletes) for the given participant
     *
     * @param participant
     * @return the result list with Relation objects
     */
    public List<Relation> getAllRelations(RelationParticipant participant);

    /**
     * returns all the relations for the given participant, including obsoletes if specified
     *
     * @param participant
     * @param includeObsolete
     * @return the resulting list with Relation objects
     */
    public List<Relation> getRelations(RelationParticipant participant, boolean includeObsolete);

    /**
     * returns the relations between the specified from-to row interval, valid on the given date for the participant,
     * including obsoletes if specified.
     *
     * @param participant
     * @param date
     * @param includeObsolete
     * @param from
     * @param to
     * @return the resulting list with Relation objects
     */
    public List<Relation> getRelations(RelationParticipant participant, Date date, boolean includeObsolete, int from, int to);

    /**
     * returns all relations (including obsoletes) for the given participant
     *
     * @param participant
     * @param period
     * @param includeObsolete
     * @return the result list with Relation objects
     */
    public List<Relation> getRelations(RelationParticipant participant, Interval period, boolean includeObsolete);

    /**
     * returns true if this attribute is navigatable
     *
     * @return the resulting boolean value
     */
    public boolean isNavigatable();

    public boolean getNavigatable();

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