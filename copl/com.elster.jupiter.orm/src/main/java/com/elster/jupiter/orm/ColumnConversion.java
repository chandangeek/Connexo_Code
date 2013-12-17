package com.elster.jupiter.orm;
 
public enum ColumnConversion {
	NOCONVERSION,
	NUMBER2INT, 
	NUMBER2INTNULLZERO, 
	NUMBER2LONG,
	NUMBER2LONGNULLZERO,
	CHAR2BOOLEAN,
	NUMBER2BOOLEAN,
	NUMBER2UTCINSTANT,
	NUMBER2NOW,
	NUMBER2ENUM,
	NUMBER2ENUMPLUSONE,
	CHAR2ENUM,
	CHAR2PRINCIPAL,
	NUMBER2INTWRAPPER,
	CHAR2UNIT,
	CHAR2CURRENCY,
    CHAR2FILE,
    CHAR2JSON,
    DATE2DATE,
    TIMESTAMP2DATE
}
