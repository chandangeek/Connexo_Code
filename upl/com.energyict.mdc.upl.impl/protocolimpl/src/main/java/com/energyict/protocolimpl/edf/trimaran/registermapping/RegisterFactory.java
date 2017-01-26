/*
 * RegisterFactory.java
 *
 * Created on 27 juni 2006, 11:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaran.registermapping;

import com.energyict.cbo.Quantity;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.edf.trimaran.Trimaran;
import com.energyict.protocolimpl.edf.trimaran.core.MonthInfoTable;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Koen
 */
public class RegisterFactory {

    Trimaran trimeran;
    List registers=null;

    /** Creates a new instance of RegisterFactory */
    public RegisterFactory(Trimaran trimeran) {
        this.trimeran=trimeran;

    }

    public Register findRegister(ObisCode obc) throws IOException {
        ObisCode obisCode = new ObisCode(obc.getA(),obc.getB(),obc.getC(),obc.getD(),obc.getE(),Math.abs(obc.getF()));
        Iterator it = getRegisters().iterator();
        while(it.hasNext()) {
            Register register = (Register)it.next();
            if (register.getRegisterValue().getObisCode().equals(obisCode)) {
                return register;
            }
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }

    public List getRegisters() throws IOException {
        if (registers == null) {
            buidRegisterList();
        }
        return registers;
    }

    public void buidRegisterList() throws IOException {
        registers = new ArrayList();

        buildIndexes(255,trimeran.getDataFactory().getCurrentMonthInfoTable());
        buildIndexes(0,trimeran.getDataFactory().getPreviousMonthInfoTable());
    }

    private void buildIndexes(int fField,MonthInfoTable monthInfo) {
        Quantity quantity=null;
        RegisterValue registerValue=null;
        Date toDate=null;
        Date fromDate=null;

        if (fField == 0) {
            Calendar cal = ProtocolUtils.getCalendar(trimeran.getTimeZone());
            int month = monthInfo.getMonth();
            if ((++month) > 12)
                month=1;
            month--;
            cal.set(Calendar.MONTH,month);
            cal.set(Calendar.DAY_OF_MONTH,1);
            cal.set(Calendar.HOUR_OF_DAY,2);
            cal.set(Calendar.MINUTE,0);
            cal.set(Calendar.SECOND,0);
            toDate = new Date(cal.getTime().getTime());
            cal.add(Calendar.MONTH,-1);
            fromDate = new Date(cal.getTime().getTime());
        }

        if (fField == 255) {
            Calendar cal = ProtocolUtils.getCalendar(trimeran.getTimeZone());
            int month = monthInfo.getMonth()-1;
            cal.set(Calendar.MONTH,month);
            cal.set(Calendar.DAY_OF_MONTH,1);
            cal.set(Calendar.HOUR_OF_DAY,2);
            cal.set(Calendar.MINUTE,0);
            cal.set(Calendar.SECOND,0);
            fromDate = new Date(cal.getTime().getTime());
        }


        for (int eField=1;eField<=3;eField++) {
            quantity = monthInfo.getActiveQuantity(eField-1);
            registerValue = new RegisterValue(ObisCode.fromString("1.1.1.8."+eField+"."+fField),quantity,null,toDate);
            registers.add(new Register(registerValue));

            quantity = monthInfo.getReactiveQuantity(eField-1);
            registerValue = new RegisterValue(ObisCode.fromString("1.1.3.8."+eField+"."+fField),quantity,null,toDate);
            registers.add(new Register(registerValue));

            quantity = monthInfo.getSquareExceedQuantity(eField-1);
            registerValue = new RegisterValue(ObisCode.fromString("1.1.1.38."+eField+"."+fField),quantity,null,fromDate,toDate);
            registers.add(new Register(registerValue));

            quantity = monthInfo.getMaxDemandQuantity(eField-1);
            registerValue = new RegisterValue(ObisCode.fromString("1.1.1.6."+eField+"."+fField),quantity,null,fromDate,toDate);
            registers.add(new Register(registerValue));

            quantity = monthInfo.getNrOf10inuteIntervalsQuantity(eField-1);
            registerValue = new RegisterValue(ObisCode.fromString("0.1.96.8."+eField+"."+fField),quantity,null,fromDate,toDate);
            registers.add(new Register(registerValue));

            quantity = monthInfo.getNrOfExceedsQuantity(eField-1);
            registerValue = new RegisterValue(ObisCode.fromString("1.1.1.128."+eField+"."+fField),quantity,null,fromDate,toDate);
            registers.add(new Register(registerValue));
        } // for (int eField=1;eField<=3;eField++)

        quantity = monthInfo.getExceededEnergyQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.10.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));

        quantity = monthInfo.getSubscribedPowerPeakQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.129.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));
        quantity = monthInfo.getSubscribedPowerNormalWinterQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.130.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));
        quantity = monthInfo.getSubscribedPowerLowWinterQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.131.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));
        quantity = monthInfo.getSubscribedPowerNormalSummerQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.132.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));
        quantity = monthInfo.getSubscribedPowerLowSummerQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.133.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));
        quantity = monthInfo.getSubscribedPowerMobileQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.134.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));
        quantity = monthInfo.getSubscribedPowerNormalHalfSeasonQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.135.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));
        quantity = monthInfo.getSubscribedPowerLowHalfSeasonQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.136.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));
        quantity = monthInfo.getSubscribedPowerLowLowSeasonQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.137.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));
        quantity = monthInfo.getRapportQuantity();
        registerValue = new RegisterValue(ObisCode.fromString("1.1.1.138.1."+fField),quantity,null,fromDate,toDate);
        registers.add(new Register(registerValue));



    } // private void buildIndexes(int fField,MonthInfoTable monthInfo)



}
