/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;


import java.util.ArrayList;
import java.util.List;

public class TopTasksPayload {

    public String userName;
    public List<String> workGroups = new ArrayList<>();
    public ProcessDefinitionInfos processDefinitionInfos;

    public TopTasksPayload() {

    }

}
