package com.energyict.mdc.dynamic.relation;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.BusinessObjectFactory;
import com.energyict.mdc.common.NamedBusinessObject;
import com.energyict.mdc.dynamic.ValueFactory;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Just like FolderType is a specification for a Typed Folder a RelationType
 * represents a specification for any other user defined object. It defines
 * customer defined attributes.
 */
public interface RelationType extends NamedBusinessObject, BusinessObjectFactory<Relation> {

    @Override
    public void delete();

    /**
     * gets the IdBusinessObject corr. with the given id
     *
     * @param id the id
     * @return the IdBusinessObject corr. with the given id
     */
    public Relation get(int id);

    /**
     * returns the attribute types for this relation type
     *
     * @return a List of RelationAttributeType objects
     */
    List<RelationAttributeType> getAttributeTypes();

    /**
     * returns the list of unique constraints tied to this relation type
     *
     * @return a List of Constraint objects
     */
    List<Constraint> getConstraints();

    /**
     * returns the list of constraints that will reject violations and throw an
     * exception if one attempts to add a relation that violates the constraint
     *
     * @return a List of Constraint objects
     */
    List<Constraint> getRejectViolationConstraints();

    /**
     * tests if the receiver is configured for having versions on time
     * resolution
     *
     * @return true if the type has time resolution, false otherwise
     */
    boolean hasTimeResolution();

    /**
     * tests if the receiver is active
     *
     * @return true if active, false otherwise
     */
    boolean isActive();

    /**
     * returns the attribute values for a given <code>Relation</code>
     *
     * @param relation the relation
     * @return a Map with keys the attribute type and values the attribute
     *         values.
     */
    Map<RelationAttributeType, Object> getAttributes(Relation relation);

    /**
     * returns the list of affected relations when inserting the new relation,
     * defined in the transaction
     *
     * @param transaction the transaction containing the new relation be
     *                    returned
     * @return a List with affected relation objects
     */
    List<Relation> getAffectedRelations(RelationTransaction transaction);

    /**
     * returns an empty transaction object for relation construction
     *
     * @return the transaction object
     */
    RelationTransaction newRelationTransaction();

    /**
     * creates a new relation as specified in the transaction object
     *
     * @param transaction transaction containing the definition for the new relation
     * @return the Relation object
     */
    Relation createRelation(RelationTransaction transaction) throws BusinessException, SQLException;

    /**
     * tests if this folder type has this attribute defined
     *
     * @param name the attribute name to test
     * @return true if defined , false otherwise
     */
    boolean hasAttribute(String name);

    /**
     * returns the attribute type with the given index
     *
     * @param index the zero based index
     * @return the attribute type.
     */
    RelationAttributeType getAttributeType(int index);

    /**
     * Returns the {@link RelationAttributeType} with the specified id.
     *
     * @param id The id of the RelationAttributeType
     * @return the attribute type.
     */
    RelationAttributeType getAttributeTypeById(int id);

    /**
     * returns the attribute type with the given name
     *
     * @param name the name to match
     * @return the attribute type or null
     */
    RelationAttributeType getAttributeType(String name);

    /**
     * returns the list of attribute types that can be used to host the
     * participant
     *
     * @param participant
     * @return a List of RelationAttributeType objects
     */
    List<RelationAttributeType> getAvailableAttributeTypes(RelationParticipant participant);

    /**
     * returns a shadow object initialized with the receiver
     *
     * @return a shadow object
     */
    RelationTypeShadow getShadow();

    /**
     * updates the folder type
     *
     * @param shadow contains the new attribute values
     * @throws BusinessException if a business exception occurred
     * @throws SQLException      if a database error occurred
     */
    void update(final RelationTypeShadow shadow) throws BusinessException, SQLException;

    /**
     * creates a new folder attribute type
     *
     * @param faShadow contains the new folder attribute type attribute values.
     * @return the new folder attribute type
     * @throws SQLException      if a database error occurred
     * @throws BusinessException if a business exception occurred
     */
    RelationAttributeType createAttributeType(RelationAttributeTypeShadow faShadow) throws SQLException, BusinessException;

    /**
     * deletes a folder attribute type
     *
     * @param faType the folder attribute type to delete
     * @throws SQLException      if a database error occurred
     * @throws BusinessException if a business exception occurred
     */
    void deleteAttributeType(RelationAttributeType faType) throws SQLException, BusinessException;

    /**
     * activates the receiver. This will generate a database table to store the
     * values of the defined attribute types
     *
     * @throws SQLException      if a database error occurred
     * @throws BusinessException if a business exception occurred
     */
    void activate();

    /**
     * deactivates the receiver
     *
     * @throws SQLException      if a database error occurred
     * @throws BusinessException if a business exception occurred
     */
    void deactivate() throws SQLException, BusinessException;

    /**
     * returns the name of the table storing the dynamic attributes
     *
     * @return the dynamic attribute table name
     */
    String getDynamicAttributeTableName();

    /**
     * returns the name of the table storing the obsolete dynamic attributes
     *
     * @return the obsolete dynamic attribute table name
     */
    String getObsoleteAttributeTableName();

    /**
     * Add a new attribute to this relation type
     *
     * @param fieldName   the internal name of the field
     * @param displayName the name used for displaying the field
     * @param factory     the type factory for the type
     * @throws SQLException      if a database error occured.
     * @throws BusinessException if a business error occured.
     */
    void addAttribute(String fieldName, String displayName, ValueFactory factory) throws BusinessException, SQLException;

    /**
     * returns the receiver's display name (which can - in contrast to the name
     * - contain whatever characters you want)
     *
     * @return the receiver's display name
     */
    String getDisplayName();

    /**
     * tests if the receiver is a default relation of the FolderType
     *
     * @return true if the receiver is a default relation of the FolderType,
     *         false otherwise
     */
    boolean isDefault();

    /**
     * Returns the attribute used to lock relation creation/editing
     */
    RelationAttributeType getLockAttributeType();

    /**
     * Returns the id of the attribute used to lock relation creation/editing
     */
    int getLockAttributeTypeId();

    RelationAttributeType getDefaultAttributeType();

    /**
     * return a list with relations for this type according to the specified
     * searchfilter criteria
     *
     * @param searchFilter the searchfilter criteria
     * @return a list of relations
     */
    List<Relation> findByFilter(RelationSearchFilter searchFilter);

    /**
     * get all relations that have been modified since the specified date
     *
     * @param since query modifications since date
     * @return
     */
    List<Relation> getModifiedSince(Date since);

    /**
     * The system may set up certain relation types for internal use. These are marked as System RelationTypes.
     *
     * @return true if this is a System Relation Type, false otherwise.
     */
    boolean isSystemRelationType();

}