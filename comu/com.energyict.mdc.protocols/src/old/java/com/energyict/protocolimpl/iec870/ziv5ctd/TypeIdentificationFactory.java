/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TypeIdentificationFactory.java
 *
 * Created on 17 January 2006, 08:48
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import com.energyict.mdc.common.NotFoundException;

import java.util.Iterator;
import java.util.TimeZone;
import java.util.TreeMap;

/** @author fbo */

public class TypeIdentificationFactory {

    TreeMap idMap = new TreeMap();
    TreeMap mnemoMap = new TreeMap();

    TimeZone timeZone;

    public TypeIdentification get( int id ){
        Integer anId = new Integer( id );
        return (TypeIdentification)idMap.get(anId);
    }

    /**
     * Creates a new instance of TypeIdentificationFactory
     */
    TypeIdentificationFactory(TimeZone timeZone) {
        this.timeZone = timeZone;
        //Single-point information with time tag
        map(0x01, "M_SP_TA_2", new Type1Parser(timeZone) );
        //Operational integrated totals, four octets each
        map(0x08, "M_IT_TG_2", new Type8Parser());
        //Periodically reset operational integrated totals, four octets each
        map(0x0b, "M_IT_TK_2", new Type8Parser() );

        //Manufacturer and product specification of integrated total DTE
        map(0x47, "P_MP_NA_2", new Type47Parser());
        //Current system time of integrated total DTE
        map(0x48, "M_TI_TA_2", new Type48Parser(timeZone));
        //Read manufacturer and product specification
        map(100, "C_RD_NA_2"   );
        //Read record of single-point information with time tag of a selected time range
        map(0x66, "C_SP_NB_2" );
        //Read current system time of integrated total DTE
        map(103, "C_TI_NA_2");
        //Read operational integrated totals of a selected time range
        //and of a selected range of addresses"
        map(0x7a, "C_CI_NT_2" );
        //Read periodically reset operational integrated totals of a
        //selected time range and of a selected range of addresses
        map(0x7b, "C_CI_NU_2" );
        map(128, "M_DS_TA_2");
        map(129, "P_ME_NA_2");
        map(130, "M_DS_TB_2");
        map(131, "M_CH_TA_2");
        map(132, "C_PK_2");
        //Tarification information
        map(133, "C_TA_VC_2");
        map(0x86, "C_TA_VM_2");
        map(0x87, "M_TA_VC_2", new Type87Parser(timeZone));
        map(0x88, "M_TA_VM_2", new Type88Parser(timeZone));
        map(0x89, "C_TA_CP_2");
        map(139, "M_IB_TG_2");
        map(140, "M_IB_TK_2");
        map(141, "C_RM_NA_2");
        //"Shipment of the configuration of the equipment RM"
        map(142, "M_RM_NA_2");
        map(150, "");
        map(151, "", new Type97Parser() );
        //Grup. Instantaneous. Resquesta.
        map(162, "");
        //Grup. Instantaneous. Resquesta.
        map(163, "", new TypeA3Parser(timeZone));
        //Change date and hour
        map(181, "C_CS_TA_2" );
        //Read parameters of measure point
        map(182, "C_PI_NA_2");
        //Initiate session and send access key
        map(183, "C_AC_NA_2");
        //Finalize session
        map(187, "C_FS_NA_2" );

    }

    /**
     * @param id of IEC870TypeIdentification
     * @return IEC870TypeIdentification with id
     */
    public TypeIdentification getTypeIdentification(int id) {
        Integer iid = new Integer(id);
        TypeIdentification ti = (TypeIdentification) idMap.get(iid);
        if (ti == null)
            throw createNotFoundException(id + " not found");
        else
            return ti;
    }

    /**
     * @param mnemo of IEC870TypeIdentification
     * @return IEC870TypeIdentification with mnemo code
     */
    public int getIdForMnemo(String mnemo) {
        TypeIdentification ti = (TypeIdentification) mnemoMap.get(mnemo);
        if (ti != null) {
            return ti.getId();
        } else {
            throw createNotFoundException(mnemo + " not found");
        }
    }

    protected TypeIdentification map(int id, String mnemo ) {
        TypeIdentification type = new TypeIdentification(id, mnemo, null);
        idMap.put(new Integer(id), type);
        mnemoMap.put(mnemo, type);
        return type;
    }

    protected TypeIdentification map(
            int id, String mnemo, TypeParser typeParser) {

        TypeIdentification type =
            new TypeIdentification(id, mnemo, typeParser);

        idMap.put(new Integer(id), type);
        mnemoMap.put(mnemo, type);
        return type;
    }

    private NotFoundException createNotFoundException(String msg) {
        String completeMsg = "IEC870TypeIdentificationFactory " + msg;
        return new NotFoundException(completeMsg);
    }

    public String toString() {
        StringBuilder result = new StringBuilder("IEC870TypeIdentificationFactory \n");
        Iterator i = idMap.values().iterator();
        while (i.hasNext())
            result.append(i.next().toString() + "\n");
        return result.toString();
    }

}
