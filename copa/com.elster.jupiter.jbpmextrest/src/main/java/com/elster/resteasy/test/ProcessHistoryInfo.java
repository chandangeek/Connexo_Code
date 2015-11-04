package com.elster.resteasy.test;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class ProcessHistoryInfo {

    public long status;
    public long duration;
    public long processInstanceId;
    public String processName;
    public String processVersion;
    public String userIdentity;
    public Date startDate;
    public Date endDate;

    public ProcessHistoryInfo(Object[] obj){
        status = obj[0] == null ? -1 :((BigDecimal) obj[0]).longValue();
        processInstanceId = obj[1] == null ? -1 :((BigDecimal) obj[1]).longValue();
        processName = obj[2] == null ? "" :(String) obj[2];
        processVersion = obj[3] == null ? "" : (String) obj[3];
        userIdentity = obj[4] == null ? "" :(String) obj[4];
        startDate = obj[5] == null ? null : (Timestamp) obj[5];
        endDate = obj[6] == null ? null : (Timestamp) obj[6];
        duration = obj[7] == null ? -1 : ((BigDecimal) obj[7]).longValue();
    }

}
