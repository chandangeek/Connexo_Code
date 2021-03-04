/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.conditions;

import java.util.Objects;

public class Hint {

    private final HintType hintType;
    private final String tableName;

    public Hint(HintType hintType, String tableName) {
        this.hintType = hintType;
        this.tableName = tableName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Hint hint = (Hint) o;
        return hintType == hint.hintType && tableName.equals(hint.tableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hintType, tableName);
    }

    public String toSqlString() {
        return hintType.hintName + "(" + tableName + ")";
    }

    @Override
    public String toString() {
        return toSqlString();
    }

    public enum HintType {

        INDEX("INDEX"),
        LEADING("LEADING"),
        USE_NL("USE_NL");

        private final String hintName;

        HintType(String hintName) {
            this.hintName = hintName;
        }
    }
}