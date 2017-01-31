/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;


import com.elster.jupiter.bpm.UserTaskInfo;

import java.util.List;

public class TopTaskInfo {

    public long totalUserAssigned;
    public long totalWorkGroupAssigned;
    public long total;
    public List<UserTaskInfo> items;

    public TopTaskInfo(){

    }

}
