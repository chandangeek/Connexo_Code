package com.energyict.mdc.dynamic.relation.impl;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessObjectFactory;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.dynamic.JupiterReferenceFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.relation.Constraint;
import com.energyict.mdc.dynamic.relation.DefaultAttributeTypeDetective;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationAttributeTypeShadow;
import com.energyict.mdc.dynamic.relation.RelationParticipant;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.dynamic.relation.exceptions.CannotDeleteDefaultRelationAttributeException;
import com.energyict.mdc.dynamic.relation.exceptions.DuplicateNameException;
import com.energyict.mdc.dynamic.relation.exceptions.NameContainsInvalidCharactersException;
import com.energyict.mdc.dynamic.relation.exceptions.NameIsRequiredException;
import com.energyict.mdc.dynamic.relation.exceptions.NameTooLongException;
import com.energyict.mdc.dynamic.relation.exceptions.RelationAttributeHasNullValuesException;
import com.energyict.mdc.dynamic.relation.exceptions.RelationTypeDDLException;
import com.energyict.mdc.dynamic.relation.exceptions.ValueFactoryCreationException;
import com.energyict.mdc.dynamic.relation.impl.legacy.PersistentNamedObject;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Ranges;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

public class RelationAttributeTypeImpl extends PersistentNamedObject implements RelationAttributeType {

    private static final String[] RESERVED_WORDS = {"ID", "NAME", "MPTID", "EXTERNID", "FOLDERTYPEID",
            "VERSION", "FROMDATE", "TODATE", "CLOSEDATE", "OBSOLETEDATE", "CRE_DATE", "MOD_DATE", "CREUSERID",
            "MODUSERID", "FLAGS"};

    private final Clock clock;
    private Thesaurus thesaurus;
    private DefaultAttributeTypeDetective defaultAttributeTypeDetective;
    private PropertySpecService propertySpecService;
    private String valueFactoryClassName;
    private ValueFactory valueFactory;
    private String displayName;
    private String roleName;
    private int objectFactoryId;
    private boolean largeString = false;
    private boolean required;
    private boolean navigatable;
    private boolean hidden;
    private final Reference<RelationType> relationType = ValueReference.absent();
    private Boolean isDefault = null;

    @Inject
    RelationAttributeTypeImpl(DataModel dataModel, Clock clock, Thesaurus thesaurus, DefaultAttributeTypeDetective defaultAttributeTypeDetective, PropertySpecService propertySpecService) {
        super(dataModel);
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.defaultAttributeTypeDetective = defaultAttributeTypeDetective;
        this.propertySpecService = propertySpecService;
    }

    public RelationAttributeTypeImpl(DataModel dataModel, Clock clock, RelationType relationType, Thesaurus thesaurus, String name, PropertySpecService propertySpecService) {
        super(dataModel, name);
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.relationType.set(relationType);
        this.propertySpecService = propertySpecService;
    }

    @Override
    protected DataMapper getDataMapper() {
        return this.getDataModel().mapper(RelationAttributeType.class);
    }

    @Override
    public String getType() {
        return RelationAttributeType.class.getName();
    }

    public void update(RelationAttributeTypeShadow shadow) {
        this.validateUpdate(shadow);
        if (this.getRelationType().isActive() && this.required != shadow.isRequired()) {
            this.updateRequired(shadow.isRequired());
        }
        this.copyUpdate(shadow);
        this.post();
    }

    protected void init(RelationType relationType, RelationAttributeTypeShadow shadow) {
        this.validateNew(relationType, shadow);
        this.copyNew(relationType, shadow);
    }

    protected void copy(RelationAttributeTypeShadow shadow) {
        this.setName(shadow.getName());
        this.objectFactoryId = shadow.getObjectFactoryId();
        this.displayName = shadow.getDisplayName();
        this.roleName = shadow.getRoleName();
        this.valueFactoryClassName = shadow.getValueFactoryClassName();
        this.valueFactory = null;
        this.required = shadow.isRequired();
        this.navigatable = isReference() && shadow.isNavigatable();
        this.hidden = shadow.isHidden();
    }

    protected void copyNew(RelationType relationType, RelationAttributeTypeShadow shadow) {
        this.relationType.set(relationType);
        copy(shadow);
    }

    protected void copyUpdate(RelationAttributeTypeShadow shadow) {
        copy(shadow);
    }

    protected void validateNew(RelationType relationType, RelationAttributeTypeShadow shadow) {
        validate(relationType, shadow);
    }

    protected void validateUpdate(RelationAttributeTypeShadow shadow) {
        if (this.isDefault() && shadow.getEssentialAttributesChanged()) {
            throw new RelationAttributeHasNullValuesException(this, this.thesaurus, MessageSeeds.RELATION_ATTRIBUTE_TYPE_STORAGE_CONTAINS_NULL_VALUES);
        }
        this.validate(this.getRelationType(), shadow);
    }

    @Override
    protected void validateDelete() {
        if (this.isDefault()) {
            throw new CannotDeleteDefaultRelationAttributeException(this, this.thesaurus, MessageSeeds.RELATION_ATTRIBUTE_TYPE_CANNOT_DELETE_DEFAULT);
        }
    }

    protected void validate(RelationType relationType, RelationAttributeTypeShadow shadow) {
        String newName = shadow.getName();
        if (this.isInvalidName(newName)) {
            throw new NameIsRequiredException(this.thesaurus, MessageSeeds.RELATION_ATTRIBUTE_TYPE_NAME_IS_REQUIRED);
        }
        this.validate(newName);
        if (newName.length() > 30) {
            throw new NameTooLongException(this.thesaurus, MessageSeeds.RELATION_ATTRIBUTE_TYPE_NAME_TOO_LONG, newName, 30);
        }
        if (!newName.equals(this.getName())) {
            this.validateConstraint(newName, relationType);
        }
        this.newValueFactory(shadow.getValueFactoryClassName(), shadow.getObjectFactoryId());
    }

    protected void validateConstraint(String name, RelationType relationType) {
        Optional<RelationAttributeType> attributeType = this.getDataModel().mapper(RelationAttributeType.class).
                getUnique("relationType", relationType, "name", name);
        if (attributeType.isPresent()) {
            throw new DuplicateNameException(this.thesaurus, MessageSeeds.RELATION_ATTRIBUTE_TYPE_ALREADY_EXISTS, name, relationType.getName());
        }
    }

    public void updateRequired(boolean isRequired) {
        if (this.required == isRequired) {
            return;
        }
        if (isRequired && hasNullValues()) { // #eiserver-178 no longer take the ORU-table into account
            throw new RelationAttributeHasNullValuesException(this, this.thesaurus, MessageSeeds.RELATION_ATTRIBUTE_TYPE_STORAGE_CONTAINS_NULL_VALUES);
        }
        new RelationTypeDdlGenerator(this.getDataModel(), getRelationType(), this.thesaurus, true).alterAttributeColumnRequired(RelationAttributeTypeImpl.this, isRequired);
        setRequired(isRequired);
    }

    private boolean hasNullValues() {
        RelationType relation = getRelationType();
        return hasNullValues(relation.getDynamicAttributeTableName()); // #eiserver-178 no longer take the ORU-table into account
    }

    private boolean hasNullValues(String tableName) {
        try {
            try (PreparedStatement stmnt = this.getNullValueSql(tableName).getStatement(this.getConnection())) {
                try (ResultSet rs = stmnt.executeQuery()) {
                    return rs.next();
                }
            }
        }
        catch (SQLException e) {
            throw new RelationTypeDDLException(e, this.relationType.get().getName(), this.thesaurus, MessageSeeds.DDL_ERROR);
        }
    }

    private SqlBuilder getNullValueSql(String tableName) {
        SqlBuilder builder = new SqlBuilder("select * from ");
        builder.append(tableName);
        builder.appendWhereOrAnd();
        builder.append(getName());
        builder.append(" is null and rownum = 1");
        return builder;
    }

    protected void setRequired(boolean required) {
        this.required = required;
    }

    public String getDbType() {
        return getValueFactory().getDatabaseTypeName();
    }

    public Object valueFromDb(Object object) throws SQLException {
        return getValueFactory().valueFromDatabase(object);
    }

    public Object valueToDb(Object object) {
        return getValueFactory().valueToDatabase(object);
    }

    public int getJdbcType() {
        return getValueFactory().getJdbcType();
    }

    public boolean requiresIndex() {
        return getValueFactory().requiresIndex();
    }

    public String getIndexType() {
        return getValueFactory().getIndexType();
    }

    public synchronized ValueFactory getValueFactory() {
        if (this.valueFactory == null) {
            this.valueFactory = this.newValueFactory();
        }
        return this.valueFactory;
    }

    private ValueFactory newValueFactory () {
        return this.newValueFactory(this.valueFactoryClassName, this.objectFactoryId);
    }

    private ValueFactory newValueFactory (String valueFactoryClassName, int objectFactoryId) {
        try {
            if (valueFactoryClassName.equals(JupiterReferenceFactory.class.getCanonicalName())) {
                PropertySpec propertySpec =
                        this.propertySpecService.referencePropertySpec(
                                valueFactoryClassName,  // Don't care as we are only interested in the ValueFactory
                                false,                  // Don't care as we are only interested in the ValueFactory
                                FactoryIds.forId(objectFactoryId));
                return propertySpec.getValueFactory();
            }
            else {
                return this.propertySpecService.getValueFactory((Class<? extends ValueFactory<Object>>) Class.forName(valueFactoryClassName));
            }
        }
        catch (ClassNotFoundException | ClassCastException ex) {
            throw new ValueFactoryCreationException(ex, valueFactoryClassName, this.thesaurus, MessageSeeds.VALUEFACTORY_CREATION);
        }
    }

    public Class getValueType() {
        return getValueFactory().getValueType();
    }

    public Class getSpecificValueType() {
        return getValueFactory().getValueType();
    }

    public String getValueFactoryClassName() {
        return this.valueFactoryClassName;
    }

    public String getDisplayName() {
        if (this.displayName != null) {
            return this.displayName;
        }
        else {
            return getName();
        }
    }

    public String getRoleName() {
        if (this.roleName == null) {
            return getDisplayName();
        }
        else {
            return this.roleName;
        }
    }

    @Override
    protected void validate(String name) {
        if (Checks.is(name).emptyOrOnlyWhiteSpace()) {
            throw new NameIsRequiredException(this.thesaurus, MessageSeeds.RELATION_ATTRIBUTE_TYPE_NAME_IS_REQUIRED);
        }
        String validChars = this.getValidCharacters();
        for (int i = 0; i < name.length(); i++) {
            if (validChars.indexOf(name.charAt(i)) == -1) {
                throw new NameContainsInvalidCharactersException(this.thesaurus, MessageSeeds.RELATION_ATTRIBUTE_TYPE_NAME_CONTAINS_INVALID_CHARACTERS, name, this.getValidCharacters());
            }
        }
    }

    protected String getValidCharacters() {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_";
    }

    public int getRelationTypeId() {
        return this.relationType.get().getId();
    }

    public boolean isRequired() {
        return this.required;
    }

    public boolean getRequired() {
        return isRequired();
    }

    public boolean isNavigatable() {
        return this.navigatable;
    }

    public boolean getNavigatable() {
        return isNavigatable();
    }

    public boolean isDefault() {
        if (isDefault == null) {
            isDefault = doGetDefault();
        }
        return isDefault;
    }

    @Override
    public void clearDefaultFlag() {
        isDefault = null;
    }

    private boolean doGetDefault() {
        return this.defaultAttributeTypeDetective.isDefaultAttribute(this);
    }

    public synchronized RelationTypeImpl getRelationType() {
        return (RelationTypeImpl) this.relationType.get();
    }

    public List<Relation> getAllRelations(RelationParticipant participant) {
        return new RelationFactory(this.getRelationType(), this.clock).findByParticipantAndAttributeType(participant, this);
    }

    public List<Relation> getRelations(RelationParticipant participant, boolean includeObsolete) {
        return new RelationFactory(this.getRelationType(), this.clock).findByParticipantAndAttributeType(participant, this, includeObsolete);
    }

    public List<Relation> getRelations(RelationParticipant participant, Instant when, boolean includeObsolete, int from, int to) {
        return new RelationFactory(this.getRelationType(), this.clock).findByParticipantAndAttributeType(participant, this, when, includeObsolete, from, to);
    }

    public List<Relation> getRelations(RelationParticipant participant, Range<Instant> period, boolean includeObsolete) {
        List<Relation> result = new ArrayList<>();
        List<Relation> all = getRelations(participant, includeObsolete);
        for (Relation relation : all) {
            if (Ranges.does(period).overlap(relation.getPeriod())) {
                result.add(relation);
            }
        }
        return result;
    }

    public RelationAttributeTypeShadow getShadow() {
        return new RelationAttributeTypeShadow(this);
    }

    @Override
    public BusinessObjectFactory getFactory() {
        throw new ApplicationException("getFactory is no longer supported on RelationAttributeTypeImpl now that it uses the new Jupiter Kore ORM framework");
    }

    protected boolean isInvalidName(String name) {
        for (int i = 0; i < RESERVED_WORDS.length; i++) {
            if (RESERVED_WORDS[i].equalsIgnoreCase(name)) {
                return true;
            }
        }
        return DatabaseReservedWord.isReservedWord(name) || !startsWithLetter(name);
    }

    private boolean startsWithLetter(String string) {
        return ((string != null) && (!string.isEmpty()) && (Character.isLetter(string.charAt(0))));
    }

    public boolean isReference() {
        return objectFactoryId > 0;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean hasConstraint() {
        return !this.getDataModel().
                    query(Constraint.class, ConstraintMember.class).
                    select(where("attributeTypeId").isEqualTo(this.getId())).isEmpty();
    }

    @Override
    public String toString() {
        return getRelationType().getName() + "." + getName();
    }

}