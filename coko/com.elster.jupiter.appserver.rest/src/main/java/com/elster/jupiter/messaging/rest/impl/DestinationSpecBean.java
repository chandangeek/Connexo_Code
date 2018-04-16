package com.elster.jupiter.messaging.rest.impl;

class DestinationSpecBean {

    private String name;
    private String queueTypeName;

    DestinationSpecBean(String name, String queueTypeName) {
        this.name = name;
        this.queueTypeName = queueTypeName;
    }

    String getName() {
        String value = name.trim();
        value = value.replace(" ", "_");
        return value;
    }

    String getQueueTypeName() {
        return queueTypeName.trim();
    }

    boolean isWrongNameDefined() {
        return name == null || name.trim().isEmpty();
    }

    boolean isWrongQueueTypeNameDefined() {
        return queueTypeName == null || queueTypeName.trim().isEmpty();
    }

}
