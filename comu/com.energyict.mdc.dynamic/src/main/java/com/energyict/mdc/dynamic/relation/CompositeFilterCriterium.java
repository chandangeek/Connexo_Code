package com.energyict.mdc.dynamic.relation;

import com.energyict.mdc.common.SqlBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-14 (17:24)
 */
public class CompositeFilterCriterium implements FilterCriterium {

    private enum Operator {
        AND {
            @Override
            protected void appendTo(SqlBuilder builder) {
                builder.append(" AND ");
            }
        },

        OR {
            @Override
            protected void appendTo(SqlBuilder builder) {
                builder.append(" OR ");
            }
        };

        protected abstract void appendTo(SqlBuilder builder);
    }

    private Operator operator;
    private List children;

    public static CompositeFilterCriterium matchAll (FilterCriterium... criteria) {
        CompositeFilterCriterium composite = new CompositeFilterCriterium(Operator.AND);
        for (FilterCriterium each : criteria) {
            composite.add(each);
        }
        return composite;
    }

    public static CompositeFilterCriterium matchAtLeastOne (FilterCriterium... criteria) {
        CompositeFilterCriterium composite = new CompositeFilterCriterium(Operator.OR);
        for (FilterCriterium each : criteria) {
            composite.add(each);
        }
        return composite;
    }

    private CompositeFilterCriterium(Operator operator) {
        this.operator = operator;
        this.children = new ArrayList();
    }

    public void appendWhereClause(SqlBuilder builder) {
        if (!children.isEmpty()) {
            builder.append(" (");
            Iterator it = children.iterator();
            boolean first = true;
            while (it.hasNext()) {
                if (first) {
                    first = false;
                } else {
                    this.operator.appendTo(builder);
                }
                builder.append(" (");
                FilterCriterium entry = (FilterCriterium) it.next();
                entry.appendWhereClause(builder);
                builder.append(")");
            }
            builder.append(")");
        }
    }

    public void add(FilterCriterium filter) {
        children.add(filter);
    }

}