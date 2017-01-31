/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class RunningProcessInfo {

    public long status;
    public long processInstanceId;
    public String processId;
    public String processName;
    public String processVersion;
    public String userIdentity;
    public Date startDate;
    public List<TaskSummary> tasks;

    public RunningProcessInfo(Object[] obj){
        status = obj[0] == null ? -1 :((BigDecimal) obj[0]).longValue();
        processId = obj[1] == null ? "" : (String) obj[1];
        processName = obj[2] == null ? "" :(String) obj[2];
        processVersion = obj[3] == null ? "" : (String) obj[3];
        userIdentity = obj[4] == null ? "" :(String) obj[4];
        startDate = obj[5] == null ? null : (Timestamp) obj[5];
        processInstanceId = obj[6] == null ? -1 :((BigDecimal) obj[6]).longValue();
    }
     public RunningProcessInfo(){

     }
}
