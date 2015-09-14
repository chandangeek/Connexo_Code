package com.energyict.mdc.dynamic.relation.impl;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.BusinessObjectFactory;
import com.energyict.mdc.common.DatabaseException;
import com.energyict.mdc.common.ShadowList;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.relation.Constraint;
import com.energyict.mdc.dynamic.relation.ConstraintShadow;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationAttributeTypeShadow;
import com.energyict.mdc.dynamic.relation.RelationSearchFilter;
import com.energyict.mdc.dynamic.relation.RelationTransaction;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.dynamic.relation.RelationTypeShadow;
import com.energyict.mdc.dynamic.relation.exceptions.CannotAddRequiredRelationAttributeException;
import com.energyict.mdc.dynamic.relation.exceptions.CannotDeleteRelationType;
import com.energyict.mdc.dynamic.relation.exceptions.DuplicateNameException;
import com.energyict.mdc.dynamic.relation.exceptions.LockAttributeShouldBeReferenceTypeException;
import com.energyict.mdc.dynamic.relation.exceptions.LockAttributeShouldBeRequiredException;
import com.energyict.mdc.dynamic.relation.exceptions.NameContainsInvalidCharactersException;
import com.energyict.mdc.dynamic.relation.exceptions.NameIsRequiredException;
import com.energyict.mdc.dynamic.relation.exceptions.NameTooLongException;
import com.energyict.mdc.dynamic.relation.exceptions.NoLockAttributeException;
import com.energyict.mdc.dynamic.relation.exceptions.RelationTypeDDLException;
import com.energyict.mdc.dynamic.relation.impl.legacy.PersistentNamedObject;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.util.conditions.Where.where;

public class RelationTypeImpl extends PersistentNamedObject implements RelationType {

    private final Clock clock;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;
    private final List<RelationAttributeType> attributeTypes = new ArrayList<>();
    private final List<Constraint> constraints = new ArrayList<>();
    private boolean active;
    private boolean hasTimeResolution;
    private String displayName;
    private Instant modDate;
    private int lockAttributeTypeId;
    private RelationAttributeType lockAttributeType;
    private boolean system;

    @Inject
    public RelationTypeImpl(DataModel dataModel, Clock clock, TransactionService transactionService, Thesaurus thesaurus) {
        super(dataModel);
        this.clock = clock;
        this.transactionService = transactionService;
        this.thesaurus = thesaurus;
    }

    TransactionService getTransactionService() {
        return transactionService;
    }

    @Override
    public BusinessObjectFactory getFactory() {
        throw new ApplicationException("getFactory is no longer supported on RelationTypeImpl now that it uses the new Jupiter Kore ORM framework");
    }

    @Override
    public String getType() {
        return RelationType.class.getName();
    }

    @Override
    protected DataMapper<RelationType> getDataMapper() {
        return this.getDataModel().mapper(RelationType.class);
    }

    @Override
    public void delete() {
        try {
            super.delete();
        }
        catch (BusinessException | SQLException e) {
            /* Todo: Should be able to remove this once all objects are use the new ORM service
             * and delete as defined on BusinessObject no longer throws BusinessException and SQLException. */
            throw new ApplicationException(e);
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean hasTimeResolution() {
        return this.hasTimeResolution;
    }

    public synchronized RelationAttributeType getLockAttributeType() {
        if (this.lockAttributeType == null) {
            this.lockAttributeType = this.findLockAttribute();
        }
        return this.lockAttributeType;
    }

    private RelationAttributeType findLockAttributeByName(String name) {
        for (RelationAttributeType attributeType : this.attributeTypes) {
            if (name.equals(attributeType.getName())) {
                return attributeType;
            }
        }
        return null;
    }

    private RelationAttributeType findLockAttribute() {
        return this.findAttribute(this.lockAttributeTypeId);
    }

    private RelationAttributeType findAttribute(int id) {
        for (RelationAttributeType attributeType : this.attributeTypes) {
            if (attributeType.getId() == id) {
                return attributeType;
            }
        }
        return null;
    }

    public String getDisplayName() {
        if ((this.displayName != null) && (!this.displayName.trim().isEmpty())) {
            return this.displayName;
        }
        else {
            return getName();
        }
    }

    public String getCustomDisplayName() {
        return this.getDisplayName();
    }

    public synchronized List<Constraint> getConstraints() {
        return ImmutableList.copyOf(this.constraints);
    }

    private Constraint findConstraint(int id) {
        for (Constraint constraint : this.constraints) {
            if (constraint.getId() == id) {
                return constraint;
            }
        }
        return null;
    }

    public List<Constraint> getRejectViolationConstraints() {
        List<Constraint> result = new ArrayList<>();
        for (Object o : getConstraints()) {
            Constraint constraint = (Constraint) o;
            if (constraint.isRejectViolations()) {
                result.add(constraint);
            }
        }
        return result;
    }

    public List<Constraint> getResolveViolationConstraints() {
        List<Constraint> result = new ArrayList<>();
        for (Object o : getConstraints()) {
            Constraint constraint = (Constraint) o;
            if (!constraint.isRejectViolations()) {
                result.add(constraint);
            }
        }
        return result;
    }

    public RelationTypeShadow getShadow() {
        return new RelationTypeShadow(this);
    }

    public void update(RelationTypeShadow shadow, PropertySpecService propertySpecService) {
        this.validateUpdate(shadow);
        this.copyUpdate(shadow);
        this.processAttributes(shadow.getAttributeTypeShadows(), propertySpecService);
        this.post();
        this.lockAttributeTypeId = this.findLockAttributeByName(shadow.getLockAttributeTypeShadow().getName()).getId();
        this.processConstraints(shadow.getConstraintShadows());
        this.post();
    }

    protected void processAttributes(ShadowList<RelationAttributeTypeShadow> list, PropertySpecService propertySpecService) {
        if (list.isDirty()) {
            this.processDeletedAttributes(list.getDeletedShadows());
            this.processUpdatedAttributes(list.getUpdatedShadows());
            this.processNewAttributes(list.getNewShadows(), propertySpecService);
        }
    }

    private void processDeletedAttributes(List<RelationAttributeTypeShadow> deletedShadows) {
        for (RelationAttributeTypeShadow attShadow : deletedShadows) {
            RelationAttributeType target = this.findAttribute(attShadow.getId());
            if (target != null) {
                deleteAttributeType(target);
            }
        }
    }

    private void processUpdatedAttributes(List<RelationAttributeTypeShadow> updatedShadows) {
        for (RelationAttributeTypeShadow attShadow : updatedShadows) {
            RelationAttributeType target = this.findAttribute(attShadow.getId());
            if (target != null) {
                target.update(attShadow);
            }
        }
    }

    private void processNewAttributes(List<RelationAttributeTypeShadow> newShadows, PropertySpecService propertySpecService) {
        for (RelationAttributeTypeShadow attShadow : newShadows) {
            attShadow.setRelationTypeId(getId());
            RelationAttributeType target = this.doAddAttribute(attShadow, propertySpecService);
            attShadow.setId(target.getId());
        }
    }

    protected void processConstraints(ShadowList<ConstraintShadow> list) {
        if (list.isDirty()) {
            this.processDeletedConstraints(list.getDeletedShadows());
            this.processUpdatedConstraints(list.getUpdatedShadows());
            this.processNewConstraints(list.getNewShadows());
        }
    }

    private void processDeletedConstraints(List<ConstraintShadow> deletedShadows) {
        for (ConstraintShadow conShadow : deletedShadows) {
            ConstraintImpl constraint = (ConstraintImpl) this.findConstraint(conShadow.getId());
            if (constraint != null) {
                // Objects that are managed in a composite collection do not need explicit delete
                constraint.validateDelete();
                this.constraints.remove(constraint);
            }
        }
    }

    private void processUpdatedConstraints(List<ConstraintShadow> updatedShadows) {
        for (ConstraintShadow conShadow : updatedShadows) {
            Constraint target = this.findConstraint(conShadow.getId());
            if (target != null) {
                target.update(conShadow);
            }
        }
    }

    private void processNewConstraints(List<ConstraintShadow> newShadows) {
        for (ConstraintShadow shadow : newShadows) {
            shadow.setRelationTypeId(getId());
            ConstraintImpl constraint = new ConstraintImpl(this.getDataModel(), this.clock, this, shadow.getName());
            constraint.init(this, shadow);
            this.constraints.add(constraint);
        }
    }

    public void init(RelationTypeShadow shadow, PropertySpecService propertySpecService) {
        this.validateNew(shadow);
        this.copyNew(shadow);
        this.processAttributes(shadow.getAttributeTypeShadows(), propertySpecService);
        this.postNew();
        this.lockAttributeTypeId = this.findLockAttributeByName(shadow.getLockAttributeTypeShadow().getName()).getId();
        this.processConstraints(shadow.getConstraintShadows());
        this.post();
    }

    protected void copy(RelationTypeShadow shadow) {
        setName(shadow.getName());
        this.hasTimeResolution = shadow.hasTimeResolution();
        this.displayName = shadow.getDisplayName();
        this.system = shadow.isSystem();
    }

    protected void copyNew(RelationTypeShadow shadow) {
        copy(shadow);
    }

    protected void copyUpdate(RelationTypeShadow shadow) {
        copy(shadow);
    }

    protected void validateNew(RelationTypeShadow shadow) {
        validate(shadow);
    }

    protected void validateUpdate(RelationTypeShadow shadow) {
        validate(shadow);
        if (this.active && !shadow.getAttributeTypeShadows().getNewShadows().isEmpty()) {
            for (RelationAttributeTypeShadow relationAttributeTypeShadow : shadow.getAttributeTypeShadows().getNewShadows()) {
                if (relationAttributeTypeShadow.getRequired() && hasAny()) {
                    throw new CannotAddRequiredRelationAttributeException(relationAttributeTypeShadow.getName(), shadow.getName(), this.thesaurus, MessageSeeds.RELATION_ATTRIBUTE_TYPE_CANNOT_ADD_REQUIRED);
                }
            }
        }
    }

    private boolean hasAny() {
        try {
            return this.getBaseFactory().hasAny();
        }
        catch (SQLException e) {
            throw new RelationTypeDDLException(e, this.getName(), this.thesaurus, MessageSeeds.DDL_ERROR);
        }
    }

    protected void validate(RelationTypeShadow shadow) {
        String newName = shadow.getName();
        if (newName == null) {
            throw new NameIsRequiredException(this.thesaurus, MessageSeeds.RELATION_TYPE_NAME_IS_REQUIRED);
        }
        if (newName.length() > 24) {
            throw new NameTooLongException(this.thesaurus, MessageSeeds.RELATION_TYPE_NAME_TOO_LONG, newName, 24);
        }
        validate(newName);
        if (!newName.equals(getName())) {
            validateConstraint(newName);
        }
        validateActivate(shadow);
    }

    @Override
    protected void validateConstraint(String name) {
        DataMapper<RelationType> factory = this.getDataMapper();
        // first 21 chars should be unique because of constraint ddl generation,
        // name is being pre- and postfixed with couple of tags and we need to
        // limit names to max 30 for oracle
        if (name.length() < 21) {
            if (!factory.find("name", name).isEmpty()) {
                throw new DuplicateNameException(this.thesaurus, MessageSeeds.RELATION_TYPE_ALREADY_EXISTS, name);
            }
        }
        else {
            String uniquePart = name.substring(0, 21);
            if (!factory.select(where("name").like(uniquePart + "%")).isEmpty()) {
                throw new DuplicateNameException(this.thesaurus, MessageSeeds.RELATION_TYPE_ALREADY_EXISTS, uniquePart);
            }
        }
    }

    public synchronized List<RelationAttributeType> getAttributeTypes() {
        return ImmutableList.copyOf(this.attributeTypes);
    }

    private RelationAttributeType doAddAttribute(RelationAttributeTypeShadow shadow, PropertySpecService propertySpecService) {
        RelationAttributeType attributeType = this.createAttributeType(shadow, propertySpecService);
        new RelationTypeDdlGenerator(this.getDataModel(), this, this.thesaurus, true).addAttributeColumn(attributeType);
        return attributeType;
    }

    protected PreparedStatement getAttributeStatement(boolean includeObsolete)
            throws SQLException {
        return prepareStatement(this.getAttributeSql(includeObsolete));
    }

    protected final void executeDdl(String sqlStatement) throws SQLException {
        try (Statement statement = getConnection().createStatement()) {
            statement.execute(sqlStatement);
        }
    }

    private PreparedStatement prepareStatement(String sql) throws SQLException {
        return getConnection().prepareStatement(sql);
    }

    public Map<RelationAttributeType, Object> getAttributes(Relation relation) {
        Map<RelationAttributeType, Object> result = new HashMap<>();
        if (hasNoAttributes()) {
            return result;
        }
        try {
            try (PreparedStatement statement = getAttributeStatement(relation.isObsolete())) {
                statement.setInt(1, relation.getId());
                try (ResultSet rs = statement.executeQuery()) {
                    List<RelationAttributeType> aTypes = getAttributeTypes();
                    if (rs.next()) {
                        for (int i = 0; i < aTypes.size(); i++) {
                            RelationAttributeType each = aTypes.get(i);
                            result.put(each, each.valueFromDb(rs.getObject(i + 1)));
                        }
                    }
                }
            }
        }
        catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
        return result;
    }

    protected String getAttributeSql(boolean includeObsolete) {
        StringBuilder builder = new StringBuilder("select ");
        Iterator<RelationAttributeType> it = getAttributeTypes().iterator();
        boolean firstTime = true;
        while (it.hasNext()) {
            if (firstTime) {
                firstTime = false;
            }
            else {
                builder.append(" , ");
            }
            RelationAttributeType each = it.next();
            builder.append(each.getName());
        }
        builder.append(" from ");
        if (includeObsolete) {
            builder.append(getObsoleteAttributeTableName());
        }
        else {
            builder.append(getDynamicAttributeTableName());
        }
        builder.append(" where id = ?");
        return builder.toString();
    }

    public List<Relation> getAffectedRelations(RelationTransaction transaction) {
        List<Relation> result = new ArrayList<>();
        List<Constraint> constraints = getResolveViolationConstraints();
        if (!constraints.isEmpty()) {
            result = getBaseFactory().affectedBy(transaction, constraints);
        }
        return result;
    }

    public String getDynamicAttributeTableName() {
        if (this.hasTimeResolution) {
            return "dru" + getName();
        }
        else {
            return "drd" + getName();
        }
    }

    public String getObsoleteAttributeTableName() {
        if (this.hasTimeResolution) {
            return "oru" + getName();
        }
        else {
            return "ord" + getName();
        }
    }

    protected String getDynamicAttributeSequenceName() {
        return getDynamicAttributeTableName() + "id";
    }

    protected String getDropDynamicAttributeTableSql() {
        return "drop table " + getDynamicAttributeTableName();
    }

    protected String getDropObsoleteAttributeTableSql() {
        return "drop table " + getObsoleteAttributeTableName();
    }

    protected String getDropDynamicAttributeSequenceSql() {
        return "drop sequence " + getDynamicAttributeSequenceName();
    }

    protected void createDynamicAttributeTable() throws SQLException {
        new RelationTypeDdlGenerator(this.getDataModel(), this, this.thesaurus, true).execute();
    }

    public void dropDynamicAttributeTable() throws SQLException {
        this.executeDdl(this.getDropDynamicAttributeTableSql());
    }

    public void dropObsoleteAttributeTable() throws SQLException {
        this.executeDdl(this.getDropObsoleteAttributeTableSql());
    }

    public void dropDynamicAttributeSequence() throws SQLException {
        this.executeDdl(this.getDropDynamicAttributeSequenceSql());
    }

    @Override
    protected void validateDelete() {
        if (this.active) {
            List remainingRelations = this.getBaseFactory().findByRelationType();
            if (!remainingRelations.isEmpty()) {
                throw new CannotDeleteRelationType(this, this.thesaurus, MessageSeeds.RELATION_TYPE_CANNOT_DELETE_WITH_EXISTING_INSTANCES);
            }
            this.validateDeleteAttributes();
            this.validateDeleteConstraints();
        }
    }

    private void validateDeleteAttributes() {
        for (RelationAttributeType attribute : this.attributeTypes) {
            RelationAttributeTypeImpl each = (RelationAttributeTypeImpl) attribute;
            each.validateDelete();
        }
    }

    private void validateDeleteConstraints() {
        for (Constraint constraint : this.constraints) {
            ConstraintImpl each = (ConstraintImpl) constraint;
            each.validateDelete();
        }
    }

    @Override
    protected void deleteDependents() throws SQLException, BusinessException {
        super.deleteDependents();
        if (this.isActive()) {
            this.deactivate();
        }
        for (Constraint constraint : this.constraints) {
            ConstraintImpl each = (ConstraintImpl) constraint;
            each.deleteDependents();
        }
        this.constraints.clear();
        this.attributeTypes.clear();
    }

    private void validateActivate() {
        RelationAttributeType attribType = getLockAttributeType();
        if (attribType == null) {
            throw new NoLockAttributeException(this.getName(), this.thesaurus, MessageSeeds.RELATION_TYPE_LOCK_ATTRIBUTE_IS_REQUIRED);
        }
        if (!attribType.isReference()) {
            throw new LockAttributeShouldBeReferenceTypeException(attribType.getName(), this.getName(), this.thesaurus, MessageSeeds.RELATION_TYPE_LOCK_ATTRIBUTE_IS_REQUIRED);
        }
        if (!attribType.isRequired()) {
            throw new LockAttributeShouldBeRequiredException(this.getName(), attribType.getName(), this.thesaurus, MessageSeeds.RELATION_TYPE_LOCK_ATTRIBUTE_IS_REQUIRED);
        }
    }

    private void validateActivate(RelationTypeShadow shadow) {
        RelationAttributeTypeShadow attShadow = shadow.getLockAttributeTypeShadow();
        if (attShadow == null) {
            throw new NoLockAttributeException(shadow.getName(), this.thesaurus, MessageSeeds.RELATION_TYPE_LOCK_ATTRIBUTE_IS_REQUIRED);
        }
        if (!shadow.getAttributeTypeShadows().contains(attShadow)) {
            throw new NoLockAttributeException(shadow.getName(), this.thesaurus, MessageSeeds.RELATION_TYPE_LOCK_ATTRIBUTE_IS_REQUIRED);
        }
        if (attShadow.getObjectFactoryId() == 0) {
            throw new LockAttributeShouldBeReferenceTypeException(attShadow.getName(), shadow.getName(), this.thesaurus, MessageSeeds.RELATION_TYPE_LOCK_ATTRIBUTE_IS_REQUIRED);
        }
        if (!attShadow.isRequired()) {
            throw new LockAttributeShouldBeRequiredException(shadow.getName(), attShadow.getName(), this.thesaurus, MessageSeeds.RELATION_TYPE_LOCK_ATTRIBUTE_IS_REQUIRED);
        }
    }

    public void activate() {
        this.validateActivate();
        this.active = true;
        this.post();
        try {
            this.createDynamicAttributeTable();
        }
        catch (SQLException e) {
            RelationTypeImpl.this.active = false;
            throw new RelationTypeDDLException(e, this.getName(), this.thesaurus, MessageSeeds.DDL_ERROR);
        }
    }

    public void deactivate() throws SQLException, BusinessException {
        if (this.isDefault()) {
            throw new BusinessException(
                    "cannotInactivateDefaultRelationType",
                    "This relation type cannot be inactivated since it is still used as default relation");
        }
        if (this.hasAny()) {
            throw new BusinessException(
                    "cannotInactivateRelationType",
                    "This relation type cannot be inactivated since it is still in use");
        }
        // first drop metadata because we need the table to still exists
        this.active = false;
        this.post();
        try {
            this.dropMetaData();
            this.dropDynamicAttributeTable();
            this.dropObsoleteAttributeTable();
            this.dropDynamicAttributeSequence();
        }
        catch (SQLException e) {
            this.active = true;
            throw e;
        }
    }

    protected void dropMetaData() throws SQLException {
        new RelationTypeDdlGenerator(this.getDataModel(), this, this.thesaurus, true).dropMetaData();
    }

    public RelationAttributeType getAttributeType(int index) {
        return getAttributeTypes().get(index);
    }

    public RelationAttributeType getAttributeTypeById(int id) {
        return this.findAttribute(id);
    }

    public RelationAttributeType getAttributeType(String name) {
        for (RelationAttributeType each : getAttributeTypes()) {
            if (each.getName().equals(name)) {
                return each;
            }
        }
        return null;
    }

    public boolean hasAttribute(String name) {
        return getAttributeType(name) != null;
    }

    public RelationAttributeType createAttributeType(RelationAttributeTypeShadow raShadow, PropertySpecService propertySpecService) {
        RelationAttributeTypeImpl relationAttributeType = new RelationAttributeTypeImpl(this.getDataModel(), this.clock, this, this.thesaurus, raShadow.getName(), propertySpecService);
        relationAttributeType.init(this, raShadow);
        this.attributeTypes.add(relationAttributeType);
        return relationAttributeType;
    }

    public void deleteAttributeType(RelationAttributeType raType) {
        this.deleteAttributeType((RelationAttributeTypeImpl) raType);
    }

    private void deleteAttributeType(RelationAttributeTypeImpl raType) {
        // Objects that are managed in a composite collection do no need an explicit delete
        raType.validateDelete();
        doDeleteAttributeType(raType);
        this.attributeTypes.remove(raType);
    }

    private void doDeleteAttributeType(RelationAttributeType raType) {
        new RelationTypeDdlGenerator(this.getDataModel(), this, this.thesaurus, true).executeDelete(raType);
    }

    public Relation get(int id) {
        return this.getBaseFactory().find(id);
    }

    public Relation findByPrimaryKey(Serializable key) {
        return this.getBaseFactory().findByPrimaryKey(key);
    }

    @Override
    protected void validate(String name) {
        if ((name == null) || (name.trim().isEmpty())) {
            throw new NameIsRequiredException(this.thesaurus, MessageSeeds.RELATION_TYPE_NAME_IS_REQUIRED);
        }
        String validChars = getValidCharacters();
        for (int i = 0; i < name.length(); i++) {
            if (validChars.indexOf(name.charAt(i)) == -1) {
                throw new NameContainsInvalidCharactersException(
                        this.thesaurus,
                        MessageSeeds.RELATION_TYPE_NAME_CONTAINS_INVALID_CHARACTERS,
                        name,
                        this.getValidCharacters());
            }
        }
    }

    protected String getValidCharacters() {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_";
    }

    public boolean hasNoAttributes() {
        return this.getAttributeTypes().isEmpty();
    }

    @Override
    public String toString() {
        return this.getCustomDisplayName();
    }

    public RelationTransaction newRelationTransaction() {
        return new RelationTransactionImpl(clock, this);
    }

    public boolean isDefault() {
        for (RelationAttributeType mdwAttributeType : getAttributeTypes()) {
            if (mdwAttributeType.isDefault()) {
                return true;
            }
        }
        return false;
    }

    public RelationAttributeType getDefaultAttributeType() {
        for (RelationAttributeType each : getAttributeTypes()) {
            if (each.isDefault()) {
                return each;
            }
        }
        return null;
    }

    public List<Relation> findByFilter(RelationSearchFilter searchFilter) {
        return this.getBaseFactory().findByFilter(searchFilter);
    }

    protected RelationFactory getBaseFactory() {
        return new RelationFactory(this, this.clock);
    }

    Instant getModDate() {
        return this.modDate;
    }

    @Override
    public boolean isSystemRelationType() {
        return system;
    }

}