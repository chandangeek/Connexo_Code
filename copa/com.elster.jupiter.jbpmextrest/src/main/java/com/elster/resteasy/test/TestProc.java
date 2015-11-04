package com.elster.resteasy.test;

import java.math.BigDecimal;
import java.util.List;


public class TestProc {

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


//    *******
//    public String startDate;
//    public String endDate;
//    public String logDate;
//    *******

    public TestProc(List<Object[]> list){
        status = list.get(0)[0] == null ? -1 :((BigDecimal) list.get(0)[0]).longValue();
        processInstanceId = list.get(0)[1] == null ? -1 :((BigDecimal) list.get(0)[1]).longValue();
        processId = list.get(0)[2] == null ? "" : (String) list.get(0)[2];
        processName = list.get(0)[3] == null ? "" :(String) list.get(0)[3];
        externalId = list.get(0)[4] == null ? "" :(String) list.get(0)[4];
        processVersion = list.get(0)[5] == null ? "" : (String) list.get(0)[5];
        userIdentity = list.get(0)[6] == null ? "" :(String) list.get(0)[6];
//        startDate = (String) list.get(0)[7];
//        endDate = (String) list.get(0)[8];
        duration = list.get(0)[9] == null ? -1 : ((BigDecimal) list.get(0)[9]).longValue();
        parentProcessInstanceId = list.get(0)[10] == null ? -1 : ((BigDecimal) list.get(0)[10]).longValue();
        processInstanceIdFromVariableLog = list.get(0)[11] == null ? -1 : ((BigDecimal) list.get(0)[11]).longValue();
//        logDate = (String) list.get(0)[12];
        variableIdFromVariableLog = list.get(0)[13] == null ? "" :(String) list.get(0)[13];
        oldValueFromVariableLog = list.get(0)[14] == null ? "" :(String) list.get(0)[14];


    }
}
