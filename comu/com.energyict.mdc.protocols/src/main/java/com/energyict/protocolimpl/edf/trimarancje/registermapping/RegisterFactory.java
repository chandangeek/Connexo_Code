/*
 * RegisterFactory.java
 *
 * Created on 27 juni 2006, 11:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarancje.registermapping;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.edf.trimarancje.Trimaran;
import com.energyict.protocolimpl.edf.trimarancje.core.CurrentPeriodTable;
import com.energyict.protocolimpl.edf.trimarancje.core.PreviousPeriodTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Koen
 */
public class RegisterFactory {

    Trimaran trimaran;
    List registers=null;
    int[] activeEnergyMatrix={6, 1, 4, 5, 11, 12}; //these form the same ObisCodes as for the CVE and ICE meter

    /** Creates a new instance of RegisterFactory */
    public RegisterFactory(Trimaran trimaran) {
        this.trimaran=trimaran;

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

        buildIndexes(trimaran.getDataFactory().getCurrentPeriodTable(),
        		trimaran.getDataFactory().getPreviousPeriodTable());

//        buildIndexes(255,trimaran.getDataFactory().getCurrentMonthInfoTable());
//        buildIndexes(0,trimaran.getDataFactory().getPreviousMonthInfoTable());
    }

    private void buildIndexes(CurrentPeriodTable currentPeriodTable, PreviousPeriodTable previousPeriodTable) {
        Quantity quantity=null;
        RegisterValue registerValue=null;
        Date toDate=null;
        Date fromDate=null;

        // We may not just substract one month, we are not certain the values are closed each month.
//        Date fromDate0 = previousPeriodTable.getTimeStamp();
//        Calendar cal = ProtocolUtils.getCalendar(trimaran.getTimeZone());
//        cal.setTime(fromDate0);
//        cal.add(Calendar.MONTH, -1);
//        Date fromDate1 = cal.getTime();

        for (int eField = 0; eField < 6; eField++) {
        	quantity = currentPeriodTable.getActiveQuantity(eField);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.1.8."+ activeEnergyMatrix[eField] +".255"), quantity);
        	registers.add(new Register(registerValue));
        	quantity = previousPeriodTable.getActiveQuantityP1(eField);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.1.8."+ activeEnergyMatrix[eField] +".0"), quantity,
        			null, currentPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
        	quantity = previousPeriodTable.getActiveQuantityP2(eField);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.1.8."+ activeEnergyMatrix[eField] +".1"), quantity,
        			null, previousPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
        }
        int offset = 17;
        for (int zone = 0; zone <= 3; zone++){
        	// Puissance souscrite - Exceeding power
        	quantity = currentPeriodTable.getExceedingPowerQuantity(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.9.129." + (offset+zone) + ".255"), quantity);
        	registers.add(new Register(registerValue));
        	quantity = previousPeriodTable.getExceedingPowerQuantityP1(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.9.129." + (offset+zone) + ".0"), quantity,
        			null, previousPeriodTable.getTimeStamp(), currentPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
        	quantity = previousPeriodTable.getExceedingPowerQuantityP2(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.9.129." + (offset+zone) + ".1"), quantity,
        			null, previousPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));

        	// Puissance apparente maximale - Maximum Demand
        	quantity = currentPeriodTable.getExceedingPowerQuantity(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.9.6." + (offset+zone) + ".255"), quantity,
        			null, previousPeriodTable.getTimeStamp(), currentPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
        	quantity = previousPeriodTable.getExceedingPowerQuantityP1(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.9.6." + (offset+zone) + ".0"), quantity,
        			null, previousPeriodTable.getTimeStamp(), currentPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
        	quantity = previousPeriodTable.getExceedingPowerQuantityP2(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.9.6." + (offset+zone) + ".1"), quantity,
        			null, previousPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));

        	// Temps de fonctionnement pendant la zone ??
        	quantity = currentPeriodTable.getTarifDurationQuantity(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("0.1.96.8." + (offset+zone) + ".255"), quantity,
        			null, previousPeriodTable.getTimeStamp(), currentPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
        	quantity = previousPeriodTable.getTarifDurationQuantity(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("0.1.96.8." + (offset+zone) + ".0"), quantity,
        			null, previousPeriodTable.getTimeStamp(), currentPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));

        	// Durée totale de dépassement de la puissance souscrite dans la zone tarifaire ??
        	quantity = currentPeriodTable.getDurationExceedingPowerQuantity(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.9.37." + (offset+zone) + ".255"), quantity,
        			null, previousPeriodTable.getTimeStamp(), currentPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
        	quantity = previousPeriodTable.getDurationExceedingPowerQuantityP1(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.9.37." + (offset+zone) + ".0"), quantity,
        			null, previousPeriodTable.getTimeStamp(), currentPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
          	quantity = previousPeriodTable.getDurationExceedingPowerQuantityP2(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.9.37." + (offset+zone) + ".1"), quantity,
        			null, previousPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));

        	// Coefficients de dépassement dans la zone tariaire ???
        	quantity = currentPeriodTable.getCoefficientQuantity(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.9.139." + (offset+zone) + ".255"), quantity);
        	registers.add(new Register(registerValue));
        	quantity = previousPeriodTable.getCoefficientQuantityP1(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.9.139." + (offset+zone) + ".0"), quantity,
        			null, previousPeriodTable.getTimeStamp(), currentPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
          	quantity = previousPeriodTable.getCoefficientQuantityP2(zone);
        	registerValue = new RegisterValue(ObisCode.fromString("1.1.9.139." + (offset+zone) + ".1"), quantity,
        			null, previousPeriodTable.getTimeStamp());
        	registers.add(new Register(registerValue));
        }

	}

}
