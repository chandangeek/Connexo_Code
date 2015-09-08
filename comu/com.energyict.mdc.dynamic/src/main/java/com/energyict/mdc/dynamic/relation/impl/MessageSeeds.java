package com.energyict.mdc.dynamic.relation.impl;

import com.energyict.mdc.dynamic.relation.RelationService;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (11:00)
 */
public enum MessageSeeds implements MessageSeed {
    DDL_ERROR(100, "dynamic.relation.unexpected.sql", "Unexpected SQLError while modifying storage area for relation type \"{0}\""),
    VALUEFACTORY_CREATION(500, "valueFactory.creation.failure", "Failure to create value factory of type \"{0}\""),
    RELATION_TYPE_NAME_IS_REQUIRED(1001, "relationType.name.required", "The name of a relation type is required"),
    RELATION_TYPE_ALREADY_EXISTS(1002, "relationType.duplicateNameX", "A relation type with name \"{0}\" already exists"),
    RELATION_TYPE_NAME_CONTAINS_INVALID_CHARACTERS(1003, "relationTypeNameX.invalidCharacters", "The name of relation type '0' can only contain the following valid characters: \"{1}\""),
    RELATION_TYPE_NAME_TOO_LONG(1004, "relationTypeNameX.maximumLengthY", "The name of a relation type is limited to {1,number} characters: \"{0}\""),
    RELATION_TYPE_LOCK_ATTRIBUTE_IS_REQUIRED(1005, "relationType.lockAttribute.required", "The relation type \"{0}\" cannot be created without a lock attribute"),
    RELATION_TYPE_LOCK_ATTRIBUTE_SHOULD_BE_REFERENCE(1006, "relationType.lockAttribute.notReference", "The lock attribute \"{1}\" of relation type \"{0}\" should be a reference to another object"),
    RELATION_TYPE_LOCK_ATTRIBUTE_SHOULD_BE_REQUIRED(1006, "relationType.lockAttribute.requiredFlag", "The lock attribute \"{1}\" of relation type \"{0}\" should be a required attribute"),
    RELATION_TYPE_CANNOT_DELETE_WITH_EXISTING_INSTANCES(1007, "cannotDeleteRelationType", "Cannot delete relation type \"{0}\" that is still in use"),
    RELATION_ATTRIBUTE_TYPE_NAME_IS_REQUIRED(2000, "relationAttributeType.name.required", "The name of a relation attribute type is required"),
    RELATION_ATTRIBUTE_TYPE_NAME_CONTAINS_INVALID_CHARACTERS(2001, "relationAttributeTypeName.invalidCharacters", "The name of relation attribute type '0' can only contain the following valid characters: \"{1}\""),
    RELATION_ATTRIBUTE_TYPE_NAME_TOO_LONG(2002, "relationAttributeTypeName.maximumLengthY", "The name of a relation attribute type is limited to {1,number} characters: \"{0}\""),
    RELATION_ATTRIBUTE_TYPE_ALREADY_EXISTS(2003, "relationAttributeType.duplicateNameX", "A relation attribute type with name \"{0}\" already exists in relation type \"{1}\""),
    RELATION_ATTRIBUTE_TYPE_CANNOT_DELETE_DEFAULT(2004, "cannotDeleteDefaultRelationAttribute", "Cannot delete a default relation type attribute \"{0}\" of relation type \"{1}\""),
    RELATION_ATTRIBUTE_TYPE_CANNOT_UPDATE_DEFAULT(2005, "cannotUpdateDefaultRelationAttribute", "Cannot update a default relation type attribute \"{0}\" of relation type \"{1}\""),
    RELATION_ATTRIBUTE_TYPE_STORAGE_CONTAINS_NULL_VALUES(2006, "attributeXHasNullValues", "Cannot make relation type attribute \"{0}\" of relation type \"{1}\" required because the storage area already contains NULL values"),
    RELATION_ATTRIBUTE_TYPE_CANNOT_ADD_REQUIRED(2007, "cannotAddRequiredAttributeXWithExistingY", "Cannot add required attribute \"{0}\" to relation type \"{1}\" because objects of that relation type already exist"),
    CONSTRAINT_CANNOT_DELETE_DEFAULT(3000, "cannotDeleteDefaultConstraint", "Constraint on default attribute cannot be deleted"),
    CONSTRAINT_NAME_IS_REQUIRED(3001, "constraint.name.required", "The name of a relation attribute type is required"),
    CONSTRAINT_ALREADY_EXISTS(3002, "constraint.duplicateNameX", "A constraint with name \"{0}\" already exists in relation type \"{1}\""),
    CONSTRAINT_MULTIPLE_NON_REJECT_NOT_ALLOWED(3003, "multipleNonRejectConstraintsNotAllowed", "Multiple constraints that do not reject violations is not allowed"),
    CONSTRAINT_WITHOUT_ATTRIBUTES(3004, "constraintAttributesCannotBeEmpty", "Constraint '{0'} makes no sense without attributes"),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;

    MessageSeeds(int number, String key, String defaultFormat) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    @Override
    public Level getLevel() {
        return Level.SEVERE;
    }

    @Override
    public String getModule() {
        return RelationService.COMPONENT_NAME;
    }

}