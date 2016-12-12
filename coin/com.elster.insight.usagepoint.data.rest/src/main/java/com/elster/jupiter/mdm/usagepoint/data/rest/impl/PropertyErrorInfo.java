package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

class PropertyErrorInfo {
    public String id;
    public String msg;

    PropertyErrorInfo() {
    }

    PropertyErrorInfo(String id, String msg) {
        this.id = id;
        this.msg = msg;
    }
}