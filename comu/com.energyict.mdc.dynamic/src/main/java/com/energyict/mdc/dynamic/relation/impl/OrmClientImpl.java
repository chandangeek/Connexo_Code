package com.energyict.mdc.dynamic.relation.impl;

import com.energyict.mdc.common.BusinessObject;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.dynamic.relation.Constraint;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationType;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Order;

import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation of the {@link OrmClient} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-17 (10:33)
 */
public class OrmClientImpl implements OrmClient {
    private final DataModel dataModel;

    public OrmClientImpl(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    private enum DatabaseReservedWords {
        SHARE,
        RAW,
        DROP,
        BETWEEN,
        FROM,
        DESC,
        OPTION,
        PRIOR,
        LONG,
        THEN,
        DEFAULT,
        ALTER,
        IS,
        INTO,
        MINUS,
        INTEGER,
        NUMBER,
        GRANT,
        IDENTIFIED,
        ALL,
        TO,
        ORDER,
        ON,
        FLOAT,
        DATE,
        HAVING,
        CLUSTER,
        NOWAIT,
        RESOURCE,
        ANY,
        TABLE,
        INDEX,
        FOR,
        UPDATE,
        WHERE,
        CHECK,
        SMALLINT,
        WITH,
        DELETE,
        BY,
        ASC,
        REVOKE,
        LIKE,
        SIZE,
        RENAME,
        NOCOMPRESS,
        NULL,
        GROUP,
        VALUES,
        AS,
        IN,
        VIEW,
        EXCLUSIVE,
        COMPRESS,
        SYNONYM,
        SELECT,
        INSERT,
        EXISTS,
        NOT,
        TRIGGER,
        ELSE,
        CREATE,
        INTERSECT,
        PCTFREE,
        DISTINCT,
        CONNECT,
        SET,
        MODE,
        OF,
        UNIQUE,
        VARCHAR2,
        VARCHAR,
        LOCK,
        OR,
        CHAR,
        DECIMAL,
        UNION,
        PUBLIC,
        AND,
        START
    }

    @Override
    public boolean isDatabaseReservedWord(String name) {
        for (DatabaseReservedWords reservedWord : DatabaseReservedWords.values()) {
            if (reservedWord.toString().equals(name.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DataMapper<RelationType> getRelationTypeFactory() {
        return this.dataModel.mapper(RelationType.class);
    }

    @Override
    public DataMapper<RelationAttributeType> getRelationAttributeTypeFactory() {
        return this.dataModel.mapper(RelationAttributeType.class);
    }

    @Override
    public DataMapper<Constraint> getConstraintFactory() {
        return this.dataModel.mapper(Constraint.class);
    }

    @Override
    public List<RelationType> findRelationTypesByParticipant(BusinessObject participant) {
        IdBusinessObjectFactory factory = (IdBusinessObjectFactory) participant.getFactory();
        return this.dataModel.
                query(RelationType.class, RelationAttributeType.class).
                select(
                    where("objectFactoryId").isEqualTo(factory.getId()),
                    Order.ascending("name"));
    }

    @Override
    public RelationAttributeType findByRelationTypeAndName(RelationType type, String name) {
        return this.dataModel.mapper(RelationAttributeType.class).
                getUnique(
                        "relationType", type,
                        "name", name).orNull();
    }

    @Override
    public Constraint findConstraintByNameAndRelationType(String constraintName, RelationType relationType) {
        return this.dataModel.mapper(Constraint.class).
                getUnique(
                        "relationType", relationType,
                        "name", constraintName).orNull();
    }

    @Override
    public List<Constraint> findByRelationAttributeType(RelationAttributeTypeImpl relationAttributeType) {
        return this.dataModel.
                query(Constraint.class, ConstraintMember.class).
                select(where("attributeTypeId").isEqualTo(relationAttributeType.getId()));
    }

}