package com.energyict.mdc.dynamic.relation.impl;

import com.elster.jupiter.orm.DataMapper;
import com.energyict.mdc.common.BusinessObject;
import com.energyict.mdc.dynamic.relation.Constraint;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationType;

import java.util.List;

/**
 * Acts as a facade for the {@link com.elster.jupiter.orm.DataModel}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-17 (10:35)
 */
public interface OrmClient {

    boolean isDatabaseReservedWord(String name);

    public DataMapper<RelationType> getRelationTypeFactory();

    public DataMapper<RelationAttributeType> getRelationAttributeTypeFactory();

    public DataMapper<Constraint> getConstraintFactory();

    public List<RelationType> findRelationTypesByParticipant(BusinessObject participant);

    public RelationAttributeType findByRelationTypeAndName(RelationType type, String name);

    public Constraint findConstraintByNameAndRelationType(String constraintName, RelationType relationType);

    public List<Constraint> findByRelationAttributeType(RelationAttributeTypeImpl relationAttributeType);

}