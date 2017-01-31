/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RealTimeBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum.basepages;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class RegisterDataBasePage extends AbstractBasePage {

    private int offset;
    private int selfReadSet;
    private List quantities;
    private Date selfReadDate;
    private int nrOfRegisterReadings;

    /** Creates a new instance of RealTimeBasePage */
    public RegisterDataBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RegisterDataBasePage on offset "+offset+":\n");

        if (getSelfReadSet() != -1) {
            strBuff.append("   selfReadSet="+selfReadSet+"\n");
            strBuff.append("   selfReadDate="+selfReadDate+"\n");
            strBuff.append("   nrOfRegisterReadings="+nrOfRegisterReadings+"\n");
        }

        for (int i=0;i<getQuantities().size();i++) {
            strBuff.append("       registers["+i+"]="+(Quantity)getQuantities().get(i)+"\n");
        }

        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        int usedRegisters = nrOfRegisters(((BasePagesFactory)getBasePagesFactory()).getProgramTableBasePage(false).getProgramEntries())+
                            nrOfRegisters(((BasePagesFactory)getBasePagesFactory()).getProgramTableBasePage(true).getProgramEntries());
        int length = usedRegisters*6;
        if (getSelfReadSet() != -1)
            length+=6;

        return new BasePageDescriptor(getOffset()+length*(getSelfReadSet()==-1?0:getSelfReadSet()),length);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        BigDecimal bd;

        if (getSelfReadSet() != -1) {
            TimeZone tz = getBasePagesFactory().getProtocolLink().getTimeZone();
            if (!((BasePagesFactory)getBasePagesFactory()).getGeneralSetUpBasePage().isDstEnabled())
                tz = ProtocolUtils.getWinterTimeZone(tz);
            setSelfReadDate(com.energyict.protocolimpl.itron.protocol.Utils.buildDateYearFirst(data, offset, tz));
            offset += com.energyict.protocolimpl.itron.protocol.Utils.buildDateSize();
            setNrOfRegisterReadings((int)data[offset++]&0xFF);
        }


        setQuantities(new ArrayList());

        List programEntries = ((BasePagesFactory)getBasePagesFactory()).getProgramTableBasePage(false).getProgramEntries();
        offset = buildRegisters(programEntries,data,offset);

        programEntries = ((BasePagesFactory)getBasePagesFactory()).getProgramTableBasePage(true).getProgramEntries();
        offset = buildRegisters(programEntries,data,offset);


    } // protected void parse(byte[] data) throws IOException


    private int buildRegisters(List programEntries, byte[] data, int offset) throws IOException {
        Iterator it = programEntries.iterator();
        while(it.hasNext()) {
            ProgramEntry p = (ProgramEntry)it.next();
            if ((!p.isNonRegisterValue()) && (p.getDisplaySetup()!=0)) {
                UnitTable ut = UnitTable.findUnitTable(p.getRegisterNr());
                BigDecimal bd = ut.getRegisterValue(data,offset, ((BasePagesFactory)getBasePagesFactory()).getFirmwareRevisionBasePage().getFirmwareRevision(),p.getScale());
                bd = bd.multiply(((BasePagesFactory)getBasePagesFactory()).getQuantum().getAdjustRegisterMultiplier());
                Quantity quantity = new Quantity(bd, Unit.get(ut.getUnit().getDlmsCode(), p.getScale()));
                getQuantities().add(quantity);

                // debug
//                byte[] sdata = ProtocolUtils.getSubArray2(data, offset, 6);
//                System.out.println(ProtocolUtils.outputHexString(sdata)+", "+quantity+", regtype=0x"+Integer.toHexString(p.getRegisterType())+", registerNr="+p.getRegisterNr()+", custcode=0x"+Integer.toHexString(p.getCustomerIdCode())+", displsetup=0x"+Integer.toHexString(p.getDisplaySetup())+"\n");


                offset+=6;

            } // if ((!p.isNonRegisterValue()) && (p.getDisplaySetup()!=0))
        } // while(it.hasNext()) {
        return offset;
    } // private void buildRegisters()

    private int nrOfRegisters(List programEntries) throws IOException {
        int count=0;
        Iterator it = programEntries.iterator();
        while(it.hasNext()) {
            ProgramEntry p = (ProgramEntry)it.next();
            if ((!p.isNonRegisterValue()) && (p.getDisplaySetup()!=0)) {
                count++;
            } // if ((!p.isNonRegisterValue()) && (p.getDisplaySetup()!=0))
        } // while(it.hasNext()) {
        return count;
    } // private int nrOfRegisters(List programEntries) throws IOException


    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }



    public int getSelfReadSet() {
        return selfReadSet;
    }

    public void setSelfReadSet(int selfReadSet) {
        this.selfReadSet = selfReadSet;
    }

    public Date getSelfReadDate() {
        return selfReadDate;
    }

    public void setSelfReadDate(Date selfReadDate) {
        this.selfReadDate = selfReadDate;
    }

    public int getNrOfRegisterReadings() {
        return nrOfRegisterReadings;
    }

    public void setNrOfRegisterReadings(int nrOfRegisterReadings) {
        this.nrOfRegisterReadings = nrOfRegisterReadings;
    }



    public List getQuantities() {
        return quantities;
    }

    public void setQuantities(List quantities) {
        this.quantities = quantities;
    }


} // public class RealTimeBasePage extends AbstractBasePage
