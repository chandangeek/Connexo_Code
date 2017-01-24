/*
 * TransmissionCause.java
 *
 * Created on 18 juni 2003, 17:04
 */

package com.energyict.protocolimpl.iec870;

import com.energyict.mdc.common.NotFoundException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author  Koen
 */
public class IEC870TransmissionCause {
    public static final List causes = new ArrayList();
    static {
        causes.add(new IEC870TransmissionCause(0,"not used",""));
        causes.add(new IEC870TransmissionCause(1,"periodic, cyclic per/cyc","CYCLIC"));
        causes.add(new IEC870TransmissionCause(2,"background scan3 back","BACK"));
        causes.add(new IEC870TransmissionCause(3,"spontaneous spont","SPONT"));
        causes.add(new IEC870TransmissionCause(4,"initialized init","INIT"));
        causes.add(new IEC870TransmissionCause(5,"request or requested req","REQ"));
        causes.add(new IEC870TransmissionCause(6,"activation act","ACT"));
        causes.add(new IEC870TransmissionCause(7,"activation confirmation actcon","ACTCON"));
        causes.add(new IEC870TransmissionCause(8,"deactivation deact","DEACT"));
        causes.add(new IEC870TransmissionCause(9,"deactivation confirmation deactcon","DEACTCON"));
        causes.add(new IEC870TransmissionCause(10,"activation termination actterm","ACTTERM"));
        causes.add(new IEC870TransmissionCause(11,"return information caused by a remote command retrem","RETREM"));
        causes.add(new IEC870TransmissionCause(12,"return information caused by a local command retloc","RETLOC"));
        causes.add(new IEC870TransmissionCause(13,"file transfer file",""));
        causes.add(new IEC870TransmissionCause(20,"interrogated by station interrogation inrogen","INROGEN"));
        causes.add(new IEC870TransmissionCause(21,"interrogated by group 1 interrogation inro1","INRO1"));
        causes.add(new IEC870TransmissionCause(22,"interrogated by group 2 interrogation inro2","INRO2"));
        causes.add(new IEC870TransmissionCause(23,"interrogated by group 3 interrogation inro3","INRO3"));
        causes.add(new IEC870TransmissionCause(24,"interrogated by group 4 interrogation inro4","INRO4"));
        causes.add(new IEC870TransmissionCause(25,"interrogated by group 5 interrogation inro5","INRO5"));
        causes.add(new IEC870TransmissionCause(26,"interrogated by group 6 interrogation inro6","INRO6"));
        causes.add(new IEC870TransmissionCause(27,"interrogated by group 7 interrogation inro7","INRO7"));
        causes.add(new IEC870TransmissionCause(28,"interrogated by group 8 interrogation inro8","INRO8"));
        causes.add(new IEC870TransmissionCause(29,"interrogated by group 9 interrogation inro9","INRO9"));
        causes.add(new IEC870TransmissionCause(30,"interrogated by group 10 interrogation inro10","INRO10"));
        causes.add(new IEC870TransmissionCause(31,"interrogated by group 11 interrogation inro11","INRO11"));
        causes.add(new IEC870TransmissionCause(32,"interrogated by group 12 interrogation inro12","INRO12"));
        causes.add(new IEC870TransmissionCause(33,"interrogated by group 13 interrogation inro13","INRO13"));
        causes.add(new IEC870TransmissionCause(34,"interrogated by group 14 interrogation inro14","INRO14"));
        causes.add(new IEC870TransmissionCause(35,"interrogated by group 15 interrogation inro15","INRO15"));
        causes.add(new IEC870TransmissionCause(36,"interrogated by group 16 interrogation inro16","INRO16"));
        causes.add(new IEC870TransmissionCause(37,"requested by general counter request reqcogen","REQCOGEN"));
        causes.add(new IEC870TransmissionCause(38,"requested by group 1 counter request reqco1","REQCO1"));
        causes.add(new IEC870TransmissionCause(39,"requested by group 2 counter request reqco2","REQCO2"));
        causes.add(new IEC870TransmissionCause(40,"requested by group 3 counter request reqco3","REQCO3"));
        causes.add(new IEC870TransmissionCause(41,"requested by group 4 counter request reqco4","REQCO4"));
        causes.add(new IEC870TransmissionCause(44,"unknown type identification",""));
        causes.add(new IEC870TransmissionCause(45,"unknown cause of transmission",""));
        causes.add(new IEC870TransmissionCause(46,"unknown common address of ASDU",""));
        causes.add(new IEC870TransmissionCause(47,"unknown information object address",""));

        // reserved cause ranges
        causes.add(new IEC870TransmissionCause(48,"(48..63)for special use (private range)",""));
        causes.add(new IEC870TransmissionCause(14,"(14..19)reserved for further compatible definitions",""));
        causes.add(new IEC870TransmissionCause(42,"(42..43)reserved for further compatible definitions",""));
    }

    int id;
    String description;
    String abbr;

    /** Creates a new instance of TransmissionCause */
    public IEC870TransmissionCause(int id, String description, String abbr) {
        this.id=id;
        this.description=description;
        this.abbr=abbr;
    }

    public int getId() {
        return id;
    }
    public String getDescription() {
        return description;
    }
    public String getAbbr() {
        return abbr;
    }

    public static IEC870TransmissionCause getTransmissionCause(int id) {

        // reserved cause ranges
        if ((id>=48) && (id<=63)) id = 48;
        if ((id>=14) && (id<=19)) id = 14;
        if ((id>=42) && (id<=43)) id = 42;

        Iterator it = causes.iterator();
        while(it.hasNext()) {
            IEC870TransmissionCause cause = (IEC870TransmissionCause)it.next();
            if (cause.getId() == id) return cause;
        }
        throw new NotFoundException("IEC870TransmissionCause, id "+id+" not found");
    }
    public static int getId(String abbr) {

        Iterator it = causes.iterator();
        while(it.hasNext()) {
            IEC870TransmissionCause cause = (IEC870TransmissionCause)it.next();
            if (cause.getAbbr().compareTo(abbr) == 0) return cause.getId();
        }
        throw new NotFoundException("IEC870TransmissionCause, "+abbr+" not found");
    }

}
