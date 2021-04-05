/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.conditions;

import com.elster.jupiter.util.MessageSeeds;
import com.elster.jupiter.util.exception.NoFieldSpecifiedException;
import com.elster.jupiter.util.exception.SqlInjectionException;
import com.elster.jupiter.util.sql.SqlFragment;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public final class Order {

    private static final Pattern SQL_I = Pattern.compile("\\s+|--|;");
    private static final String COMMAND = "Order";

    public static final Order[] NOORDER = new Order[0];
    private String rawSql;
    private String name;
    private final boolean ascending;
    private String function;
    private NullStrategy nullStrategy = NullStrategy.NONE;

    private Order(String name, boolean ascending) {
        String trimmedColumnName = name.trim();
        if (trimmedColumnName.isEmpty()) {
            throw new NoFieldSpecifiedException(COMMAND);
        }

        if (SQL_I.matcher(trimmedColumnName).find()) {
            //if (trimmedColumnName.contains(" ") || trimmedColumnName.contains("'")||trimmedColumnName.contains(";")) {
            throw new SqlInjectionException(MessageSeeds.POSSIBLE_SQL_INJECTION_ORDER_FIELD, name);
        }
        this.name = name;
        this.ascending = ascending;
    }

    public boolean ascending() {
        return ascending;
    }

    public String getName() {
        return name;
    }

    public String ordering() {
        return ascending ? "ASC" : "DESC";
    }

    public Order apply(String name) {
        this.function = name;
        return this;
    }

    public Order applySqlString(String sql){
        this.rawSql = sql;
        return this;
    }

    public Order toUpperCase() {
        return apply("upper");
    }

    public Order toLowerCase() {
        return apply("lower");
    }

    public String getClause(String resolvedField) {
        return getBaseClause(resolvedField) + nullStrategy.getClause();
    }

    private String getBaseClause(String resolvedField) {
        if (rawSql != null) {
            return rawSql.replaceAll(name, resolvedField) + " " + ordering();
        }
        else if (function != null) {
            return function + "(" + resolvedField + ")" + " " + ordering();
        } else {
            return resolvedField + " " + ordering();
        }
    }

    public Order nullsFirst() {
        this.nullStrategy = NullStrategy.NULLSFIRST;
        return this;
    }

    public Order nullsLast() {
        this.nullStrategy = NullStrategy.NULLSLAST;
        return this;
    }

    public Order wrap(String containingField) {
        name = containingField + '.' + name;
        return this;
    }

    public static Order ascending(String name) {
        return new Order(Objects.requireNonNull(name), true);
    }

    public static Order descending(String name) {
        return new Order(Objects.requireNonNull(name), false);
    }

    @Deprecated
    public static Order[] from(String[] orderBy) {
        if (orderBy == null) {
            return new Order[0];
        }
        Order[] result = new Order[orderBy.length];
        for (int i = 0; i < orderBy.length; i++) {
            result[i] = Order.ascending(orderBy[i]);
        }
        return result;
    }

    @Deprecated
    public static Order[] from(String order, String[] orders) {
        Order[] result = new Order[orders.length + 1];
        result[0] = Order.ascending(order);
        for (int i = 0; i < orders.length; i++) {
            result[i + 1] = Order.ascending(orders[i]);
        }
        return result;
    }

    private enum NullStrategy {
        NONE(""),
        NULLSFIRST("NULLS FIRST"),
        NULLSLAST("NULLS LAST");

        private final String clause;

        NullStrategy(String clause) {
            this.clause = clause;
        }

        String getClause() {
            return " " + clause + " ";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Order)) {
            return false;
        }

        Order order = (Order) o;

        if (ascending != order.ascending) {
            return false;
        }
        if (name != null ? !name.equals(order.name) : order.name != null) {
            return false;
        }
        if (function != null ? !function.equals(order.function) : order.function != null) {
            return false;
        }
        return nullStrategy == order.nullStrategy;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (ascending ? 1 : 0);
        result = 31 * result + (function != null ? function.hashCode() : 0);
        result = 31 * result + (nullStrategy != null ? nullStrategy.hashCode() : 0);
        return result;
    }
}
