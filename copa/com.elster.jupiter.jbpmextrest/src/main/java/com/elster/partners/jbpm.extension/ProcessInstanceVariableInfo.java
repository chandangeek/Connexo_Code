/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class ProcessInstanceVariableInfo {

    @JsonIgnore
    static final String passwordVariableName = "password";

    public String variableName;
    public String value;
    public String oldValue;
    public Date logDate;
    public long nodeInstanceId = -1;

    public ProcessInstanceVariableInfo(){

    }

    public ProcessInstanceVariableInfo(Object[] obj){
        this.variableName = obj[7] == null ? "" : (String) obj[7];
        this.logDate = obj[1] == null ? null : (Timestamp) obj[1];
        this.value = obj[6] == null ? "" :
                this.variableName.toLowerCase().contains(this.passwordVariableName)
                        ? ""
                        :(String) obj[6];
        this.oldValue = obj[3] == null ? "" : (String) obj[3];
    }

    public ProcessInstanceVariableInfo(Object[] obj, List<ProcessInstanceNodeInfo> nodes){
        this.variableName = obj[7] == null ? "" : (String) obj[7];
        this.logDate = obj[1] == null ? null : (Timestamp) obj[1];
        this.value = obj[6] == null ? "" :
                this.variableName.toLowerCase().contains(this.passwordVariableName)
                        ? ""
                        :(String) obj[6];
        this.oldValue = obj[3] == null ? "" : (String) obj[3];
        if(logDate != null) {
            for (int i=0; i< nodes.size();i++) {
                if(logDate.getTime() >= nodes.get(i).logDate.getTime()) {
                    this.nodeInstanceId = nodes.get(i).nodeInstanceId;
                    i = nodes.size();
                }
            }
            if(nodeInstanceId < 0){
                nodeInstanceId = nodes.get(nodes.size() - 1).nodeInstanceId;
            }
        }
    }

    private boolean isWithinRange(Date testDate, Date startDate, Date endDate) {
        return !(testDate.before(startDate) || testDate.after(endDate));
    }

}
