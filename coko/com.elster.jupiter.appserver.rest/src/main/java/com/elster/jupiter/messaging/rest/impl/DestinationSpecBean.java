package com.elster.jupiter.messaging.rest.impl;

import com.elster.jupiter.orm.ReservedWord;

class DestinationSpecBean {

    private static String CHARACTERS_ONLY = "[a-zA-Z]+";

    private String name;
    private String queueTypeName;

    DestinationSpecBean(String name, String queueTypeName) {
        this.name = name;
        this.queueTypeName = queueTypeName;
    }

    String getName() {
        return name.trim().toUpperCase();
    }

    String getQueueTypeName() {
        return queueTypeName.trim();
    }

    boolean isWrongNameDefined() {
        return name == null || name.trim().isEmpty() || !name.matches(CHARACTERS_ONLY);
    }

    boolean isWrongQueueTypeNameDefined() {
        return queueTypeName == null || queueTypeName.trim().isEmpty() || !queueTypeName.matches(CHARACTERS_ONLY);
    }

    boolean isReserved() {
        return ReservedWord.isReserved(name);
    }

}
