/*
 * ViewableFileId.java
 *
 * Created on 19 december 2006, 11:18
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Koen
 */
public class ViewableFileId {

    static private final int MASS_MEMORY_1 = 100;
    static private final int MASS_MEMORY_2 = 101;
    static private final int EXTENDED_EVENT_LOG_1 = 150;
    static private final int EXTENDED_EVENT_LOG_2 = 151;
    static private final int EXTENDED_EVENT_LOG_3 = 152;
    static private final int EXTENDED_EVENT_LOG_4 = 153;
    static private final int EXTENDED_EVENT_LOG_5 = 154;
    static private final int EXTENDED_EVENT_LOG_6 = 155;

    static List viewableFileIds = new ArrayList();
    static {
        viewableFileIds.add(new ViewableFileId(MASS_MEMORY_1,"mass memory","first mass memory"));
        viewableFileIds.add(new ViewableFileId(MASS_MEMORY_2,"mass memory","second mass memory"));
        viewableFileIds.add(new ViewableFileId(102,"extended mass memory","first extended mass memory"));
        viewableFileIds.add(new ViewableFileId(103,"extended mass memory","second extended mass memory"));
        viewableFileIds.add(new ViewableFileId(104,"extended mass memory","third extended mass memory"));
        viewableFileIds.add(new ViewableFileId(105,"extended mass memory","fourth extended mass memory"));
        viewableFileIds.add(new ViewableFileId(106,"extended mass memory","fifth extended mass memory"));
        viewableFileIds.add(new ViewableFileId(107,"extended mass memory","sixth extended mass memory"));
        viewableFileIds.add(new ViewableFileId(108,"extended mass memory","seventh extended mass memory"));
        viewableFileIds.add(new ViewableFileId(109,"extended mass memory","eighth extended mass memory"));
        viewableFileIds.add(new ViewableFileId(110,"extended mass memory","reserved for more extended mass memories"));
        viewableFileIds.add(new ViewableFileId(111,"extended mass memory","reserved for more extended mass memories"));
        viewableFileIds.add(new ViewableFileId(112,"extended mass memory","reserved for more extended mass memories"));
        viewableFileIds.add(new ViewableFileId(113,"extended mass memory","reserved for more extended mass memories"));
        viewableFileIds.add(new ViewableFileId(114,"extended mass memory","reserved for more extended mass memories"));
        viewableFileIds.add(new ViewableFileId(115,"extended mass memory","reserved for more extended mass memories"));
        viewableFileIds.add(new ViewableFileId(116,"extended mass memory","reserved for more extended mass memories"));
        viewableFileIds.add(new ViewableFileId(117,"extended mass memory","reserved for more extended mass memories"));
        viewableFileIds.add(new ViewableFileId(118,"extended mass memory","reserved for more extended mass memories"));
        viewableFileIds.add(new ViewableFileId(119,"extended mass memory","reserved for more extended mass memories"));
        viewableFileIds.add(new ViewableFileId(120,"extended mass memory","reserved for more extended mass memories"));
        viewableFileIds.add(new ViewableFileId(121,"extended mass memory","reserved for more extended mass memories"));
        viewableFileIds.add(new ViewableFileId(EXTENDED_EVENT_LOG_1,"extended event log","first extended event log"));
        viewableFileIds.add(new ViewableFileId(EXTENDED_EVENT_LOG_2,"extended event log","second extended event log"));
        viewableFileIds.add(new ViewableFileId(EXTENDED_EVENT_LOG_3,"extended event log","third extended event log"));
        viewableFileIds.add(new ViewableFileId(EXTENDED_EVENT_LOG_4,"extended event log","fourth extended event log"));
        viewableFileIds.add(new ViewableFileId(EXTENDED_EVENT_LOG_5,"extended event log","fifth extended event log"));
        viewableFileIds.add(new ViewableFileId(EXTENDED_EVENT_LOG_6,"extended event log","sixth extended event log"));
        viewableFileIds.add(new ViewableFileId(200,"harmonics","phase A"));
        viewableFileIds.add(new ViewableFileId(201,"harmonics","phase B"));
        viewableFileIds.add(new ViewableFileId(202,"harmonics","phase C"));
        viewableFileIds.add(new ViewableFileId(300,"voltage quality","Sag Level 1 Phase A"));
        viewableFileIds.add(new ViewableFileId(301,"voltage quality","Sag Level 2 Phase A"));
        viewableFileIds.add(new ViewableFileId(302,"voltage quality","Sag Level 1 Phase B"));
        viewableFileIds.add(new ViewableFileId(303,"voltage quality","Sag Level 2 Phase B"));
        viewableFileIds.add(new ViewableFileId(304,"voltage quality","Sag Level 1 Phase C"));
        viewableFileIds.add(new ViewableFileId(305,"voltage quality","Sag Level 2 Phase C"));
        viewableFileIds.add(new ViewableFileId(400,"voltage quality","Swell Level 1 Phase A"));
        viewableFileIds.add(new ViewableFileId(401,"voltage quality","Swell Level 2 Phase A"));
        viewableFileIds.add(new ViewableFileId(402,"voltage quality","Swell Level 1 Phase B"));
        viewableFileIds.add(new ViewableFileId(403,"voltage quality","Swell Level 2 Phase B"));
        viewableFileIds.add(new ViewableFileId(404,"voltage quality","Swell Level 1 Phase C"));
        viewableFileIds.add(new ViewableFileId(405,"voltage quality","Swell Level 2 Phase C"));
        viewableFileIds.add(new ViewableFileId(500,"voltage quality","Interruption Class 1"));
        viewableFileIds.add(new ViewableFileId(501,"voltage quality","Interruption Class 2"));
        viewableFileIds.add(new ViewableFileId(502,"voltage quality","Interruption Class 3"));
        viewableFileIds.add(new ViewableFileId(600,"voltage quality","Imbalance Phase A"));
        viewableFileIds.add(new ViewableFileId(601,"voltage quality","Imbalance Phase B"));
        viewableFileIds.add(new ViewableFileId(602,"voltage quality","Imbalance Phase C"));

    }


    private int id;
    private String type;
    private String comment;

    /** Creates a new instance of ViewableFileId */
    private ViewableFileId(int id, String type, String comment) {
        this.setId(id);
        this.setType(type);
        this.setComment(comment);
    }

    public boolean isMassMemory1() {
        return getId()==MASS_MEMORY_1;
    }

    public boolean isMassMemory2() {
        return getId()==MASS_MEMORY_2;
    }

    public boolean isExtendedEventLog1() {
        return getId()==EXTENDED_EVENT_LOG_1;
    }

    public boolean isExtendedEventLog2() {
        return getId()==EXTENDED_EVENT_LOG_2;
    }

    public boolean isExtendedEventLog3() {
        return getId()==EXTENDED_EVENT_LOG_3;
    }

    public boolean isExtendedEventLog4() {
        return getId()==EXTENDED_EVENT_LOG_4;
    }

    public boolean isExtendedEventLog5() {
        return getId()==EXTENDED_EVENT_LOG_5;
    }

    public boolean isExtendedEventLog6() {
        return getId()==EXTENDED_EVENT_LOG_6;
    }

    static ViewableFileId findViewableFileId(int id) throws IOException {
        Iterator it = viewableFileIds.iterator();
        while(it.hasNext()) {
            ViewableFileId vid = (ViewableFileId)it.next();
            if (vid.getId() == id)
                return vid;
        }
        throw new IOException("ViewableFileId, findViewableFileId, invalid id "+id);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ViewableFileId:\n");
        strBuff.append("   comment="+getComment()+"\n");
        strBuff.append("   id="+getId()+"\n");
        strBuff.append("   type="+getType()+"\n");
        return strBuff.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public static ViewableFileId getMASS_MEMORY_1() throws IOException {
        return findViewableFileId(MASS_MEMORY_1);
    }

    public static ViewableFileId getMASS_MEMORY_2() throws IOException {
        return findViewableFileId(MASS_MEMORY_2);
    }

    public static ViewableFileId getEXTENDED_EVENT_LOG_1() throws IOException {
        return findViewableFileId(EXTENDED_EVENT_LOG_1);
    }

    public static ViewableFileId getEXTENDED_EVENT_LOG_2() throws IOException {
        return findViewableFileId(EXTENDED_EVENT_LOG_2);
    }

    public static ViewableFileId getEXTENDED_EVENT_LOG_3() throws IOException {
        return findViewableFileId(EXTENDED_EVENT_LOG_3);
    }

    public static ViewableFileId getEXTENDED_EVENT_LOG_4() throws IOException {
        return findViewableFileId(EXTENDED_EVENT_LOG_4);
    }

    public static ViewableFileId getEXTENDED_EVENT_LOG_5() throws IOException {
        return findViewableFileId(EXTENDED_EVENT_LOG_5);
    }

    public static ViewableFileId getEXTENDED_EVENT_LOG_6() throws IOException {
        return findViewableFileId(EXTENDED_EVENT_LOG_6);
    }



}
