package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import java.util.List;

/**
 * Models the supported functions that can be used in {@link com.elster.jupiter.metering.config.Formula}'s
 * of {@link com.elster.jupiter.metering.config.ReadingTypeDeliverable}s.
 *
 * @author Isabelle Gheysens (igh)
 * @since 2016-02-08
 */
public enum Function {
    SUM(1),
    MAX(2),
    MIN(3),
    AVG(4);

    private final int id;

    Function(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void appendTo(SqlBuilder sqlBuilder, List<SqlFragment> arguments) {
        // All currently known functions support only 1 argument
        if (arguments.size() != 1) {
            throw new IllegalArgumentException(this.name() + " takes exactly 1 argument but got " + arguments.size());
        }
        sqlBuilder.append(this.name());
        sqlBuilder.append("(");
        sqlBuilder.add(arguments.get(0));
        sqlBuilder.append(")");
    }

}
