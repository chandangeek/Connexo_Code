package com.elster.jupiter.webservices.rest.impl;

public class RelatedObjectInfo {
    public long id;
    public String value;
    public String key;
    public String keyDisplayValue;//Translation

    public RelatedObjectInfo(long id, String value, String key, String keyDisplayValue){
        this.id = id;
        this.value = value;
        this.key = key;
        this.keyDisplayValue = keyDisplayValue;
    }
}
