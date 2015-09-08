package com.energyict.mdc.dynamic.relation.impl;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.BusinessObjectFactory;
import com.energyict.mdc.common.DatabaseException;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.dynamic.relation.Constraint;
import com.energyict.mdc.dynamic.relation.ConstraintShadow;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationAttributeTypeShadow;
import com.energyict.mdc.dynamic.relation.RelationTransaction;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.dynamic.relation.exceptions.CannotDeleteDefaultRelationConstraintException;
import com.energyict.mdc.dynamic.relation.exceptions.DuplicateNameException;
import com.energyict.mdc.dynamic.relation.exceptions.EmptyConstraintException;
import com.energyict.mdc.dynamic.relation.exceptions.MultipleNonRejectConstraintsNotAllowedException;
import com.energyict.mdc.dynamic.relation.exceptions.NameIsRequiredException;
import com.energyict.mdc.dynamic.relation.impl.legacy.PersistentNamedObject;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConstraintImpl extends PersistentNamedObject implements Constraint {

    private final Clock clock;
    private Thesaurus thesaurus;
    private final List<ConstraintMember> members = new ArrayList<>();
    private final Reference<RelationType> relationType = ValueReference.absent();
    private boolean rejectViolations;
    private Instant modDate;

    @Inject
    ConstraintImpl(DataModel dataModel, Thesaurus thesaurus, Clock clock) {
        super(dataModel);
        this.clock = clock;
        this.thesaurus = thesaurus;
    }

    public ConstraintImpl(DataModel dataModel, Clock clock, RelationType relationType, String name) {
        super(dataModel, name);
        this.clock = clock;
        this.relationType.set(relationType);
    }

    @Override
    protected DataMapper getDataMapper() {
        return this.getDataModel().mapper(Constraint.class);
    }

    @Override
    public BusinessObjectFactory getFactory() {
        throw new ApplicationException("getFactory is no longer supported on ConstraintImpl now that it uses the new Jupiter Kore ORM framework");
    }

    @Override
    public String getType() {
        return Constraint.class.getName();
    }

    @SuppressWarnings("unchecked")
    public void update(ConstraintShadow shadow) {
        this.validateUpdate(shadow);
        this.copyUpdate(shadow);
        this.post();
        this.doDeleteMembers(shadow.getAttributeTypeShadows().getDeletedShadows());
        this.doInsertMembers(shadow.getAttributeTypeShadows().getNewShadows());
    }

    @SuppressWarnings("unchecked")
    protected void init(RelationType relationType, final ConstraintShadow shadow) {
        this.validateNew(relationType, shadow);
        this.copyNew(shadow);
        this.doInsertMembers(shadow.getAttributeTypeShadows());
    }

    protected void copy(ConstraintShadow shadow) {
        setName(shadow.getName());
        this.rejectViolations = shadow.isRejectViolations();
    }

    protected void copyNew(ConstraintShadow shadow) {
        copy(shadow);
    }

    protected void copyUpdate(ConstraintShadow shadow) {
        copy(shadow);
    }

    protected void validateNew(RelationType relationType, ConstraintShadow shadow) {
        validate(relationType, shadow);
    }

    protected void validateUpdate(ConstraintShadow shadow) {
        validate(getRelationType(), shadow);
    }

    @Override
    protected void validateDelete() {
        if (isDefault()) {
            throw new CannotDeleteDefaultRelationConstraintException(this.thesaurus, MessageSeeds.CONSTRAINT_CANNOT_DELETE_DEFAULT);
        }
    }

    @Override
    protected void deleteDependents() throws SQLException, BusinessException {
        super.deleteDependents();
        this.members.clear();
    }

    protected void validate(RelationType relationType, ConstraintShadow shadow) {
        String newName = shadow.getName();
        if (newName == null) {
            throw new NameIsRequiredException(this.thesaurus, MessageSeeds.CONSTRAINT_NAME_IS_REQUIRED);
        }
        if (!newName.equals(getName())) {
            validateConstraint(newName, relationType);
        }
        if (!shadow.isRejectViolations()) {
            for (Constraint each : relationType.getConstraints()) {
                if (!each.isRejectViolations() && (each.getId() != this.getId())) {
                    throw new MultipleNonRejectConstraintsNotAllowedException(this.thesaurus, MessageSeeds.CONSTRAINT_MULTIPLE_NON_REJECT_NOT_ALLOWED);
                }
            }
        }
        if (Checks.is(shadow.getName()).emptyOrOnlyWhiteSpace()) {
            throw new NameIsRequiredException(this.thesaurus, MessageSeeds.CONSTRAINT_NAME_IS_REQUIRED);
        }
        if (shadow.getAttributeTypeShadows().isEmpty()) {
            throw new EmptyConstraintException(this.thesaurus, this, MessageSeeds.CONSTRAINT_WITHOUT_ATTRIBUTES);
        }
    }

    protected void validateConstraint(String name, RelationType relationType) {
        Optional<Constraint> constraint = this.getDataModel().mapper(Constraint.class).getUnique("relationType", relationType, "name", name);
        if (!constraint.isPresent()) {
            throw new DuplicateNameException(this.thesaurus, MessageSeeds.CONSTRAINT_ALREADY_EXISTS, name, relationType.getName());
        }
    }

    private void doDeleteMembers(List<RelationAttributeTypeShadow> deletedShadows) {
        for (RelationAttributeTypeShadow relationAttributeType : deletedShadows) {
            ConstraintMember member = this.findMember(relationAttributeType);
            if (member != null) {
                // Objects that are managed with a composite collection do not need an explicit delete
                this.members.remove(member);
            }
        }
    }

    private ConstraintMember findMember(RelationAttributeTypeShadow relationAttributeType) {
        for (ConstraintMember member : this.members) {
            if (member.getAttributeTypeId() == relationAttributeType.getId()) {
                return member;
            }
        }
        return null;
    }

    private void doInsertMembers(List<RelationAttributeTypeShadow> newShadows) {
        for (RelationAttributeTypeShadow relationAttributeType : newShadows) {
            int attributeTypeId = this.findAttributeTypeByName(relationAttributeType.getName()).getId();
            this.members.add(new ConstraintMember(this, attributeTypeId));
        }
    }

    private RelationAttributeType findAttributeTypeByName(String name) {
        for (RelationAttributeType attributeType : this.getRelationType().getAttributeTypes()) {
            if (name.equals(attributeType.getName())) {
                return attributeType;
            }
        }
        return null;
    }

    public synchronized List<RelationAttributeType> getAttributeTypes() {
        List<RelationAttributeType> attributeTypes = new ArrayList<>(this.members.size());
        for (ConstraintMember member : this.members) {
            attributeTypes.add(this.relationType.get().getAttributeTypeById(member.getAttributeTypeId()));
        }
        return attributeTypes;
    }

    @Override
    protected void validate(String name) throws BusinessException {
        if (Checks.is(name).emptyOrOnlyWhiteSpace()) {
            throw new BusinessException("nameCantBeBlank", "The name cannot be blank");
        }
    }

    public int getRelationTypeId() {
        return this.relationType.get().getId();
    }

    public synchronized RelationType getRelationType() {
        return this.relationType.get();
    }

    public ConstraintShadow getShadow() {
        return new ConstraintShadow(this);
    }

    public boolean isDefault() {
        List attribs = getAttributeTypes();
        if (attribs.size() != 1) {
            return false;
        } else {
            RelationAttributeType attribType = (RelationAttributeType) attribs.get(0);
            return attribType.isDefault();
        }
    }

    public boolean appendAttributeSql(SqlBuilder builder, RelationTransaction transaction) {
        if (!getAttributeTypes().isEmpty() && !ignoreConstraint(transaction)) {
            doAppendAttributeSql(builder, transaction);
            return true;
        } else {
            return false;
        }
    }

    private void doAppendAttributeSql(SqlBuilder builder, RelationTransaction transaction) {
        builder.append(" (");
        boolean first = true;
        for (RelationAttributeType type : getAttributeTypes()) {
            Object object = transaction.get(type);
            if (first) {
                first = false;
            } else {
                builder.append(" and ");
            }
            builder.append(type.getName());
            builder.append(" = ?");
            builder.bindObject(type.valueToDb(object));
        }
        builder.append(")");
    }

    private boolean ignoreConstraint(RelationTransaction transaction) {
        for (RelationAttributeType type : getAttributeTypes()) {
            Object object = transaction.get(type);
            if (object == null) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public List<Relation> getViolatedRelations(RelationTransaction transaction) {
        List result = new ArrayList();
        if (!getAttributeTypes().isEmpty() && !ignoreConstraint(transaction)) {
            try {
                SqlBuilder builder = new SqlBuilder("select id from ");
                builder.append(getRelationType().getDynamicAttributeTableName());
                builder.appendWhereOrAnd();
                if (transaction.getTo() != null) {
                    builder.append("fromdate < ? ");
                    builder.appendWhereOrAnd();
                    if (getRelationType().hasTimeResolution()) {
                        builder.bindUtc(transaction.getTo());
                    } else {
                        builder.bindTimestamp(transaction.getTo());
                    }
                }
                builder.append("(todate is null or todate > ?) ");
                if (getRelationType().hasTimeResolution()) {
                    builder.bindUtc(transaction.getFrom());
                } else {
                    builder.bindTimestamp(transaction.getFrom());
                }
                builder.append(" and ");
                this.doAppendAttributeSql(builder, transaction);
                builder.append(" order by fromdate");
                try (PreparedStatement stmnt = builder.getStatement(this.getConnection())) {
                    try (ResultSet rs = stmnt.executeQuery()) {
                        while (rs.next()) {
                            int relationId = rs.getInt(1);
                            result.add(new RelationFactory((RelationTypeImpl) getRelationType(), this.clock).find(relationId));
                        }
                    }
                }
            } catch (SQLException e) {
                throw new DatabaseException(e);
            }
        }
        return result;
    }

    public boolean isRejectViolations() {
        return this.rejectViolations;
    }

}