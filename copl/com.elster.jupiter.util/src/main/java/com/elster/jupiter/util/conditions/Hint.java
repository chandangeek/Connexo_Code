/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.conditions;

public class Hint {

    private final HintName hintName;
    private final String tableAlias;

    public Hint(HintName hintName, String tableAlias) {
        this.hintName = hintName;
        this.tableAlias = tableAlias;
    }

    public String toSqlString() {
        return hintName.hintName + "(" + tableAlias + ")";
    }

    public enum HintName {

        INDEX("INDEX"),
        LEADING("LEADING"),
        USE_NL("USE_NL");

        private final String hintName;

        HintName(String hintName) {
            this.hintName = hintName;
        }
    }
}
