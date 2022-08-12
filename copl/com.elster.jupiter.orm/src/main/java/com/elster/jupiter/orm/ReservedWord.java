/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import java.util.stream.Stream;

/**
 * List of reserved words that cannot be used for columns, tables.
 * Copied from: <a href="https://docs.oracle.com/cd/B10501_01/appdev.920/a42525/apb.htm">Oracle docs</a>
 */
public enum ReservedWord {
    ACCESS,
    ADD,
    ALL,
    ALTER,
    AND,
    ANY,
    ARRAYLEN,
    AS,
    ASC,
    AUDIT,
    BETWEEN,
    BY,
    CHAR,
    CHECK,
    CLUSTER,
    COLUMN,
    COMMENT,
    COMPRESS,
    CONNECT,
    CREATE,
    CURRENT,
    DATE,
    DECIMAL,
    DEFAULT,
    DELETE,
    DESC,
    DISTINCT,
    DROP,
    ELSE,
    EXCLUSIVE,
    EXISTS,
    FILE,
    FLOAT,
    FOR,
    FROM,
    GRANT,
    GROUP,
    HAVING,
    IDENTIFIED,
    IMMEDIATE,
    IN,
    INCREMENT,
    INDEX,
    INITIAL,
    INSERT,
    INTEGER,
    INTERSECT,
    INTO,
    IS,
    LEVEL,
    LIKE,
    LOCK,
    LONG,
    MAXEXTENTS,
    MINUS,
    MODE,
    MODIFY,
    NOAUDIT,
    NOCOMPRESS,
    NOT,
    NOTFOUND,
    NOWAIT,
    NULL,
    NUMBER,
    OF,
    OFFLINE,
    ON,
    ONLINE,
    OPTION,
    OR,
    ORDER,
    PCTFREE,
    PRIOR,
    PRIVILEGES,
    PUBLIC,
    RAW,
    RENAME,
    RESOURCE,
    REVOKE,
    ROW,
    ROWID,
    ROWLABEL,
    ROWNUM,
    ROWS,
    SELECT,
    SESSION,
    SET,
    SHARE,
    SIZE,
    SMALLINT,
    SQLBUF,
    START,
    SUCCESSFUL,
    SYNONYM,
    SYSDATE,
    TABLE,
    THEN,
    TO,
    TRIGGER,
    UID,
    UNION,
    UNIQUE,
    UPDATE,
    USER,
    VALIDATE,
    VALUES,
    VARCHAR,
    VARCHAR2,
    VIEW,
    WHENEVER,
    WHERE,
    WITH;

    public static boolean isReserved(String aString) {
        return Stream.of(values()).anyMatch(each -> each.match(aString));
    }

    public static boolean isReserved(String aString, boolean isTest) {
        boolean reserved = isReserved(aString);
        return isTest ? reserved && !INCREMENT.match(aString) : reserved; // H2 SEQUENCES table uses column called "INCREMENT"
    }

    private boolean match(String aString) {
        return this.name().equalsIgnoreCase(aString);
    }

}
