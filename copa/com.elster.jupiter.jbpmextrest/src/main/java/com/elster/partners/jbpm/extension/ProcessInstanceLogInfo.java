/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class ProcessInstanceLogInfo {

    public String processId;
    public String processName;
    public String externalId;
    public String processVersion;
    public String userIdentity;
    public String outcome;
    public String processInstanceDescription;
    public long duration;
    public long parentProcessInstanceId;
    public long status;
    public long processInstanceId;
    public Date startDate;
    public Date endDate;

    public ProcessInstanceLogInfo(Object[] obj){

        processId = obj[0] == null ? "" : (String) obj[0];
        processName = obj[1] == null ? "" :(String) obj[1];
        externalId = obj[2] == null ? "" :(String) obj[2];
        processVersion = obj[3] == null ? "" : (String) obj[3];
        userIdentity = obj[4] == null ? "" :(String) obj[4];
        outcome = obj[5] == null ? "" :(String) obj[5];
        processInstanceDescription = obj[6] == null ? "" :(String) obj[6];
        duration = obj[7] == null ? -1 : ((BigDecimal) obj[7]).longValue();
        parentProcessInstanceId = obj[8] == null ? -1 : ((BigDecimal) obj[8]).longValue();
        status = obj[9] == null ? -1 :((BigDecimal) obj[9]).longValue();
        processInstanceId = obj[10] == null ? -1 :((BigDecimal) obj[10]).longValue();
        startDate = obj[11] == null ? null : (Timestamp) obj[11];
        endDate = obj[12] == null ? null : (Timestamp) obj[12];
    }
}
