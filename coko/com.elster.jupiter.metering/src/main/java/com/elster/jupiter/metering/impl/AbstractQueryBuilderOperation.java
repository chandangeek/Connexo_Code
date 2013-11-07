package com.elster.jupiter.metering.impl;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractQueryBuilderOperation implements QueryBuilderOperation {

    // ORM inheritance map
    static final Map<String, Class<? extends QueryBuilderOperation>> IMPLEMENTERS = initImplementors();

    private static Map<String, Class<? extends QueryBuilderOperation>> initImplementors() {
        Map<String, Class<? extends QueryBuilderOperation>> map = new HashMap<>();

        map.put(OpenBracketOperation.TYPE_IDENTIFIER, OpenBracketOperation.class);
        map.put(CloseBracketOperation.TYPE_IDENTIFIER, CloseBracketOperation.class);
        map.put(NotOperation.TYPE_IDENTIFIER, NotOperation.class);
        map.put(AndOperation.TYPE_IDENTIFIER, AndOperation.class);
        map.put(OrOperation.TYPE_IDENTIFIER, OrOperation.class);
        map.put(SimpleConditionOperation.TYPE_IDENTIFIER, SimpleConditionOperation.class);

        return map;
    }


    private int position;

    @Override
    public void setPosition(int i) {
        this.position = i;
    }

}
