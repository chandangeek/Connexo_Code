/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.dlms.iskrame37x;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.ObjectReference;

import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author gna
 * SVA|29032012|Reworked reead out of billing registers (class StoredValuesImpl) - this class becomes obsolete
 */
public class RegisterProfile {

	/**
	 *
	 */

	List billingSets=new ArrayList();
	CosemObjectFactory cof;
	ProtocolLink protocolLink;
	Calendar billingCalendar = null;

	private int DEBUG = 0;
	private int iInterval = -1;

	private Date firstDate = null;
	private Date billingDate = null;
	private Date fromDate = null;

	private static final int EOB_STATUS=0;

    private ObisCode obisCodeArray[] = {
    		ObisCode.fromString("1.0.1.8.1.255"), // A+ Tariff 1
    		ObisCode.fromString("1.0.1.8.2.255"), // A+ Tariff 2
//    		ObisCode.fromString("1.0.1.8.3.255"), // A+ Tariff 3
//    		ObisCode.fromString("1.0.1.8.4.255"), // A+ Tariff 4
    		ObisCode.fromString("1.0.2.8.1.255"), // A- Tariff 1
    		ObisCode.fromString("1.0.2.8.2.255"), // A- Tariff 2
//    		ObisCode.fromString("1.0.2.8.3.255"), // A+ Tariff 3
//    		ObisCode.fromString("1.0.2.8.4.255"), // A+ Tariff 4
    		ObisCode.fromString("0.1.128.50.0.255")}; // MBus

    private ObisCode obisCodeArray1[] = {
    		ObisCode.fromString("1.0.1.8.1.VZ"), // A+ Tariff 1
    		ObisCode.fromString("1.0.1.8.2.VZ"), // A+ Tariff 2
//    		ObisCode.fromString("1.0.1.8.3.VZ"), // A+ Tariff 3
//    		ObisCode.fromString("1.0.1.8.4.VZ"), // A+ Tariff 4
    		ObisCode.fromString("1.0.2.8.1.VZ"), // A- Tariff 1
    		ObisCode.fromString("1.0.2.8.2.VZ"), // A- Tariff 2
//    		ObisCode.fromString("1.0.2.8.3.VZ"), // A+ Tariff 3
//    		ObisCode.fromString("1.0.2.8.4.VZ"), // A+ Tariff 4
    		ObisCode.fromString("0.1.128.50.0.VZ")}; // MBus

    private ObisCode obisCodeArray2[] = {
    		ObisCode.fromString("1.0.1.8.1.VZ-1"), // A+ Tariff 1
    		ObisCode.fromString("1.0.1.8.2.VZ-1"), // A+ Tariff 2
//    		ObisCode.fromString("1.0.1.8.3.VZ-1"), // A+ Tariff 3
//    		ObisCode.fromString("1.0.1.8.4.VZ-1"), // A+ Tariff 4
    		ObisCode.fromString("1.0.2.8.1.VZ-1"), // A- Tariff 1
    		ObisCode.fromString("1.0.2.8.2.VZ-1"), // A- Tariff 2
//    		ObisCode.fromString("1.0.2.8.3.VZ-1"), // A+ Tariff 3
//    		ObisCode.fromString("1.0.2.8.4.VZ-1"), // A+ Tariff 4
    		ObisCode.fromString("0.1.128.50.0.VZ-1")}; // MBus

	public RegisterProfile() {
		// TODO Auto-generated constructor stub
	}

	public void getProfileBuffer(ObisCode dailyObisCode) throws IOException {
		if (billingSets.size() == 0) {
			processDataContainer(cof.getProfileGeneric(dailyObisCode).getBuffer());
		}
	}

	private void processDataContainer(DataContainer buffer) {

        billingSets.clear();
        int nrOfBillingSets = buffer.getRoot().getNrOfElements();
        if (DEBUG>=1) System.out.println("nrOfBillingSets : "+nrOfBillingSets);
        for (int billingSetId=0;billingSetId<nrOfBillingSets;billingSetId++) {
        	if (DEBUG>=1) System.out.println("************************************************************************************");
            BillingSet billingSet = getBillingSet(billingSetId,buffer);
            for ( int i = 1; i < buffer.getRoot().getStructure(billingSetId).getElements().length; i++ ){
            	billingSet.addBillingValue(getBillingValues(billingSetId, buffer, i, obisCodeArray[i-1]));
            }
            billingSets.add(billingSet);
        }

	}

	private BillingValue getBillingValues(int billingSetId, DataContainer buffer, int item, ObisCode obisCode) {
		DataStructure ds = buffer.getRoot().getStructure(billingSetId);
//		Date date = null;
//		date = ds.getOctetString(0).toDate(protocolLink.getTimeZone());

		ScalerUnit sUnit = new ScalerUnit(0, 30); //IskraME37X.getScalerUnit(obisCode);
		BillingValue billingValue = new BillingValue(billingDate, (long)ds.getValue(item), sUnit, obisCode);

		return billingValue;
	}

	private BillingSet getBillingSet(int billingSetId, DataContainer buffer) {

		int billingReason;
		billingDate = null;
		Calendar cal = Calendar.getInstance( protocolLink.getTimeZone() );

		if ( firstDate == null ) {
			firstDate = buffer.getRoot().getStructure(billingSetId).getOctetString(EOB_STATUS).toDate(protocolLink.getTimeZone());
		}
		else if ( buffer.getRoot().getStructure(billingSetId).isOctetString(0) ){
			firstDate = buffer.getRoot().getStructure(billingSetId).getOctetString(EOB_STATUS).toDate(protocolLink.getTimeZone());
			billingCalendar = null;
		}



		if ( iInterval != -1 ){ // for the daily an intervaltime is used ...

			if ( billingCalendar == null ){
				billingCalendar = Calendar.getInstance( protocolLink.getTimeZone() );
				billingCalendar.setTime(firstDate);

        		cal.setTime(billingCalendar.getTime());

        		if ( (cal.get(Calendar.SECOND) == 0) && (cal.get(Calendar.MINUTE) == 0) && (cal.get(Calendar.HOUR_OF_DAY) == 0)) {
			        cal.add(Calendar.DATE, -1);
		        }
		        else {
			        cal.set(Calendar.HOUR_OF_DAY, 0);
		        }
				cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);

        		fromDate = cal.getTime();

			}
			else{
				fromDate = billingCalendar.getTime();;
				billingCalendar.add(Calendar.SECOND, iInterval);
			}

			billingDate = billingCalendar.getTime();
			billingReason = 3; //Daily billingPoint
		}

		else { // for the monthly no intervaltime is used
			billingDate = buffer.getRoot().getStructure(billingSetId).getOctetString(EOB_STATUS).toDate(protocolLink.getTimeZone());

			if (billingSetId != 0) {
				fromDate = buffer.getRoot().getStructure(billingSetId - 1).getOctetString(EOB_STATUS).toDate(protocolLink.getTimeZone());
			}
			else{
        		cal.setTime(billingDate);
        		cal.set(Calendar.DATE, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
        		fromDate = cal.getTime();
			}

			billingReason = 4; //Monthly billingPoint
		}

    	int daysSinceLastReset = 0;
    	int nrOfResets = 0;
        BillingSet billingSet = new BillingSet(billingDate,billingReason,daysSinceLastReset,nrOfResets);
        if (DEBUG>=1) {
	        System.out.println("KV_DEBUG> " + billingSet);
        }
        return billingSet;
	}

	public void setCosemObjectFactory(CosemObjectFactory cof) {
		this.cof = cof;
		protocolLink = cof.getProtocolLink();
	}

	public CosemObject getValues(ObisCode obisCode) throws IOException {

        int billingElement = -1;
        billingElement = getElement(obisCode);

        HistoricalValue historicalValue = new HistoricalValue();

        if (billingSets.size() <= 0) {
        	throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
        }

        BillingSet billingSet = (BillingSet)billingSets.get(billingSets.size()-1);
        historicalValue.setBillingDate(billingSet.getBillingDate());

        //***********************************************************************************
        // Need to change this?
        BillingValue billingValue = (BillingValue) billingSet.getBillingValues().get(billingElement);
        //***********************************************************************************

        ExtendedRegister extendedRegister = new ExtendedRegister(cof.getProtocolLink(),new ObjectReference(obisCode.getLN()));
        extendedRegister.setValue(new Long(billingValue.getValue()));
        extendedRegister.setScalerUnit(billingValue.getScalerUnit());
        extendedRegister.setCaptureTime(billingValue.getCaptureDateTime());
        historicalValue.setCosemObject(extendedRegister);

        return historicalValue;

	}

	private int getElement(ObisCode obisCode) throws NoSuchObjectException {
		for (int i = 0; i < obisCodeArray.length; i++){

			if (obisCode.getF() == 0){
				if ( obisCode.equals(obisCodeArray1[i]) ) {
					return i;
				}
			}
			else if (obisCode.getF() == -1){
				if ( obisCode.equals(obisCodeArray2[i]) ) {
					return i;
				}
			}

		}
		throw new NoSuchObjectException(null);
	}

	public void getInterval(ObisCode genericProfileObisCode) throws IOException {
		iInterval = cof.getProfileGeneric(genericProfileObisCode).getCapturePeriod();
	}

	public Date getFromDate() {
		return fromDate;
	}

}
