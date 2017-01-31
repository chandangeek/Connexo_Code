/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class ProcessInstanceInfo {

    public String processId;
    public String processName;
    public String externalId;
    public String processVersion;
    public String userIdentity;
    public String variableIdFromVariableLog;
    public String oldValueFromVariableLog;
    public long duration;
    public long parentProcessInstanceId;
    public long status;
    public long processInstanceId;
    public long processInstanceIdFromVariableLog;
    public Date startDate;
    public Date endDate;
    public Date logDate;

    public ProcessInstanceInfo(Object[] obj){
        status = obj[0] == null ? -1 :((BigDecimal) obj[0]).longValue();
        processInstanceId = obj[1] == null ? -1 :((BigDecimal) obj[1]).longValue();
        processId = obj[2] == null ? "" : (String) obj[2];
        processName = obj[3] == null ? "" :(String) obj[3];
        externalId = obj[4] == null ? "" :(String) obj[4];
        processVersion = obj[5] == null ? "" : (String) obj[5];
        userIdentity = obj[6] == null ? "" :(String) obj[6];
        startDate = obj[7] == null ? null : (Timestamp) obj[7];
        endDate = obj[8] == null ? null : (Timestamp) obj[8];
        duration = obj[9] == null ? -1 : ((BigDecimal) obj[9]).longValue();
        parentProcessInstanceId = obj[10] == null ? -1 : ((BigDecimal) obj[10]).longValue();
        processInstanceIdFromVariableLog = obj[11] == null ? -1 : ((BigDecimal) obj[11]).longValue();
        logDate = obj[12] == null ? null : (Timestamp) obj[12];
        variableIdFromVariableLog = obj[13] == null ? "" :(String) obj[13];
        oldValueFromVariableLog = obj[14] == null ? "" :(String) obj[14];
    }
}
