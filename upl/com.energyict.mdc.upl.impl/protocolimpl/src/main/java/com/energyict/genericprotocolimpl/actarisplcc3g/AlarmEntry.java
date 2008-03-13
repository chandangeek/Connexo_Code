/*
 * AlarmEntry.java
 *
 * Created on 10 januari 2008, 9:51
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g;

import java.util.*;

/**
 *
 * @author kvds
 */
public class AlarmEntry {
    
    private final int ALARM_TAMPER = 1;
    private final int ALARM_END_OF_DISCOVER = 2;
    private final int ALARM_END_OF_LOAD_PROFILE_REQUEST = 3;
    private final int ALARM_NEW_METER_FOUND = 4;
    private final int ALARM_BEGIN_OF_DISCOVER = 5;
    
    String[] alarmDescriptions = new String[]{"","ALARM_TAMPER","ALARM_END_OF_DISCOVER","ALARM_END_OF_LOAD_PROFILE_REQUEST","ALARM_NEW_METER_FOUND","ALARM_BEGIN_OF_DISCOVER"}; 
    
    private Date datetime;
    private String serialNumber;
    private int id;
    
    /** Creates a new instance of AlarmEntry */
    public AlarmEntry(Date datetime,String serialNumber,int id) {
        this.datetime=datetime;
        this.serialNumber=serialNumber;
        this.id=id;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("AlarmEntry:\n");
        strBuff.append("   datetime="+getDatetime()+"\n");
        strBuff.append("   serialNumber="+getSerialNumber()+"\n");
        strBuff.append("   id="+getId()+"\n");
        strBuff.append("   alarm="+getAlarmDescription()+"\n");
        return strBuff.toString();
    }        
    
    public String getAlarmDescription() {
        if ((id>0) && (id < alarmDescriptions.length)) {
            return alarmDescriptions[id];
        }
        else return "Unknown alarm!";
    }
    
    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isALARM_TAMPER() {
        return getId() == ALARM_TAMPER;
    }

    public boolean isALARM_END_OF_DISCOVER() {
        return getId() == ALARM_END_OF_DISCOVER;
    }

    public boolean isALARM_END_OF_LOAD_PROFILE_REQUEST() {
        return getId() == ALARM_END_OF_LOAD_PROFILE_REQUEST;
    }

    public boolean isALARM_BEGIN_OF_DISCOVER() {
        return getId() == ALARM_BEGIN_OF_DISCOVER;
    }

    public boolean isALARM_NEW_METER_FOUND() {
        return getId() == ALARM_NEW_METER_FOUND;
    }
    
    public boolean isUnknownAlarm() {
        return !(isALARM_TAMPER() || 
                isALARM_END_OF_DISCOVER() || 
                isALARM_END_OF_LOAD_PROFILE_REQUEST() ||
                isALARM_BEGIN_OF_DISCOVER() || 
                isALARM_NEW_METER_FOUND());
    }
    
}
