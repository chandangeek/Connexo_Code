package com.elster.jupiter.util;

public class Checks {

    public static ObjectChecker<Object> is(Object object) {
        return new ObjectChecker<>(object);
    }

    public static StringChecker is(String s) {
        return new StringChecker(s);
    }

}
