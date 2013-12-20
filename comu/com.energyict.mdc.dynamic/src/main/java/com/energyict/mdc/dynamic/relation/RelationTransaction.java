package com.energyict.mdc.dynamic.relation;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.dynamic.VersionedDynamicAttributeOwner;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

/**
 * <code>Transaction</code> wrapping a <code>Relation</code>
 *
 * @see Transaction
 */
public interface RelationTransaction extends Transaction<Relation>, VersionedDynamicAttributeOwner {

    public Object get(RelationAttributeType attribType);

    public void set(RelationAttributeType attribType, Object value);

    public void setFlags(int flags);

    public int getFlags();

    public Date getFrom();

    public void setFrom(Date from);

    public Date getTo();

    public void setTo(Date from);

    public Interval getPeriod();

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
