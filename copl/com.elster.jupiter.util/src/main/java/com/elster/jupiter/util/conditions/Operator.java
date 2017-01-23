package com.elster.jupiter.util.conditions;

public enum Operator {
    EQUAL("="),
    NOTEQUAL("!="),
    GREATERTHAN(">"),
    LESSTHAN("<"),
    GREATERTHANOREQUAL(">="),
    LESSTHANOREQUAL("<="),
    LIKE("LIKE") {
        @Override
        public String getFormat() {
            return "{0} LIKE ? ESCAPE ''\\''";
        }
    },
    REGEXP_LIKE("REGEXP_LIKE") {
        @Override
        public String getFormat() {
            return getSymbol() + "({0},?,?)";
        }
    },
    ISNULL("IS NULL") {
        @Override
        public String getFormat() {
            return "{0} " + getSymbol();
        }
    },
    ISNOTNULL("IS NOT NULL") {
        @Override
        public String getFormat() {
            return "{0} " + getSymbol();
        }
    },
    BETWEEN("BETWEEN") {
        @Override
        public String getFormat() {
            return "{0} BETWEEN ? AND ?";
        }
    },
    EQUALORBOTHNULL("") {
        @Override
        public String getFormat() {
            return " DECODE({0} , ? , 1 , 0 ) = 1";
        }
    },
    NOTEQUALANDNOTBOTHNULL("") {
        @Override
        public String getFormat() {
            return " DECODE({0} , ? , 1 , 0 ) = 0";
        }
    },
    EQUALIGNORECASE("") {
        @Override
        public String getFormat() {
            return "UPPER({0}) = UPPER(?)";
        }
    },
    LIKEIGNORECASE("") {
        @Override
        public String getFormat() {
            return "''a'' || UPPER(nvl({0}, '''')) LIKE ''a'' || UPPER(?) ESCAPE ''\\''";  // prepending 'a' to make sure % also matches empty strings, which in oracle are null
        }
    },
    SOUNDSAS("") {
        @Override
        public String getFormat() {
            return "SOUNDEX({0}) = SOUNDEX(?)";
        }
    };

    private final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getFormat() {
        return "{0} " + getSymbol() + " ? ";
    }

    public Comparison compare(String fieldName, Object... values) {
        return new Comparison(fieldName, this, values);
    }

    public static Comparison isTrue(String fieldName) {
        return Operator.EQUAL.compare(fieldName, true);
    }

    public static Comparison isFalse(String fieldName) {
        return Operator.EQUAL.compare(fieldName, false);
    }

    public static Comparison isNull(String fieldName) {
        return Operator.ISNULL.compare(fieldName);
    }

    public static Comparison isNotNull(String fieldName) {
        return Operator.ISNOTNULL.compare(fieldName);
    }
}
