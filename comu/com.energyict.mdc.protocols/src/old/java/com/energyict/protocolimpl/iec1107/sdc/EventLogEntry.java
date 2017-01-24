/*
 * EventLogEntry.java
 *
 * Created on 3 november 2004, 9:59
 */

package com.energyict.protocolimpl.iec1107.sdc;


import java.util.Date;


/**
 *
 * @author  Koen
 */
public class EventLogEntry {

    Date date;
    int id;
    int type;
    int[] infos={-1,-1,-1,-1};

    public EventLogEntry(int id,int type,Date date) {
        this(id,type,null,date);
    }
    /** Creates a new instance of EventLogEntry */
    public EventLogEntry(int id,int type,int[] infos,Date date) {
        this.id=id;
        this.type=type;
        if (infos != null)
           for (int i=0 ; i< infos.length; i++)
               this.infos[i]=infos[i];
        this.date=date;
    }

    public void addInfo(int index, int info) {
        infos[index] = info;
    }

    public void addInfo1(int info) {
        infos[0] = info;
    }
    public void addInfo2(int info) {
        infos[1] = info;
    }
    public void addInfo3(int info) {
        infos[2] = info;
    }
    public void addInfo4(int info) {
        infos[3] = info;
    }

    public int getInfo(int index) {
        return infos[index];
    }

    public int getInfo1() {
        return infos[0];
    }
    public int getInfo2() {
        return infos[1];
    }
    public int getInfo3() {
        return infos[2];
    }
    public int getInfo4() {
        return infos[3];
    }


    /**
     * Getter for property date.
     * @return Value of property date.
     */
    public java.util.Date getDate() {
        return date;
    }

    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public int getId() {
        return id;
    }

    /**
     * Getter for property type.
     * @return Value of property type.
     */
    public int getType() {
        return type;
    }

    /**
     * Getter for property infos.
     * @return Value of property infos.
     */
    public int[] getInfos() {
        return this.infos;
    }

    public String toString() {
        return getId()+", "+getType()+", "+getDate()+", "+getInfo1()+", "+getInfo2()+", "+getInfo3()+", "+getInfo4();
    }

}
