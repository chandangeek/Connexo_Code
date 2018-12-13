/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class ProcessInstanceNodeInfo {

    public String nodeName;
    public String nodeType;
    public String type;
    public String nodeId;
    public Date logDate;
    public long nodeInstanceId;

    public ProcessInstanceNodeInfo(){

    }

    public ProcessInstanceNodeInfo(Object[] obj, String processInstanceStatus){
        this.nodeName = obj[0] == null ? "" :(String) obj[0];
        this.nodeType = obj[1] == null ? "" :(String) obj[1];
        long typeValue = obj[7] == null ? -1 :((BigDecimal) obj[7]).longValue();
        if(typeValue == 0 && !nodeType.equals("FaultNode")){
            if(processInstanceStatus.toLowerCase().equals("active")){
                this.type = "ACTIVE";
            }else if(processInstanceStatus.toLowerCase().equals("completed")){
                this.type = "COMPLETED";
            }else{
                this.type = "ABORTED";
            }
        }else{
            this.type = "COMPLETED";
        }
        this.logDate = obj[2] == null ? null : (Timestamp) obj[2];
        this.nodeInstanceId = obj[5] == null ? 0 :((BigDecimal) obj[5]).longValue();
        this.nodeId = obj[4] == null ? "" :(String) obj[4];
    }


}
