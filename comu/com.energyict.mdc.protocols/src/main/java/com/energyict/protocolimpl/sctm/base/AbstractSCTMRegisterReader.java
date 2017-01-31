/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AbstractSCTMRegisterReader.java
 *
 * Created on 15 december 2004, 15:15
 */

package com.energyict.protocolimpl.sctm.base;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.metcom.Metcom;
import com.energyict.protocolimpl.siemens7ED62.SCTMRegister;
import com.energyict.protocolimpl.siemens7ED62.SiemensSCTM;
import com.energyict.protocolimpl.siemens7ED62.SiemensSCTMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 *
 * @author  Koen
 */
abstract public class AbstractSCTMRegisterReader {

    Metcom metcom=null; // KV 06092005 WVEM
    List sctmRegisterSpecs = new ArrayList();
    SiemensSCTM siemensSCTM=null; // KV 06092005 WVEM

    /** Creates a new instance of AbstractSCTMRegisterReader */
    public AbstractSCTMRegisterReader(Metcom metcom) {
        this.metcom=metcom;
    }

    // KV 06092005 WVEM
    public AbstractSCTMRegisterReader(SiemensSCTM siemensSCTM) {
        this.siemensSCTM=siemensSCTM;
    }

    private SiemensSCTM getSCTMConnection() {
        if (metcom==null)
            return siemensSCTM;
        else
            return metcom.getSCTMConnection();
    }

    private SCTMRegister getSCTMRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        try {
            byte[] data=null;

            // KV 06092005 WVEM
            data = getSCTMConnection().sendRequest(getSCTMConnection().TABENQ1,name.getBytes());

            if (data==null)
               return null;
            else
               return new SCTMRegister(data);
        }
        catch(SiemensSCTMException e) {
            throw new IOException("AbstractSCTMRegisterReader, getTime, SiemensSCTMException, "+e.getMessage());
        }
    }

    public RegisterValue readRegisterValue(ObisCode obisCode) throws IOException {
        if ((obisCode.getA() == 0) &&
            (obisCode.getC() == 96) &&
            (obisCode.getD() == 99)) {
            int address;
            address = (obisCode.getB()*100+obisCode.getE())*100+obisCode.getF();

            SCTMRegister sc = getSCTMRegister(String.valueOf(address));
            if (sc == null)
                throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
            if (sc.isQuantity())
                return new RegisterValue(obisCode,sc.getQuantityValue());
            else
                return new RegisterValue(obisCode,sc.toString().trim());
        }
        else {
            Quantity quantity = readRegisterQuantity(obisCode);
            return new RegisterValue(obisCode,quantity);
        }
    }

    public long readRegisterLong(ObisCode obisCode) throws IOException {
        Iterator it = getSctmRegisterSpecs().iterator();
        while(it.hasNext()) {
            SCTMRegisterSpec srs = (SCTMRegisterSpec)it.next();
            String address = srs.getRegisterSpecAddress(obisCode);
            if (address != null) {
                SCTMRegister sc = getSCTMRegister(address);
                if (sc == null)
                   throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
                else
                   return sc.getLongValue();
            }

        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }

    public Quantity readRegisterQuantity(ObisCode obisCode) throws IOException {
        Iterator it = getSctmRegisterSpecs().iterator();
        while(it.hasNext()) {
            SCTMRegisterSpec srs = (SCTMRegisterSpec)it.next();
            String address = srs.getRegisterSpecAddress(obisCode);
            if (address != null) {
                SCTMRegister sc = getSCTMRegister(address);
                if (sc == null)
                   throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
                else
                   return sc.getQuantityValue();

            }
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }

    public String getRegisterInfo() throws IOException {
        StringBuffer strBuff=new StringBuffer();
        Iterator it = getSctmRegisterSpecs().iterator();
        while(it.hasNext()) {
            SCTMRegisterSpec srs = (SCTMRegisterSpec)it.next();
            String description = srs.getDescription();
            description = description.replaceAll("@", String.valueOf(srs.getObisCode().getB()+(srs.getSubAddressRange()==1?"":".."+((srs.getObisCode().getB()+srs.getSubAddressRange())-1))));
            strBuff.append(srs.getObisCode()+", "+description+" ("+srs.getRegisterSpecAddressRange()+")\n");
        }

        strBuff.append("Manufacturer specific codes following format BEEFF (SCTM address) --> 0.B.96.99.E.F (OBIS code)\n");
        return strBuff.toString();
    }

    public boolean isManufacturerSpecific(ObisCode obisCode) {
        if ((obisCode.getA() == 0) &&
            (obisCode.getC() == 96) &&
            (obisCode.getD() == 99))
            return true;
        else
            return false;

    }

    public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {

        if ((obisCode.getA() == 0) &&
            (obisCode.getC() == 96) &&
            (obisCode.getD() == 99)) {
            int address = (obisCode.getB()*100+obisCode.getE())*100+obisCode.getF();
            return new RegisterInfo("Manufacturer specific register at address "+address);
        }
        else {
            Iterator it = getSctmRegisterSpecs().iterator();
            while(it.hasNext()) {
                SCTMRegisterSpec srs = (SCTMRegisterSpec)it.next();
                if (srs.containsObisCode(obisCode)) {
                    String description = srs.getDescription();
                    description = description.replaceAll("@", String.valueOf(obisCode.getB()));
                    return new RegisterInfo(description);
                }
            }
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }




    /**
     * Getter for property sctmRegisterSpecs.
     * @return Value of property sctmRegisterSpecs.
     */
    public java.util.List getSctmRegisterSpecs() {
        return sctmRegisterSpecs;
    }

    /**
     * Setter for property sctmRegisterSpecs.
     * @param sctmRegisterSpecs New value of property sctmRegisterSpecs.
     */
    public void setSctmRegisterSpecs(java.util.List sctmRegisterSpecs) {
        this.sctmRegisterSpecs = sctmRegisterSpecs;
    }



}
