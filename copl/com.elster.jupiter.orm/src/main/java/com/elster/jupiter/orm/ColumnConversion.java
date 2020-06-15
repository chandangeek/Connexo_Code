/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

/*
 * Defines conversions between a database column and a Java field
 */
public enum ColumnConversion {
    NOCONVERSION,
    NUMBER2INT,
    NUMBER2INTNULLZERO,
    NUMBER2LONG,
    NUMBER2LONGNULLZERO,
    CHAR2BOOLEAN,
    NUMBER2BOOLEAN,
    NUMBER2NOW,
    NUMBER2ENUM,
    NUMBER2ENUMPLUSONE,
    CHAR2ENUM,
    CHAR2PRINCIPAL,
    NUMBER2INTWRAPPER,
    NUMBER2LONGWRAPPER,
    CHAR2UNIT,
    CHAR2CURRENCY,
    CHAR2FILE,
    CHAR2JSON,
    DATE2INSTANT,
    TIMESTAMP2INSTANT,
    CLOB2STRING,
    BLOB2BYTE,
    NUMBERINUTCSECONDS2INSTANT,
    NUMBER2INSTANT,
    CHAR2PATH,
    SDOGEOMETRY2SPATIALGEOOBJ,
    BLOB2SQLBLOB;

    public static final int CATALOGNAMELIMIT = 30;

}
