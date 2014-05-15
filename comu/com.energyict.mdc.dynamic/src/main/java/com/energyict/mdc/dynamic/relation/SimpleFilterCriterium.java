package com.energyict.mdc.dynamic.relation;

import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;

import java.util.Collection;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-14 (17:04)
 */
public class SimpleFilterCriterium implements FilterCriterium, DynamicAttributeOwner {

    public enum Operator {
        EQUALS {
            @Override
            protected void appendTo(SqlBuilder builder) {
                builder.append(" = ");
            }
        },
        DIFFERENT {
            @Override
            protected void appendTo(SqlBuilder builder) {
                builder.append(" <> ");
            }
        },
        LESS_THAN {
            @Override
            protected void appendTo(SqlBuilder builder) {
                builder.append(" < ");
            }
        },
        LESS_THAN_OR_EQUALS {
            @Override
            protected void appendTo(SqlBuilder builder) {
                builder.append(" <= ");
            }
        },
        GREATER_THAN {
            @Override
            protected void appendTo(SqlBuilder builder) {
                builder.append(" > ");
            }
        },
        GREATER_THAN_OR_EQUALS {
            @Override
            protected void appendTo(SqlBuilder builder) {
                builder.append(" >= ");
            }
        },
        LIKE {
            @Override
            protected void appendTo(SqlBuilder builder) {
                builder.append(" LIKE ");
            }
        },
        NOT_LIKE {
            @Override
            protected void appendTo(SqlBuilder builder) {
                builder.append(" NOT LIKE ");
            }
        },
        IS_UNDEFINED {
            @Override
            protected void appendTo(SqlBuilder builder) {
                builder.append(" IS NULL ");
            }
        },
        IS_DEFINED {
            @Override
            protected void appendTo(SqlBuilder builder) {
                builder.append(" IS NOT NULL ");
            }
        },
        IN {
            @Override
            protected void appendTo(SqlBuilder builder) {
                builder.append(" IN ");
            }
        },
        NOT_IN {
            @Override
            protected void appendTo(SqlBuilder builder) {
                builder.append(" NOT IN ");
            }
        };

        protected abstract void appendTo(SqlBuilder sqlBuilder);

    }

    private FilterAspect aspect;
    private Operator operator;
    private Object argument;

    public SimpleFilterCriterium(FilterAspect aspect, Operator operator, Object arg) {
        this.aspect = aspect;
        this.operator = operator;
        this.argument = arg;
    }

    public void appendWhereClause(SqlBuilder builder) {
        if (isInOrNotIn() && (argument instanceof Collection)) {
            builder.append("(");
            appendInOrNotInArgumentsList(builder, (Collection<Object>) argument, aspect.getColumnName());
            builder.append(")");
        }
        else {
            appendAttribute(builder);
            this.operator.appendTo(builder);
            appendArgument(builder);
        }
    }

    private boolean isInOrNotIn() {
        return (Operator.IN.equals(this.operator) || Operator.NOT_IN.equals(this.operator));
    }

    protected void appendAttribute(SqlBuilder builder) {
        builder.append(getModifiedSql(aspect.getColumnName()));
    }

    protected void appendArgument(SqlBuilder builder) {
        if (this.operatorHasArgument()) {
            if (argument instanceof Collection) {
                builder.append(" (");
                appendInOrNotInArgumentsList(builder, (Collection<Object>) argument, aspect.getColumnName());
                builder.append(")");
            }
            else {
                builder.append(" ?");
                builder.bindObject(aspect.valueToDb(argument));
            }
        }
    }

    protected void appendInOrNotInArgumentsList(SqlBuilder builder, Collection<Object> arguments, String columnName) {
        int total = arguments.size();
        int loops = (total / 999) + ((total % 999 == 0) ? 0 : 1);
        int cnt = 0;
        for (int i = 0; i < loops; i++) {
            if (i > 0) {
                if (Operator.IN.equals(this.operator)) {
                    builder.append(" OR ");
                }
                else {
                    builder.append(" AND ");
                }
            }
            builder.append(columnName);
            if (Operator.IN.equals(this.operator)) {
                builder.append(" IN (");
            }
            else {
                builder.append(" NOT IN (");
            }
            for (int j = 0; j < 999; j++) {
                builder.append("?");
                cnt++;
                if (cnt == total || j == 998) {
                    break;
                }
                builder.append(",");
            }
            builder.append(" )");
        }
        for (Object o : arguments) {
            builder.bindObject(aspect.valueToDb(o));
        }
    }

    private boolean operatorHasArgument() {
        return (!Operator.IS_UNDEFINED.equals(this.operator)
                && (!Operator.IS_DEFINED.equals(this.operator)));
    }

    protected String getModifiedSql(String sqlExpression) {
        if (Operator.LIKE.equals(this.operator) || Operator.NOT_LIKE.equals(this.operator)) {
            return " UPPER(" + sqlExpression + ")";
        }
        return sqlExpression;
    }

    public Object get(String key) {
        return argument;
    }

    public void set(String key, Object newValue) {
        this.argument = newValue;
    }

}