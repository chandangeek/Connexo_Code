/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;

public class ProcessVariable {
    public String name;
    public String type;

    ProcessVariable(String name, String type) {
        this.name = name;
        this.type = type;
    }
}
