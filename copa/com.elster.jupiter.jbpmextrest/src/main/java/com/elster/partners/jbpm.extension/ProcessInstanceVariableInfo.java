package com.elster.partners.jbpm.extension;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class ProcessInstanceVariableInfo {

    public String variableName;
    public String value;
    public String oldValue;
    public Date logDate;
    public String nodeInstanceId = "";

    public ProcessInstanceVariableInfo(Object[] obj){
        this.variableName = obj[7] == null ? "" : (String) obj[7];
        this.logDate = obj[1] == null ? null : (Timestamp) obj[1];
        this.value = obj[6] == null ? "" : (String) obj[6];
        this.oldValue = obj[3] == null ? "" : (String) obj[3];
    }

    public ProcessInstanceVariableInfo(Object[] obj, List<ProcessInstanceNodeInfo> nodes){
        this.variableName = obj[7] == null ? "" : (String) obj[7];
        this.logDate = obj[1] == null ? null : (Timestamp) obj[1];
        this.value = obj[6] == null ? "" : (String) obj[6];
        this.oldValue = obj[3] == null ? "" : (String) obj[3];
        if(logDate != null) {
            for (int i=0; i< nodes.size();i++) {
                if(logDate.after(nodes.get(i).logDate)) {
                    this.nodeInstanceId = nodes.get(i).nodeInstanceId;
                    i = nodes.size();
                }
            }
            if(nodeInstanceId.equals("")){
                nodeInstanceId = "0";
            }
        }
    }

    private boolean isWithinRange(Date testDate, Date startDate, Date endDate) {
        return !(testDate.before(startDate) || testDate.after(endDate));
    }

}
