/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.units.Dimension;

/**
 * Models a {@link ServerExpressionNode} as a wrapper for a {@link SqlFragment}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-04 (13:29)
 */
class SqlFragmentNode implements ServerExpressionNode {

    private final SqlFragment sqlFragment;

    SqlFragmentNode(String name) {
        this(new TextFragment(name));
    }

    SqlFragmentNode(SqlFragment sqlFragment) {
        super();
        this.sqlFragment = sqlFragment;
    }

    SqlFragment getSqlFragment() {
        return sqlFragment;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitSqlFragment(this);
    }

    @Override
    public IntermediateDimension getIntermediateDimension() {
        return IntermediateDimension.of(Dimension.DIMENSIONLESS);
    }

}