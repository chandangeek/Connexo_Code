package com.energyict.mdc.dynamic.relation;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.transaction.Transaction;
import com.google.common.collect.Range;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.dynamic.VersionedDynamicAttributeOwner;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

/**
 * <code>Transaction</code> wrapping a <code>Relation</code>
 *
 * @see Transaction
 */
@ProviderType
public interface RelationTransaction extends Transaction<Relation>, VersionedDynamicAttributeOwner {

    public Object get(RelationAttributeType attribType);

    public void set(RelationAttributeType attribType, Object value);

    public void setFlags(int flags);

    public int getFlags();

    public Instant getFrom();

    public void setFrom(Instant from);

    public Instant getTo();

    public void setTo(Instant from);

    public Range<Instant> getPeriod();

    public Map<RelationAttributeType, Object> getAttributeMap();

    public Relation execute() throws BusinessException, SQLException;

    public RelationType getRelationType();

    /**
     * Returns true if this transation has no values definied for at least one of its attributes
     *
     * @return true if this transation has no values definied for at least one of its attributes
     */
    public boolean isEmpty();

}
