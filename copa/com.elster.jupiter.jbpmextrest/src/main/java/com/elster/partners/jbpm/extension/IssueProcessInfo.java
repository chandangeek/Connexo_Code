/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import java.util.List;

public class IssueProcessInfo {

    public long processId;
    public String name;
    public Date  startDate;
    public long status;
    public String startedBy;
    public String associatedTo;
    public String version;
    public List<TaskSummary> openTasks;

    public IssueProcessInfo(){

    }

    public IssueProcessInfo(Object[] obj) {
            this.name = obj[0] == null ? "" :(String) obj[0];
            this.startDate = obj[1] == null ? null : (Timestamp) obj[1];
            this.version =  obj[2] == null ? "" : (String) obj[2];
            this.startedBy = obj[3] == null ? "" :(String) obj[3];
            this.processId = obj[4] == null ? -1 :((BigDecimal) obj[4]).longValue();
            this.status = obj[5] == null ? -1 :((BigDecimal) obj[5]).longValue();
            //TaskSummaryList taskInfos = new TaskSummaryList(jsonObject.getJSONArray("tasks"));
            //this.openTasks = taskInfos.tasks;
    }
}
