/*
 * StoredValues.java
 *
 * Created on 12 oktober 2004, 13:08
 */

package com.energyict.protocolimpl.dlms.eictz3;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author  Koen
 */
final class StoredValuesImpl implements StoredValues {
    
    private static final int EOB_STATUS=0;
    private static final int TOTAL_ENERGY=1;
    private static final int ENERGY_RATES=2;
    private static final int MAXIMUM_DEMANDS=3;
    private static final int MD_RANGE=24; // 48 entries. ObisCode followed by struct, 24 times = 48 entries in the datacontainer
    
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
        
        // For the SL7000, we delay the retrieval until we know about which billingpoint shout be retrieved...
        //profileGeneric = new ProfileGeneric(protocolLink,cof.getObjectReference(cof.HISTORIC_VALUES_OBJECT_LN,protocolLink.getMeterConfig().getHistoricValuesSN()));
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
        
        if (obisCode.getF() >= 101)
            billingPoint = obisCode.getF()- 101;
        else if ((obisCode.getF()  >=0) && (obisCode.getF() <= 99))
            billingPoint = obisCode.getF();
        else if ((obisCode.getF()  <=0) && (obisCode.getF() >= -99))
            billingPoint = obisCode.getF()*-1;
        
        if ((billingPoint+1) > billingSets.size()) {
            byte[] ln = DLMSCOSEMGlobals.HISTORIC_VALUES_OBJECT_LN;
            ln[5] = (byte)(101+billingPoint);
            // retrieve billingset
            profileGeneric = new ProfileGeneric(protocolLink,cof.getObjectReference(ln,protocolLink.getMeterConfig().getHistoricValuesSN()));
            DataContainer dc= profileGeneric.getBuffer();
            processDataContainer(dc);
        }
        
        BillingSet billingSet = (BillingSet)billingSets.get(billingPoint);
        HistoricalValue historicalValue = new HistoricalValue();
        historicalValue.setBillingDate(billingSet.getBillingDate());
        ObisCode obisCodeNoBilling=new ObisCode(obisCode.getA(),obisCode.getB(),obisCode.getC(),obisCode.getD(),obisCode.getE(),255);
        BillingValue billingValue = billingSet.find(obisCodeNoBilling);
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
        return billingValue;
    } // private List getBillingValue(int billingSetId)
    
    private BillingSet getBillingSet(int billingSetId,DataContainer dc) {
        int daysSinceLastReset = dc.getRoot().getStructure(billingSetId).getStructure(EOB_STATUS).getInteger(1);
        int nrOfResets= dc.getRoot().getStructure(billingSetId).getStructure(EOB_STATUS).getStructure(3).getInteger(0);
        int billingReason = dc.getRoot().getStructure(billingSetId).getStructure(EOB_STATUS).getStructure(3).getInteger(1);
        Date billingDate = dc.getRoot().getStructure(billingSetId).getStructure(EOB_STATUS).getOctetString(4).toUTCDate();
        BillingSet billingSet = new BillingSet(billingDate,billingReason,daysSinceLastReset,nrOfResets);
        return billingSet;
    }
    
    private void processDataContainer(DataContainer dc) {
        billingSets.clear();
        int nrOfBillingSets = dc.getRoot().getNrOfElements();
        for (int billingSetId=0;billingSetId<nrOfBillingSets;billingSetId++) {          
            BillingSet billingSet = getBillingSet(billingSetId,dc);
            billingSet.addBillingValues(getBillingValues(billingSetId,TOTAL_ENERGY,dc)); // total energy
            billingSet.addBillingValues(getBillingValues(billingSetId,ENERGY_RATES,dc)); // energy rates
            billingSet.addBillingValues(getAllMaximumDemands(billingSetId,dc)); // maximum demands
            billingSet.addBillingValue(getBillingValue(billingSetId,52,dc)); // minimum PF
            billingSet.addBillingValue(getBillingValue(billingSetId,53,dc)); // average PF
            billingSet.addBillingValue(getBillingValue(billingSetId,54,dc)); // minimum frequency
            billingSet.addBillingValue(getBillingValue(billingSetId,55,dc)); // maximum frequency
            billingSet.addBillingValues(getBillingValues(billingSetId,56,dc)); // Maximum RMS values
            billingSet.addBillingValue(getBillingValue(billingSetId,57,dc)); // minimum temperature
            billingSet.addBillingValue(getBillingValue(billingSetId,58,dc)); // maximum temperature
            billingSet.addBillingValue(getBillingValue(billingSetId,59,dc)); // import active power aggregate
            billingSet.addBillingValue(getBillingValue(billingSetId,60,dc)); // export active power aggregate
            billingSet.addBillingValue(getBillingValue(billingSetId,61,dc)); // import reactive power aggregate
            billingSet.addBillingValue(getBillingValue(billingSetId,62,dc)); // export reactive power aggregate
            billingSet.addBillingValues(getBillingValues(billingSetId,63,dc)); // excess demand
            billingSets.add(billingSet);
        } // for (billingSetId=0;billingSetId<nrOfBillingSets;billingSetId++)
    } // private void processDataContainer(DataContainer dc)
    
    
//    private void parseDataContainerDebug(DataContainer dc) {
//        int[] count=new int[dc.getMaxLevel()+1];
//        int level=0;
//        boolean ret=true;
//        DataStructure root = dc.getRoot();
//        
//        while(true) {
//            int i;
//            for (i=count[level];i<root.getNrOfElements();i++) {
//                if (root.isStructure(i)) {
//                    root=root.getStructure(i); 
//                    count[level]=i+1;
//                    level++;
//                    System.out.println("level "+level+", elements : "+root.getNrOfElements());
//                    i=0;
//                    break;
//                }
//                else {
//                    if (root.isOctetString(i)) {
//                        System.out.print("NumberOfElements "+i+" = ");
//                        ProtocolUtils.printResponseDataFormatted(root.getOctetString(i).getArray());
//                    }
//                    else
//                        System.out.println("NumberOfElements "+i+" = "+root.getElement(i));
//                }
//            } // for (i=count[level];i<root.getNrOfElements();i++)
//            
//            if (i == (root.getNrOfElements())) {
//                root=root.getParent();
//                count[level]=0;
//                level--;
//                if (root == null) {
//                    System.out.println("****************** RETURN ******************");
//                    //ret=false;
//                    break;
//                }
//            } // if (i == (root.getNrOfElements()))
//        } // while(true)
//    } // private void parseDataContainerDebug()
//    
//    
//    static public DataContainer getDataContainerDebug() {
//        DataContainer dc=null;
//        try {
//            File file = new File("datacontainer.bin");
//            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
//            dc = (DataContainer)ois.readObject();
//            ois.close();
//        }
//        catch(Exception e) {
//            e.printStackTrace();
//        }
//        return dc;
//    }
    
    /**
     * @param args the command line arguments
     */
//    public static void main(String[] args) {
//        // TODO code application logic here
//        StoredValuesImpl dcp = new StoredValuesImpl(StoredValuesImpl.getDataContainerDebug());
//    }
    
}
