/*
 * StoredValues.java
 *
 * Created on 12 oktober 2004, 13:08
 */

package com.energyict.protocolimpl.dlms.iskrame37x;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.ObjectReference;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocolimpl.dlms.CapturedObjects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 *
 * @author  Koen
 */
public class StoredValuesImpl implements StoredValues {
    final int DEBUG=1;
    
    private static final int EOB_STATUS=0;
    private static final int TOTAL_ENERGY=1;
    private static final int ENERGY_RATES=2;
    private static final int MAXIMUM_DEMANDS=3;
    private static final int MD_RANGE=24; // 48 entries. ObisCode followed by struct, 24 times = 48 entries in the datacontainer
    private static final int CUMULATIVE_DEMAND=51; // unised for the moment...
    
    private static byte[] iskraHistoricalValuesP1 = new byte[]{1,0,(byte)98,1,0,(byte) -1};
    private static byte[] iskraHistoricalValuesP2 = new byte[]{1,0,(byte)98,2,0,(byte) -1};
    private byte[] iskraProfile;
    
    private ObisCode obisCodeArray[] = {
    		ObisCode.fromString("1.0.1.8.1.255"), // A+ Tariff 1
    		ObisCode.fromString("1.0.1.8.2.255"), // A+ Tariff 2
    		ObisCode.fromString("1.0.2.8.1.255"), // A- Tariff 1
    		ObisCode.fromString("1.0.2.8.2.255"), // A- Tariff 2
    		ObisCode.fromString("0.1.128.50.0.255")}; // MBus
    
    Calendar toThisDay;
    Calendar fromThisDay;
    
    CosemObjectFactory cof;
    ProtocolLink protocolLink;
    ProfileGeneric profileGeneric=null;
    CapturedObjects capturedObjectsBillings=null;
    
    List billingSets=new ArrayList();
    
    /** Creates a new instance of StoredValues */
    public StoredValuesImpl(CosemObjectFactory cof) {
        this.cof=cof;
        protocolLink = cof.getProtocolLink();
    }
    
    //***************************************************************************************************
    // implementation of the interfaceStoredValues
    public void retrieve() throws IOException {
        
        // For the SL7000, we delay the retrieval until we know about which billingpoint shout be retrieved...
        //profileGeneric = new ProfileGeneric(protocolLink,cof.getObjectReference(cof.HISTORIC_VALUES_OBJECT_LN,protocolLink.getMeterConfig().getHistoricValuesSN()));
    	
    	if (billingSets.size() == 0) {
            // retrieve billingset      	
            profileGeneric = new ProfileGeneric(protocolLink,cof.getObjectReference(iskraProfile,protocolLink.getMeterConfig().getHistoricValuesSN()));
            processDataContainer(profileGeneric.getBuffer(fromThisDay, toThisDay));
        }
    }    
    
    public void setDates(int billingIndex){
    	// These are set in the getStoredValues()
//    	toThisDay = Calendar.getInstance();       	
//    	fromThisDay = Calendar.getInstance();
//    	fromThisDay.add(toThisDay.DATE, -billingIndex);
    	if (billingIndex == 0)
    		iskraProfile = iskraHistoricalValuesP1;
    	else if (billingIndex == -1)
    		iskraProfile = iskraHistoricalValuesP2;
		else
			try {
				throw new  NoSuchRegisterException("Only daily(0) and monthly(-1) billings allowed!");
			} catch (NoSuchRegisterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	toThisDay = null;
    	fromThisDay = null;
    }

    public int getBillingPointCounter() throws IOException {   
        if (billingSets.size() == 0) {
            // retrieve billingset
            profileGeneric = new ProfileGeneric(protocolLink,cof.getObjectReference(DLMSCOSEMGlobals.HISTORIC_VALUES_OBJECT_LN,protocolLink.getMeterConfig().getHistoricValuesSN()));
            processDataContainer(profileGeneric.getBuffer());
        }
        return ((BillingSet)billingSets.get(0)).getNrOfResets();
    }
    
    public Date getBillingPointTimeDate(int billingPoint) throws IOException {
        // did we retrieve the billingset?
        if ((billingPoint+1) > billingSets.size()) {
            // retrieve billingset
            byte[] ln = DLMSCOSEMGlobals.HISTORIC_VALUES_OBJECT_LN;
            ln[5] = (byte)(101+billingPoint);
            profileGeneric = new ProfileGeneric(protocolLink,cof.getObjectReference(ln,protocolLink.getMeterConfig().getHistoricValuesSN()));
            processDataContainer(profileGeneric.getBuffer());
        }
        return ((BillingSet)billingSets.get(billingPoint)).getBillingDate();
    }
    
    public HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException {
        // Do we have to retrieve the billingset?
        int billingPoint=0;
        int billingElement = -1;
//        
//        if (obisCode.getF() >= 101)
//            billingPoint = obisCode.getF()- 101;
//        else if ((obisCode.getF()  >=0) && (obisCode.getF() <= 99))
//            billingPoint = obisCode.getF();
//        else if ((obisCode.getF()  <=0) && (obisCode.getF() >= -99))
//            billingPoint = obisCode.getF()*-1;
//        
//        if ((billingPoint+1) > billingSets.size()) {
//            byte[] ln = cof.HISTORIC_VALUES_OBJECT_LN;
//            ln[5] = (byte)(101+billingPoint);
//            // retrieve billingset
//            profileGeneric = new ProfileGeneric(protocolLink,cof.getObjectReference(ln,protocolLink.getMeterConfig().getHistoricValuesSN()));
//            DataContainer dc= profileGeneric.getBuffer();
//            processDataContainer(dc);
//        }
        
        
        if( (obisCode.toString().indexOf("1.0.1.8.1.VZ")) == 0 ){
        	if(DEBUG == 1)System.out.println("Daily billingPoint 1.0.1.8.1.255");
        	billingElement = 0;
        }
        
        else if( (obisCode.toString().indexOf("1.0.1.8.1.VZ-1 ")) == 0 ){
        	if(DEBUG == 1)System.out.println("Daily billingPoint 1.0.1.8.1.255");
        	billingElement = 0;
        }
        
        
        
//        BillingSet billingSet = (BillingSet)billingSets.get(billingPoint);
//        HistoricalValue historicalValue = new HistoricalValue();
//        historicalValue.setBillingDate(billingSet.getBillingDate());
//        ObisCode obisCodeNoBilling=new ObisCode(obisCode.getA(),obisCode.getB(),obisCode.getC(),obisCode.getD(),obisCode.getE(),255);
////        BillingValue billingValue = billingSet.find(obisCodeNoBilling);
//        BillingValue billingValue = billingSet.getBillingValue(0);
//        ExtendedRegister extendedRegister = new ExtendedRegister(cof.getProtocolLink(),new ObjectReference(obisCode.getLN()));
//        extendedRegister.setValue(new Long(billingValue.getValue()));
//        extendedRegister.setScalerUnit(billingValue.getScalerUnit());
//        extendedRegister.setCaptureTime(billingValue.getCaptureDateTime());
//        historicalValue.setCosemObject(extendedRegister);
        
        BillingSet billingSet = (BillingSet)billingSets.get(billingPoint);
        HistoricalValue historicalValue = new HistoricalValue();
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

    
    public ProfileGeneric getProfileGeneric() {
        return profileGeneric;
    }
    
    
    //********************************************************************************************
    // Private methods to parse the datacontainer
    private List getAllMaximumDemands(int billingSetId,DataContainer dc) {
        List billingValues = new ArrayList();
        for(int id=MAXIMUM_DEMANDS;id<(MAXIMUM_DEMANDS+MD_RANGE*2);id+=2) {
            ObisCode maximumDemandObisCode = dc.getRoot().getStructure(billingSetId).getOctetString(id).toObisCode();
            if (dc.getRoot().getStructure(billingSetId).getStructure(id+1).isStructure(0)) {
                DataStructure ds = dc.getRoot().getStructure(billingSetId).getStructure(id+1).getStructure(0);
                long value = ds.getValue(0);
                ScalerUnit scalerUnit = new ScalerUnit(ds.getStructure(1).getInteger(0),
                                                       ds.getStructure(1).getInteger(1));
                Date date = ds.getOctetString(2).toUTCDate();
                BillingValue billingValue = new BillingValue(date,value,scalerUnit,maximumDemandObisCode);
                billingValues.add(billingValue);
                if (DEBUG>=1) System.out.println("KV_DEBUG> "+billingValue);
            }
        } // for(int id=0;id<entries;id++)
        return billingValues;
    } // private List getAllMaximumDemands(int billingSetId)
   
    private List getBillingValues(int billingSetId,int typeId,DataContainer dc) {
        List billingValues = new ArrayList();
        DataStructure ds = dc.getRoot().getStructure(billingSetId).getStructure(typeId).getStructure(0);
        int entries = ds.getNrOfElements();
        for(int id=0;id<entries;id++) {
            ObisCode obisCode = ds.getStructure(id).getOctetString(0).toObisCode();
            long value = ds.getStructure(id).getValue(1);
            ScalerUnit scalerUnit = new ScalerUnit(ds.getStructure(id).getStructure(2).getInteger(0),
                                                   ds.getStructure(id).getStructure(2).getInteger(1));
            Date date=null;
            if (ds.getStructure(id).getNrOfElements() > 3) {
               if (ds.getStructure(id).isOctetString(4))
                   date = ds.getStructure(id).getOctetString(4).toUTCDate();
            }
            BillingValue billingValue = new BillingValue(date,value,scalerUnit,obisCode);
            billingValues.add(billingValue);
            if (DEBUG>=1) System.out.println("KV_DEBUG> "+billingValue);
        } // for(int id=0;id<entries;id++)
        return billingValues;
    } // private List getTotalEnergy(int billingSetId)
    
    private BillingValue getBillingValue(int billingSetId,int typeId,DataContainer dc) {
        DataStructure ds = dc.getRoot().getStructure(billingSetId).getStructure(typeId);
        int entries = ds.getNrOfElements();
        ObisCode obisCode = ds.getOctetString(0).toObisCode();
        long value = ds.getValue(1);
        ScalerUnit scalerUnit = new ScalerUnit(ds.getStructure(2).getInteger(0),
                                               ds.getStructure(2).getInteger(1));
        Date date=null;
        if (ds.getNrOfElements() > 3)
           date = ds.getOctetString(4).toUTCDate();
        BillingValue billingValue = new BillingValue(date,value,scalerUnit,obisCode);
        if (DEBUG>=1) System.out.println("KV_DEBUG> "+billingValue);
        return billingValue;
    } // private List getBillingValue(int billingSetId)
    
    
    private BillingValue getBillingValues(int billingSetId, DataContainer dc){
    	DataStructure ds = dc.getRoot().getStructure(billingSetId);
    	Date date = null;
    	date = ds.getOctetString(0).toDate(protocolLink.getTimeZone());
    	BillingValue billingValue = new BillingValue(date, (int) ds.getValue(1), 
    			(long)ds.getValue(2), (long)ds.getValue(3), (long)ds.getValue(4), (long)ds.getValue(5));
    	return billingValue;
    }
    
	private BillingValue getBillingValues(int billingSetId, DataContainer dc, int item, ObisCode obisCode){
		DataStructure ds = dc.getRoot().getStructure(billingSetId);
		Date date = null;
		date = ds.getOctetString(0).toDate(protocolLink.getTimeZone());
		ScalerUnit sUnit = new ScalerUnit(0,30);
		BillingValue billingValue = new BillingValue(date, (long)ds.getValue(item), sUnit, obisCode);
			
		return billingValue;
//		return null;
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
            for ( int i = 1; i < dc.getRoot().getStructure(billingSetId).getElements().length; i++ ){
            	billingSet.addBillingValue(getBillingValues(billingSetId, dc, i-1, obisCodeArray[i-1]));
            }
            billingSets.add(billingSet);            
        } // for (billingSetId=0;billingSetId<nrOfBillingSets;billingSetId++)
    } // private void processDataContainer(DataContainer dc)


    
    /**
     * @param args the command line arguments
     */
//    public static void main(String[] args) {
//        // TODO code application logic here
//        StoredValuesImpl dcp = new StoredValuesImpl(StoredValuesImpl.getDataContainerDebug());
//    }
    
}
