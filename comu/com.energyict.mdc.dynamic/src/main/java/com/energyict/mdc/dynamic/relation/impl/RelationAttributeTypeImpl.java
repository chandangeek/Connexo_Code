package com.energyict.mdc.dynamic.relation.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessObjectFactory;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.dynamic.JupiterReferenceFactory;
import com.energyict.mdc.dynamic.LegacyReferenceFactory;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.ValueFactory;
import com.energyict.mdc.dynamic.relation.exceptions.CannotDeleteDefaultRelationAttributeException;
import com.energyict.mdc.dynamic.relation.CompositeAttributeTypeDetective;
import com.energyict.mdc.dynamic.relation.DefaultAttributeTypeDetective;
import com.energyict.mdc.dynamic.relation.exceptions.DuplicateNameException;
import com.energyict.mdc.dynamic.relation.exceptions.MessageSeeds;
import com.energyict.mdc.dynamic.relation.exceptions.NameContainsInvalidCharactersException;
import com.energyict.mdc.dynamic.relation.exceptions.NameIsRequiredException;
import com.energyict.mdc.dynamic.relation.exceptions.NameTooLongException;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.exceptions.RelationAttributeHasNullValuesException;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationAttributeTypeShadow;
import com.energyict.mdc.dynamic.relation.RelationParticipant;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.dynamic.relation.exceptions.RelationTypeDDLException;
import com.energyict.mdc.dynamic.relation.exceptions.ValueFactoryCreationException;
import com.energyict.mdc.dynamic.relation.impl.legacy.PersistentNamedObject;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RelationAttributeTypeImpl extends PersistentNamedObject implements RelationAttributeType {

    private Thesaurus thesaurus;
    private PropertySpecService propertySpecService;
    private String valueFactoryClassName;
    private ValueFactory valueFactory;
    private String displayName;
    private String roleName;
    private int numberLookupId;
    private int objectFactoryId;
    private boolean largeString = false;
    private boolean required;
    private boolean navigatable;
    private boolean hidden;
    private final Reference<RelationType> relationType = ValueReference.absent();
    private Boolean isDefault = null;

    @Inject
    RelationAttributeTypeImpl(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    public RelationAttributeTypeImpl(RelationType relationType, Thesaurus thesaurus, String name, PropertySpecService propertySpecService) {
        super(name);
        this.thesaurus = thesaurus;
        this.relationType.set(relationType);
        this.propertySpecService = propertySpecService;
    }

    public RelationAttributeTypeImpl(RelationType relationType, Thesaurus thesaurus, String name, ValueFactory valueFactory) {
        super(name);
        this.thesaurus = thesaurus;
        this.relationType.set(relationType);
        this.valueFactory = valueFactory;
    }

    @Override
    protected DataMapper getDataMapper() {
        return Bus.getServiceLocator().getOrmClient().getRelationAttributeTypeFactory();
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
            throw new RelationAttributeHasNullValuesException(this.thesaurus, this);
        }
        this.validate(this.getRelationType(), shadow);
    }

    @Override
    protected void validateDelete() {
        if (this.isDefault()) {
            throw new CannotDeleteDefaultRelationAttributeException(this.thesaurus, this);
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
        if (Bus.getServiceLocator().getOrmClient().findByRelationTypeAndName(relationType, name) != null) {
            throw new DuplicateNameException(this.thesaurus, MessageSeeds.RELATION_ATTRIBUTE_TYPE_ALREADY_EXISTS, name, relationType.getName());
        }
    }

    public void updateRequired(boolean isRequired) {
        if (this.required == isRequired) {
            return;
        }
        if (isRequired && hasNullValues()) { // #eiserver-178 no longer take the ORU-table into account
            throw new RelationAttributeHasNullValuesException(this.thesaurus, this);
        }
        new RelationTypeDdlGenerator(getRelationType(), this.thesaurus, true).alterAttributeColumnRequired(RelationAttributeTypeImpl.this, isRequired);
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
            throw new RelationTypeDDLException(this.thesaurus, e, this.relationType.get().getName());
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

    public String getStructType() {
        return getValueFactory().getStructType();
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
            if (valueFactoryClassName.equals(LegacyReferenceFactory.class.getCanonicalName())) {
                return new LegacyReferenceFactory((IdBusinessObjectFactory) Environment.DEFAULT.get().findFactory(objectFactoryId));
            }
            else if (valueFactoryClassName.equals(JupiterReferenceFactory.class.getCanonicalName())) {
                PropertySpec propertySpec =
                        this.propertySpecService.referencePropertySpec(
                                valueFactoryClassName,  // Don't care as we are only interested in the ValueFactory
                                false,                  // Don't care as we are only interested in the ValueFactory
                                FactoryIds.forId(objectFactoryId));
                return propertySpec.getValueFactory();
            }
            return (ValueFactory) Class.forName(valueFactoryClassName).newInstance();
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            throw new ValueFactoryCreationException(this.thesaurus, ex, valueFactoryClassName);
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
        List<DefaultAttributeTypeDetective> modules = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(DefaultAttributeTypeDetective.class);
        CompositeAttributeTypeDetective detective = new CompositeAttributeTypeDetective(modules);
        return detective.isDefaultAttribute(this);
    }

    public synchronized RelationType getRelationType() {
        return this.relationType.get();
    }

    public List<Relation> getAllRelations(RelationParticipant participant) {
        return new RelationFactory(this.getRelationType()).findByParticipantAndAttributeType(participant, this);
    }

    public List<Relation> getRelations(RelationParticipant participant, boolean includeObsolete) {
        return new RelationFactory(this.getRelationType()).findByParticipantAndAttributeType(participant, this, includeObsolete);
    }

    public List<Relation> getRelations(RelationParticipant participant, Date date, boolean includeObsolete, int from, int to) {
        return new RelationFactory(this.getRelationType()).findByParticipantAndAttributeType(participant, this, date, includeObsolete, from, to);
    }

    public List<Relation> getRelations(RelationParticipant participant, Interval period, boolean includeObsolete) {
        List<Relation> result = new ArrayList<>();
        List<Relation> all = getRelations(participant, includeObsolete);
        for (Relation relation : all) {
            if (period.overlaps(relation.getPeriod())) {
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
        for (int i = 0; i < reservedWords.length; i++) {
            if (name.toUpperCase().equals(reservedWords[i])) {
                return true;
            }
        }
        return Bus.getServiceLocator().getOrmClient().isDatabaseReservedWord(name) || !startsWithLetter(name);
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

    protected boolean appendForeignKeySql(StringBuilder builder, String fkName) {
        if (this.supportsReferentialIntegrity()) {
            IdBusinessObjectFactory factory = (IdBusinessObjectFactory) Environment.DEFAULT.get().findFactory(this.objectFactoryId);
            builder.append(" constraint ");
            builder.append(fkName);
            builder.append(" foreign key(");
            builder.append(getName());
            builder.append(") references ");
            builder.append(factory.getTableName());
            builder.append("(id)");
            return true;
        }
        return false;
    }

    protected boolean supportsReferentialIntegrity() {
        return getValueFactory().isReference();
    }

    public boolean hasConstraint() {
        return !Bus.getServiceLocator().getOrmClient().findByRelationAttributeType(this).isEmpty();
    }

    @Override
    public String toString() {
        return getRelationType().getName() + "." + getName();
    }

    private static final String[] reservedWords = {"ID", "NAME", "MPTID", "EXTERNID", "FOLDERTYPEID",
            "VERSION", "FROMDATE", "TODATE", "CLOSEDATE", "OBSOLETEDATE", "CRE_DATE", "MOD_DATE", "CREUSERID",
            "MODUSERID", "FLAGS"};


}
