/*
 * TransmissionCause.java
 *
 * Created on 18 juni 2003, 17:04
 */

package com.energyict.protocolimpl.mbus.core.connection.iec870;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author  Koen
 */
class IEC870TransmissionCause {
    private static final List<IEC870TransmissionCause> CAUSES = new ArrayList<>();
    static {
        CAUSES.add(new IEC870TransmissionCause(0,"not used",""));
        CAUSES.add(new IEC870TransmissionCause(1,"periodic, cyclic per/cyc","CYCLIC"));
        CAUSES.add(new IEC870TransmissionCause(2,"background scan3 back","BACK"));
        CAUSES.add(new IEC870TransmissionCause(3,"spontaneous spont","SPONT"));
        CAUSES.add(new IEC870TransmissionCause(4,"initialized init","INIT"));
        CAUSES.add(new IEC870TransmissionCause(5,"request or requested req","REQ"));
        CAUSES.add(new IEC870TransmissionCause(6,"activation act","ACT"));
        CAUSES.add(new IEC870TransmissionCause(7,"activation confirmation actcon","ACTCON"));
        CAUSES.add(new IEC870TransmissionCause(8,"deactivation deact","DEACT"));
        CAUSES.add(new IEC870TransmissionCause(9,"deactivation confirmation deactcon","DEACTCON"));
        CAUSES.add(new IEC870TransmissionCause(10,"activation termination actterm","ACTTERM"));
        CAUSES.add(new IEC870TransmissionCause(11,"return information caused by a remote command retrem","RETREM"));
        CAUSES.add(new IEC870TransmissionCause(12,"return information caused by a local command retloc","RETLOC"));
        CAUSES.add(new IEC870TransmissionCause(13,"file transfer file",""));
        CAUSES.add(new IEC870TransmissionCause(20,"interrogated by station interrogation inrogen","INROGEN"));
        CAUSES.add(new IEC870TransmissionCause(21,"interrogated by group 1 interrogation inro1","INRO1"));
        CAUSES.add(new IEC870TransmissionCause(22,"interrogated by group 2 interrogation inro2","INRO2"));
        CAUSES.add(new IEC870TransmissionCause(23,"interrogated by group 3 interrogation inro3","INRO3"));
        CAUSES.add(new IEC870TransmissionCause(24,"interrogated by group 4 interrogation inro4","INRO4"));
        CAUSES.add(new IEC870TransmissionCause(25,"interrogated by group 5 interrogation inro5","INRO5"));
        CAUSES.add(new IEC870TransmissionCause(26,"interrogated by group 6 interrogation inro6","INRO6"));
        CAUSES.add(new IEC870TransmissionCause(27,"interrogated by group 7 interrogation inro7","INRO7"));
        CAUSES.add(new IEC870TransmissionCause(28,"interrogated by group 8 interrogation inro8","INRO8"));
        CAUSES.add(new IEC870TransmissionCause(29,"interrogated by group 9 interrogation inro9","INRO9"));
        CAUSES.add(new IEC870TransmissionCause(30,"interrogated by group 10 interrogation inro10","INRO10"));
        CAUSES.add(new IEC870TransmissionCause(31,"interrogated by group 11 interrogation inro11","INRO11"));
        CAUSES.add(new IEC870TransmissionCause(32,"interrogated by group 12 interrogation inro12","INRO12"));
        CAUSES.add(new IEC870TransmissionCause(33,"interrogated by group 13 interrogation inro13","INRO13"));
        CAUSES.add(new IEC870TransmissionCause(34,"interrogated by group 14 interrogation inro14","INRO14"));
        CAUSES.add(new IEC870TransmissionCause(35,"interrogated by group 15 interrogation inro15","INRO15"));
        CAUSES.add(new IEC870TransmissionCause(36,"interrogated by group 16 interrogation inro16","INRO16"));
        CAUSES.add(new IEC870TransmissionCause(37,"requested by general counter request reqcogen","REQCOGEN"));
        CAUSES.add(new IEC870TransmissionCause(38,"requested by group 1 counter request reqco1","REQCO1"));
        CAUSES.add(new IEC870TransmissionCause(39,"requested by group 2 counter request reqco2","REQCO2"));
        CAUSES.add(new IEC870TransmissionCause(40,"requested by group 3 counter request reqco3","REQCO3"));
        CAUSES.add(new IEC870TransmissionCause(41,"requested by group 4 counter request reqco4","REQCO4"));
        CAUSES.add(new IEC870TransmissionCause(44,"unknown type identification",""));
        CAUSES.add(new IEC870TransmissionCause(45,"unknown cause of transmission",""));
        CAUSES.add(new IEC870TransmissionCause(46,"unknown common address of ASDU",""));
        CAUSES.add(new IEC870TransmissionCause(47,"unknown information object address",""));

        // reserved cause ranges
        CAUSES.add(new IEC870TransmissionCause(48,"(48..63)for special use (private range)",""));
        CAUSES.add(new IEC870TransmissionCause(14,"(14..19)reserved for further compatible definitions",""));
        CAUSES.add(new IEC870TransmissionCause(42,"(42..43)reserved for further compatible definitions",""));
    }

    private int id;
    private String description;
    private String abbr;

    private IEC870TransmissionCause(int id, String description, String abbr) {
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
        if ((id>=48) && (id<=63)) {
            id = 48;
        }
        if ((id>=14) && (id<=19)) {
            id = 14;
        }
        if ((id>=42) && (id<=43)) {
            id = 42;
        }

        for (IEC870TransmissionCause cause : CAUSES) {
            if (cause.getId() == id) {
                return cause;
            }
        }
        throw new IllegalArgumentException("IEC870TransmissionCause, id "+id+" not found");
    }

    public static int getId(String abbr) {
        for (IEC870TransmissionCause cause : CAUSES) {
            if (cause.getAbbr().compareTo(abbr) == 0) {
                return cause.getId();
            }
        }
        throw new IllegalArgumentException("IEC870TransmissionCause, "+abbr+" not found");
    }

}