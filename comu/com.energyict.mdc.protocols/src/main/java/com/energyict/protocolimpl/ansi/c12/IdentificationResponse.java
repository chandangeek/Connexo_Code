/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * IdentificationResponse.java
 *
 * Created on 16 oktober 2005, 17:22
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.ansi.c12.tables.IdentificationFeature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Koen
 */
public class IdentificationResponse extends AbstractResponse {

    // code identifying reference standard
    static public final int ANSI_C12=0x00;
    static public final int INDUSTRY_CANADA=0x01;
    private int std;

    // referenced standard version number
    private int ver;

    // referenced standard revision number
    private int rev;

    private List identificationFeatures=new ArrayList();

    /** Creates a new instance of IdentificationResponse */
    public IdentificationResponse(PSEMServiceFactory psemServiceFactory) {
        super(psemServiceFactory);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();

        strBuff.append("IdentificationResponse:\n");
        strBuff.append("    std=0x"+Integer.toHexString(std)+", ver=0x"+Integer.toHexString(ver)+", rev0x"+Integer.toHexString(rev)+"\n");
        if (getIdentificationFeatures().size()>0) {
            for (int i=0;i<getIdentificationFeatures().size();i++) {
                IdentificationFeature idf = (IdentificationFeature)getIdentificationFeatures().get(i);
                strBuff.append("    "+idf+"\n");
            }
        }
        return strBuff.toString();
    }

    protected void parse(ResponseData responseData) throws IOException {
        // in case of <ok>
        byte[] data = responseData.getData();
        setStd(C12ParseUtils.getInt(data,1));
        setVer(C12ParseUtils.getInt(data,2));
        setRev(C12ParseUtils.getInt(data,3));
        int offset = 4;
        while(true) {
            int features = C12ParseUtils.getInt(data,offset++);
            if (features == 0)
                break;
            else {
                IdentificationFeature idf = new IdentificationFeature();
                idf.setAuthenticationType(C12ParseUtils.getInt(data,offset++));
                idf.setAuthentificationAlgorithm(C12ParseUtils.getInt(data,offset++));
                if (features == 2) {
                   int ticketLength = C12ParseUtils.getInt(data,offset++);
                   idf.setTicket(ProtocolUtils.getSubArray2(data, offset,  ticketLength));
                   offset+=ticketLength;
                }
                getIdentificationFeatures().add(idf);
            }
        }


    }

    public int getStd() {
        return std;
    }

    public void setStd(int std) {
        this.std = std;
    }

    public int getVer() {
        return ver;
    }

    public void setVer(int ver) {
        this.ver = ver;
    }

    public int getRev() {
        return rev;
    }

    public void setRev(int rev) {
        this.rev = rev;
    }

    public List getIdentificationFeatures() {
        return identificationFeatures;
    }

    public IdentificationFeature getIdentificationFeature0() {
        return getIdentificationFeature(0);
    }
    public IdentificationFeature getIdentificationFeature(int index) {
        if (identificationFeatures.size() == 0)
            return null;
        else
            return (IdentificationFeature)identificationFeatures.get(index);
    }

}
