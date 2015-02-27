package com.energyict.mdc.dynamic.relation.impl;

/**
* Models the reserved words on the underlying database.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2015-02-27 (14:27)
*/
enum DatabaseReservedWord {
    SHARE,
    RAW,
    DROP,
    BETWEEN,
    FROM,
    DESC,
    OPTION,
    PRIOR,
    LONG,
    THEN,
    DEFAULT,
    ALTER,
    IS,
    INTO,
    MINUS,
    INTEGER,
    NUMBER,
    GRANT,
    IDENTIFIED,
    ALL,
    TO,
    ORDER,
    ON,
    FLOAT,
    DATE,
    HAVING,
    CLUSTER,
    NOWAIT,
    RESOURCE,
    ANY,
    TABLE,
    INDEX,
    FOR,
    UPDATE,
    WHERE,
    CHECK,
    SMALLINT,
    WITH,
    DELETE,
    BY,
    ASC,
    REVOKE,
    LIKE,
    SIZE,
    RENAME,
    NOCOMPRESS,
    NULL,
    GROUP,
    VALUES,
    AS,
    IN,
    VIEW,
    EXCLUSIVE,
    COMPRESS,
    SYNONYM,
    SELECT,
    INSERT,
    EXISTS,
    NOT,
    TRIGGER,
    ELSE,
    CREATE,
    INTERSECT,
    PCTFREE,
    DISTINCT,
    CONNECT,
    SET,
    MODE,
    OF,
    UNIQUE,
    VARCHAR2,
    VARCHAR,
    LOCK,
    OR,
    CHAR,
    DECIMAL,
    UNION,
    PUBLIC,
    AND,
    START;

    public static boolean isReservedWord(String name) {
        for (DatabaseReservedWord reservedWord : DatabaseReservedWord.values()) {
            if (reservedWord.toString().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

}