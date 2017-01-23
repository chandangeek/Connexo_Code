/*
 * StoredValues.java
 *
 * Created on 10 januari 2008
 */

package com.energyict.protocolimpl.dlms.flex;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.ObjectReference;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
*
* @author  Koen
*
*  <B>@beginchanges</B><BR>
	GN|10012008|Taken over everything from the Iskra protocol and adapted where needed
* @endchanges
*/
public class StoredValuesImpl implements StoredValues {
    final int DEBUG=0;

    private static final int EOB_STATUS=0;

    private static final int RATE1 = 0;
    private static final int RATE2 = 1;
    private static final int PLUS = 2;
    private static final int MINUS = 3;
    private int register = -1;

    Calendar toThisDay;
    Calendar fromThisDay;


    CosemObjectFactory cof;
    ProtocolLink protocolLink;
    ProfileGeneric profileGeneric=null;

    List billingSets=new ArrayList();

    /** Creates a new instance of StoredValues */
    public StoredValuesImpl(CosemObjectFactory cof) {
        this.cof=cof;
        protocolLink = cof.getProtocolLink();
    }

    //***************************************************************************************************
    // implementation of the interfaceStoredValues
    public void retrieve() throws IOException {
    	if (billingSets.size() == 0) {
            // retrieve billingset
            profileGeneric = new ProfileGeneric(protocolLink,cof.getObjectReference(DLMSCOSEMGlobals.HISTORIC_VALUES_OBJECT_LN,protocolLink.getMeterConfig().getHistoricValuesSN()));
            processDataContainer(profileGeneric.getBuffer(fromThisDay, toThisDay));
        }
    }

    public void setDates(int billingIndex){
    	// These are set in the getStoredValues()
    	toThisDay = Calendar.getInstance();
    	fromThisDay = Calendar.getInstance();
    	fromThisDay.add(toThisDay.DATE, -billingIndex);
    }

    public int getBillingPointCounter() throws IOException {
        return ((BillingSet)billingSets.get(0)).getNrOfResets();
    }

    public Date getBillingPointTimeDate(int billingPoint) throws IOException {
        return ((BillingSet)billingSets.get(billingPoint)).getBillingDate();
    }

    public HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException {
        int billingPoint=0;
        int billingIndex = 0;
        Calendar todayDate = Calendar.getInstance();
        todayDate.getTime();

        billingPoint = obisCode.getF();

        billingIndex = billingSets.size() - (billingPoint - 101) - 1;

        if ( billingIndex < 0 )
        	throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");

    	if (obisCode.toString().indexOf("1.1.1.8.0.") != -1){
    		register = PLUS;
    		if (DEBUG == 1) System.out.println("Obiscode: Total active energy IMPORT (+A)");
    	}
    	else if (obisCode.toString().indexOf("1.1.2.8.0.") != -1){
    		register = MINUS;
    		if (DEBUG == 1) System.out.println("Obiscode: Total active energy EXPORT (-A)");
    	}
    	else if (obisCode.toString().indexOf("1.1.1.8.1.") != -1){
    		register = RATE1;
    		if (DEBUG == 1) System.out.println("Obiscode: Active energy IMPORT, rate 1");
    	}
    	else if (obisCode.toString().indexOf("1.1.1.8.2.") != -1){
    		register = RATE2;
    		if (DEBUG == 1) System.out.println("Obiscode: Active energy IMPORT, rate 2");
    	}

        BillingSet billingSet = (BillingSet)billingSets.get(billingIndex);
        HistoricalValue historicalValue = new HistoricalValue();
        historicalValue.setBillingDate(billingSet.getBillingDate());
        BillingValue billingValue = billingSet.giveBillingValue();
        ExtendedRegister extendedRegister = new ExtendedRegister(cof.getProtocolLink(),new ObjectReference(obisCode.getLN()));
        extendedRegister.setValue(new Long(billingValue.getRegisterValue(register)));
        extendedRegister.setScalerUnit(billingValue.getScalerUnit());
        extendedRegister.setCaptureTime(billingValue.getCaptureDateTime());
        historicalValue.setCosemObject(extendedRegister);

        return historicalValue;

    }

    public ProfileGeneric getProfileGeneric() {
        return profileGeneric;
    }


    //********************************************************************************************

    private BillingValue getBillingValues(int billingSetId, DataContainer dc){
    	DataStructure ds = dc.getRoot().getStructure(billingSetId);
    	Date date = null;
    	date = ds.getOctetString(0).toDate(protocolLink.getTimeZone());
    	BillingValue billingValue = new BillingValue(date, (int) ds.getValue(1),
    			(long)ds.getValue(2), (long)ds.getValue(3), (long)ds.getValue(4), (long)ds.getValue(5));
    	return billingValue;
    }

    private BillingSet getBillingSet(int billingSetId,DataContainer dc) {

    	Date billingDate = dc.getRoot().getStructure(billingSetId).getOctetString(EOB_STATUS).toDate(protocolLink.getTimeZone());
    	int daysSinceLastReset = 0;
    	int nrOfResets = 0;
    	int billingReason = 3; //Daily billingpoint
        BillingSet billingSet = new BillingSet(billingDate,billingReason,daysSinceLastReset,nrOfResets);
        if (DEBUG>=1) System.out.println("KV_DEBUG> "+billingSet);
        return billingSet;
    }

    private void processDataContainer(DataContainer dc) {
        billingSets.clear();
        int nrOfBillingSets = dc.getRoot().getNrOfElements();
        if (DEBUG>=1) System.out.println("nrOfBillingSets : "+nrOfBillingSets);
        for (int billingSetId=0;billingSetId<nrOfBillingSets;billingSetId++) {
        	if (DEBUG>=1) System.out.println("************************************************************************************");
            BillingSet billingSet = getBillingSet(billingSetId,dc);
            billingSet.addBillingValue(getBillingValues(billingSetId, dc));
            billingSets.add(billingSet);
        } // for (billingSetId=0;billingSetId<nrOfBillingSets;billingSetId++)
    }

}
