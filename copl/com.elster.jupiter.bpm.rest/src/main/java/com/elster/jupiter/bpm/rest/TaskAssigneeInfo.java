/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;


public class TaskAssigneeInfo {

    private String type;
    private Long id;
    private String name;


    public TaskAssigneeInfo(String type, Long id, String name) {
        this.type = type;
        this.id = id;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    }

