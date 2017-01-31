/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;


import java.util.Map;

public class TaskOutputContentInfo {

    public Map<String, Object> outputTaskContent;

    public TaskOutputContentInfo(){
    }

    public TaskOutputContentInfo(Map<String, Object> outputTaskContent){
        this.outputTaskContent = outputTaskContent;
    }
}
