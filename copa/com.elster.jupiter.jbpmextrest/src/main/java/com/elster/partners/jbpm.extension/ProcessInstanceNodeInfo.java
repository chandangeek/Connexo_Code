package com.elster.partners.jbpm.extension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class ProcessInstanceNodeInfo {

    public String nodeName;
    public String nodeType;
    public String type;
    public Date logDate;
    public String nodeInstanceId;

    public ProcessInstanceNodeInfo(Object[] obj, String processInstanceStatus){
        long typeValue;
        this.nodeName = obj[0] == null ? "" :(String) obj[0];
        this.nodeType = obj[1] == null ? "" :(String) obj[1];
        typeValue = obj[2] == null ? -1 :((BigDecimal) obj[2]).longValue();
        if(typeValue == 0){
            this.type = processInstanceStatus.toLowerCase().equals("active") ?"ACTIVE":"ABORTED";
        }else{
            this.type = "COMPLETED";
        }
        this.logDate = obj[3] == null ? null : (Timestamp) obj[3];
        this.nodeInstanceId = obj[4] == null ? "" :(String) obj[4];
    }


}
