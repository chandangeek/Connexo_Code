package com.energyict.mdc.dynamic.relation.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.energyict.mdc.common.*;
import com.energyict.mdc.dynamic.ValueFactory;
import com.energyict.mdc.dynamic.relation.*;
import com.energyict.mdc.dynamic.relation.impl.legacy.PersistentNamedObject;
import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static com.elster.jupiter.util.conditions.Where.where;

public class RelationTypeImpl extends PersistentNamedObject implements RelationType, PersistenceAware {

    private final List<RelationAttributeType> attributeTypes = new ArrayList<>();
    private final List<Constraint> constraints = new ArrayList<>();
    private boolean active;
    private boolean hasTimeResolution;
    private String displayName;
    private Date modDate;
    private int lockAttributeTypeId;
    private RelationAttributeType lockAttributeType;
    private TypeId typeId;
    private boolean system;

    public RelationTypeImpl() {
        super();
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
    public void postLoad() {
        this.typeId = new SoftTypeId(FactoryIds.RELATION_TYPE.id(), this.getId());
    }

    @Override
    protected DataMapper<RelationType> getDataMapper() {
        return Bus.getServiceLocator().getOrmClient().getRelationTypeFactory();
    }

    @Override
    protected void postNew() {
        super.postNew();
        this.postLoad();
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean hasTimeResolution() {
        return this.hasTimeResolution;
    }

    public int getLockAttributeTypeId() {
        return this.lockAttributeTypeId;
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
        return UserEnvironment.getDefault().getCustomTranslation(getDisplayName());
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

    public void update(RelationTypeShadow shadow) throws BusinessException, SQLException {
        this.validateUpdate(shadow);
        this.copyUpdate(shadow);
        this.processAttributes(shadow.getAttributeTypeShadows());
        this.post();
        this.lockAttributeTypeId = this.findLockAttributeByName(shadow.getLockAttributeTypeShadow().getName()).getId();
        this.processConstraints(shadow.getConstraintShadows());
        this.post();
    }

    protected void processAttributes(ShadowList<RelationAttributeTypeShadow> list) throws SQLException, BusinessException {
        if (list.isDirty()) {
            this.processDeletedAttributes(list.getDeletedShadows());
            this.processUpdatedAttributes(list.getUpdatedShadows());
            this.processNewAttributes(list.getNewShadows());
        }
    }

    private void processDeletedAttributes(List<RelationAttributeTypeShadow> deletedShadows) throws SQLException, BusinessException {
        for (RelationAttributeTypeShadow attShadow : deletedShadows) {
            RelationAttributeType target = this.findAttribute(attShadow.getId());
            if (target != null) {
                deleteAttributeType(target);
            }
        }
    }

    private void processUpdatedAttributes(List<RelationAttributeTypeShadow> updatedShadows) throws BusinessException, SQLException {
        for (RelationAttributeTypeShadow attShadow : updatedShadows) {
            RelationAttributeType target = this.findAttribute(attShadow.getId());
            if (target != null) {
                target.update(attShadow);
            }
        }
    }

    private void processNewAttributes(List<RelationAttributeTypeShadow> newShadows) throws SQLException, BusinessException {
        for (RelationAttributeTypeShadow attShadow : newShadows) {
            attShadow.setRelationTypeId(getId());
            RelationAttributeType target = doAddAttribute(attShadow);
            attShadow.setId(target.getId());
        }
    }

    protected void processConstraints(ShadowList<ConstraintShadow> list) throws SQLException, BusinessException {
        if (list.isDirty()) {
            this.processDeletedConstraints(list.getDeletedShadows());
            this.processUpdatedConstraints(list.getUpdatedShadows());
            this.processNewConstraints(list.getNewShadows());
        }
    }

    private void processNewConstraints(List<ConstraintShadow> newShadows) throws SQLException, BusinessException {
        for (ConstraintShadow shadow : newShadows) {
            shadow.setRelationTypeId(getId());
            ConstraintImpl constraint = new ConstraintImpl(this, shadow.getName());
            constraint.init(this, shadow);
            this.constraints.add(constraint);
        }
    }

    private void processUpdatedConstraints(List<ConstraintShadow> updatedShadows) throws BusinessException, SQLException {
        for (ConstraintShadow conShadow : updatedShadows) {
            Constraint target = this.findConstraint(conShadow.getId());
            if (target != null) {
                target.update(conShadow);
            }
        }
    }

    private void processDeletedConstraints(List<ConstraintShadow> deletedShadows) throws BusinessException {
        for (ConstraintShadow conShadow : deletedShadows) {
            ConstraintImpl constraint = (ConstraintImpl) this.findConstraint(conShadow.getId());
            if (constraint != null) {
                // Objects that are managed in a composite collection do not need explicit delete
                constraint.validateDelete();
                this.constraints.remove(constraint);
            }
        }
    }

    public void init(RelationTypeShadow shadow) throws SQLException, BusinessException {
        this.validateNew(shadow);
        this.copyNew(shadow);
        this.processAttributes(shadow.getAttributeTypeShadows());
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

    protected void validateNew(RelationTypeShadow shadow) throws BusinessException {
        validate(shadow);
    }

    protected void validateUpdate(RelationTypeShadow shadow) throws BusinessException, SQLException {
        validate(shadow);
        if (this.active && !shadow.getAttributeTypeShadows().getNewShadows().isEmpty()) {
            for (RelationAttributeTypeShadow relationAttributeTypeShadow : shadow.getAttributeTypeShadows().getNewShadows()) {
                if (relationAttributeTypeShadow.getRequired() && hasAny()) {
                    throw new BusinessException("cannotAddRequiredAttributeXWithExistingY", "Cannot add required attribute {0} because objects of type {1} already exist", relationAttributeTypeShadow.getName(), shadow
                            .getName());
                }
            }
        }
    }

    private boolean hasAny() throws SQLException {
        return this.getBaseFactory().hasAny();
    }

    protected void validate(RelationTypeShadow shadow) throws BusinessException {
        String newName = shadow.getName();
        if (newName == null) {
            throw new BusinessException("noName", "Named object does not have a name.");
        }
        if (newName.length() > 24) {
            throw new BusinessException("invalidNameXMaxYLong",
                    "The name '{0}' is invalid since it can only be {1,number} characters long.", newName, 24);
        }
        validate(newName);
        validateFields(shadow);
        if (!newName.equals(getName())) {
            validateConstraint(newName);
        }
        validateActivate(shadow);
    }

    private void validateFields(RelationTypeShadow shadow) throws BusinessException {
        this.validateMaxLength(shadow.getName(), "name", 24, false);
        this.validateMaxLength(shadow.getDisplayName(), "displayName", 256, true);
    }

    @Override
    protected void validateConstraint(String name) throws DuplicateException {
        DataMapper<RelationType> factory = this.getDataMapper();
        // first 21 chars should be unique because of constraint ddl generation,
        // name is being pre- and postfixed with couple of tags and we need to
        // limit names to max 30 for oracle
        if (name.length() < 21) {
            if (!factory.find("name", name).isEmpty()) {
                throw new DuplicateException(
                        "duplicateRelationTypeX",
                        "A relation type with the name \"{0}\" already exists",
                        name);
            }
        }
        else {
            String uniquePart = name.substring(0, 21);
            if (!factory.select(where("name").like(uniquePart + "%")).isEmpty()) {
                throw new DuplicateException(
                        "duplicateRelationTypeStartingWithX",
                        "A relation type starting with \"{0}\" already exists",
                        uniquePart);
            }
        }
    }

    public synchronized List<RelationAttributeType> getAttributeTypes() {
        return ImmutableList.copyOf(this.attributeTypes);
    }

    public void addAttribute(String fieldName, String attDisplayName, ValueFactory factory) throws BusinessException, SQLException {
        this.addAttribute(fieldName, factory, factory.getObjectFactoryId());
    }

    private void addAttribute(String fieldName, ValueFactory factory, int objectFactoryId) throws BusinessException, SQLException {
        RelationAttributeTypeShadow shadow = new RelationAttributeTypeShadow();
        shadow.setValueFactoryClass(factory.getClass());
        shadow.setName(fieldName);
        shadow.setRelationTypeId(this.getId());
        shadow.setObjectFactoryId(objectFactoryId);
        this.doAddAttribute(shadow);
    }

    private RelationAttributeType doAddAttribute(RelationAttributeTypeShadow shadow) throws SQLException, BusinessException {
        RelationAttributeType attributeType = this.createAttributeType(shadow);
        new RelationTypeDdlGenerator(this, true).addAttributeColumn(attributeType);
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
        HashMap<RelationAttributeType, Object> result = new HashMap<>();
        if (hasNoAttributes()) {
            return result;
        }
        try {
            PreparedStatement statement = getAttributeStatement(relation.isObsolete());
            try {
                statement.setInt(1, relation.getId());
                ResultSet rs = statement.executeQuery();
                try {
                    List<RelationAttributeType> aTypes = getAttributeTypes();
                    if (rs.next()) {
                        for (int i = 0; i < aTypes.size(); i++) {
                            RelationAttributeType each = aTypes.get(i);
                            result.put(each, each.valueFromDb(rs.getObject(i + 1)));
                        }
                    }
                }
                finally {
                    rs.close();
                }
            }
            finally {
                statement.close();
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
        new RelationTypeDdlGenerator(this, true).execute();
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
    protected void validateDelete() throws SQLException, BusinessException {
        if (this.active) {
            List remainingRelations = this.getBaseFactory().findByRelationType();
            if (!remainingRelations.isEmpty()) {
                throw new BusinessException("cannotDeleteRelationType",
                        "This relation type cannot be deleted since it is still in use");
            }
            this.validateDeleteAttributes();
            this.validateDeleteConstraints();
        }
    }

    private void validateDeleteAttributes() throws BusinessException {
        for (RelationAttributeType attribute : this.attributeTypes) {
            RelationAttributeTypeImpl each = (RelationAttributeTypeImpl) attribute;
            each.validateDelete();
        }
    }

    private void validateDeleteConstraints() throws BusinessException {
        for (Constraint constraint : this.constraints) {
            ConstraintImpl each = (ConstraintImpl) constraint;
            each.validateDelete();
        }
    }

    @Override
    protected void deleteDependents() throws SQLException, BusinessException {
        if (this.isActive()) {
            this.deactivate();
        }
        for (RelationAttributeType attributeType : this.attributeTypes) {
            ((RelationAttributeTypeImpl) attributeType).clearDefaultFlag();
        }
    }

    private void validateActivate() throws BusinessException {
        RelationAttributeType attribType = getLockAttributeType();
        if (attribType == null) {
            throw new BusinessException("noLockAttributeDefined", "No lock attribute defined");
        }
        if (!attribType.isReference()) {
            throw new BusinessException(
                    "lockOnNonReferenceAttributeX",
                    "The lock is set to a non reference attribute \"{0}\"",
                    attribType.getName());
        }
        if (!attribType.isRequired()) {
            throw new BusinessException(
                    "lockOnNonRequiredAttributeX",
                    "The lock is set to a non required attribute \"{0}\"",
                    attribType.getName());
        }

    }

    private void validateActivate(RelationTypeShadow shadow) throws BusinessException {
        RelationAttributeTypeShadow attShadow = shadow.getLockAttributeTypeShadow();
        if (attShadow == null) {
            throw new BusinessException("noLockAttributeDefined", "No lock attribute defined");
        }
        if (!shadow.getAttributeTypeShadows().contains(attShadow)) {
            throw new BusinessException("noLockAttributeDefined", "No lock attribute defined");
        }
        if (attShadow.getObjectFactoryId() == 0) {
            throw new BusinessException(
                    "lockOnNonReferenceAttributeX",
                    "The lock is set to a non reference attribute \"{0}\"",
                    attShadow.getName());
        }
        if (!attShadow.isRequired()) {
            throw new BusinessException(
                    "lockOnNonRequiredAttributeX",
                    "The lock is set to a non required attribute \"{0}\"",
                    attShadow.getName());
        }
    }

    public void activate() throws SQLException, BusinessException {
        this.validateActivate();
        this.active = true;
        this.post();
        try {
            this.createDynamicAttributeTable();
        }
        catch (SQLException e) {
            RelationTypeImpl.this.active = false;
            throw e;
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
        new RelationTypeDdlGenerator(this, true).dropMetaData();
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

    public List<RelationAttributeType> getAvailableAttributeTypes(RelationParticipant participant) {
        return Bus.getServiceLocator().getOrmClient().findByRelationTypeAndParticipant(this, (BusinessObject) participant);
    }

    public boolean hasAttribute(String name) {
        return getAttributeType(name) != null;
    }

    public RelationAttributeType createAttributeType(RelationAttributeTypeShadow raShadow) throws SQLException, BusinessException {
        RelationAttributeTypeImpl relationAttributeType = new RelationAttributeTypeImpl(this, raShadow.getName());
        relationAttributeType.init(this, raShadow);
        this.attributeTypes.add(relationAttributeType);
        return relationAttributeType;
    }

    public void deleteAttributeType(RelationAttributeType raType) throws SQLException, BusinessException {
        this.deleteAttributeType((RelationAttributeTypeImpl) raType);
    }

    private void deleteAttributeType(RelationAttributeTypeImpl raType) throws SQLException, BusinessException {
        // Objects that are managed in a composite collection do no need an explicit delete
        raType.validateDelete();
        doDeleteAttributeType(raType);
        this.attributeTypes.remove(raType);
    }

    private void doDeleteAttributeType(RelationAttributeType raType) throws SQLException {
        new RelationTypeDdlGenerator(this, true).executeDelete(raType);
    }

    public Relation get(int id) {
        return this.getBaseFactory().find(id);
    }

    public Class<Relation> getInstanceType() {
        return Relation.class;
    }

    public List<Relation> findAll() {
        throw new ApplicationException("Call to findAll() on Rel not supported");
    }

    public Relation findByPrimaryKey(Serializable key) {
        return this.getBaseFactory().findByPrimaryKey(key);
    }

    public Relation findByHandle(byte[] handle) {
        return this.getBaseFactory().findByHandle(handle);
    }

    public Class getShadowClass() {
        throw new ApplicationException("Call to getShadowClass() on RelationTypeImpl not supported");
    }

    public PropertiesMetaData getPropertiesMetaData() {
        return null;
    }

    @Override
    protected void validate(String name) throws BusinessException {
        if ((name == null) || (name.trim().isEmpty())) {
            throw new BusinessException("nameCantBeBlank", "The name cannot be blank");
        }
        String validChars = getValidCharacters();
        for (int i = 0; i < name.length(); i++) {
            if (validChars.indexOf(name.charAt(i)) == -1) {
                throw new BusinessException(
                        "nameXcontainsInvalidChars",
                        "The name \"{0}\" contains invalid characters",
                        name);
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

    public IdBusinessObjectFactory getMetaTypeFactory() {
        return null;
    }

    public RelationTransaction newRelationTransaction() {
        return new RelationTransactionImpl(this);
    }

    public Relation createRelation(RelationTransaction transaction) throws BusinessException, SQLException {
        return transaction.doExecute();
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
        return new RelationFactory(this);
    }

    Date getModDate() {
        return this.modDate;
    }

    /**
     * {@inheritDoc}
     */
    public final BusinessObjectFactory getSubtypeFactory() {
        return this.getMetaTypeFactory();
    }

    /**
     * {@inheritDoc}
     */
    public final TypeId getTargetTypeId() {
        return this.typeId;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isMetaTypeFactory() {
        return false;
    }

    public List<Relation> getModifiedSince(Date since) {
        return this.getBaseFactory().findModifiedSince(since);
    }

    @Override
    public boolean isSystemRelationType() {
        return system;
    }

}