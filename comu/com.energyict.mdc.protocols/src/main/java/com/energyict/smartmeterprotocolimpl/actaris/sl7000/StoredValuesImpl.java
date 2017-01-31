/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.actaris.sl7000;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StoredValuesImpl implements StoredValues {

    private static final ObisCode MAXIMUM_DEMAND_BASE_OBIS = ObisCode.fromString("1.1.0.6.0.255");
    private static ObisCode OBIS_NUMBER_OF_AVAILABLE_HISTORICAL_SETS = ObisCode.fromString("0.0.0.1.1.255");

    private static final int EOB_STATUS=0;
    private static final int TOTAL_ENERGY=1;
    private static final int ENERGY_RATES=2;
    private static final int MAXIMUM_DEMANDS=3;
    private static final int MD_RANGE=24; // 48 entries. ObisCode followed by struct, 24 times = 48 entries in the datacontainer
    private static final int CUMULATIVE_DEMAND=51; // unused for the moment...


    private CosemObjectFactory cof;
    private ProtocolLink protocolLink;
    private ActarisSl7000 meterProtocol;
    private ProfileGeneric profileGeneric=null;
    private MaximumDemandRegisterProfileMapper maximumDemandRegisterProfileMapper;

    List billingSets=new ArrayList();

    /** Creates a new instance of StoredValues */
    public StoredValuesImpl(ActarisSl7000 meterProtocol) {
        this.meterProtocol = meterProtocol;
        this.cof= meterProtocol.getDlmsSession().getCosemObjectFactory();
        protocolLink = cof.getProtocolLink();
    }

    public void retrieve() throws IOException {
        // For the SL7000, we delay the retrieval until we know which billing point should be retrieved...
    }

    public int getBillingPointCounter() throws IOException {
        com.energyict.dlms.cosem.Register cosmeRegister = cof.getRegister(OBIS_NUMBER_OF_AVAILABLE_HISTORICAL_SETS);
        return (int) cosmeRegister.getValue();
    }

    public Date getBillingPointTimeDate(int billingPoint) throws IOException {
        if ((billingPoint+1) > billingSets.size()) {    // BillingSet for the billingPoint is not yet retrieved
            // retrieve billingSet
            byte[] ln = DLMSCOSEMGlobals.HISTORIC_VALUES_OBJECT_LN;
            ln[5] = (byte)(101+billingPoint);
            profileGeneric = new ProfileGeneric(protocolLink,cof.getObjectReference(ln,protocolLink.getMeterConfig().getHistoricValuesSN()));
            processDataContainer(profileGeneric.getBuffer());
        }
        return ((BillingSet)billingSets.get(billingPoint)).getBillingDate();
    }

    public HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException {
        int billingPoint= Math.abs(obisCode.getF());

        if ((billingPoint+1) > billingSets.size()) {    // BillingSet for the billingPoint is not yet retrieved
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

        ObisCode registerObisCode= ProtocolTools.setObisCodeField(obisCode, 5, (byte) 0xFF);
        if (isMaximumDemandRegister(registerObisCode)) {
            ObisCode profileGenericForRegister = getMaximumDemandRegisterProfileMapper().getProfileGenericForRegister(registerObisCode);
            registerObisCode =
                    profileGenericForRegister != null
                            ? profileGenericForRegister
                            : registerObisCode; // If no profile generic could be found, then fall back to original obis
        }

        BillingValue billingValue = billingSet.find(registerObisCode);
        ExtendedRegister extendedRegister = cof.getExtendedRegister(obisCode);
        extendedRegister.setValue(billingValue.getValue());
        extendedRegister.setScalerUnit(billingValue.getScalerUnit());
        extendedRegister.setCaptureTime(billingValue.getEventDateTime());
        historicalValue.setCosemObject(extendedRegister);

        return historicalValue;
    }

    private boolean isMaximumDemandRegister(ObisCode registerObisCode) {
        ObisCode obis = ProtocolTools.setObisCodeField(registerObisCode, 2, (byte) 0);
        obis = ProtocolTools.setObisCodeField(obis, 4, (byte) 0);
        return obis.equals(MAXIMUM_DEMAND_BASE_OBIS);
    }

    public ProfileGeneric getProfileGeneric() {
        return profileGeneric;
    }

    //********************************************************************************************
    // Private methods to parse the datacontainer
    private void processDataContainer(DataContainer dc) {
        billingSets.clear();
        int nrOfBillingSets = dc.getRoot().getNrOfElements();
        for (int billingSetId = 0; billingSetId < nrOfBillingSets; billingSetId++) {
            int index = TOTAL_ENERGY;
            BillingSet billingSet = createNewBillingSet(billingSetId, dc);
            billingSet.addBillingValues(getBillingValues(billingSetId, index++, dc)); // total energy
            billingSet.addBillingValues(getBillingValues(billingSetId, index++, dc)); // energy rates
            billingSet.addBillingValues(getAllMaximumDemands(billingSetId, index, dc)); // maximum demands
//            billingSet.addBillingValues(getAllCumulativeDemands(billingSetId, CUMULATIVE_DEMAND, dc)); // cumulative demand - at the moment not used
            index = 52;
            billingSet.addBillingValue(getBillingValue(billingSetId, index++, dc)); // minimum PF
            billingSet.addBillingValue(getAveragePF(billingSetId, index, dc)); // average PF        //Seems to be a bug: the average PF is not inside a structure, but occupies position 53, 54, 55
            index += (dc.getRoot().getStructure(billingSetId).isOctetString(index)) ? 3 : 1;        // First we will test if the bug is present & handle different (getAveragePF method)
            billingSet.addBillingValue(getBillingValue(billingSetId, index++, dc)); // minimum frequency
            billingSet.addBillingValue(getBillingValue(billingSetId, index++, dc)); // maximum frequency
            billingSet.addBillingValues(getBillingValues(billingSetId, index++, dc)); // Maximum RMS values
            billingSet.addBillingValue(getBillingValue(billingSetId, index++, dc)); // minimum temperature
            billingSet.addBillingValue(getBillingValue(billingSetId, index++, dc)); // maximum temperature
            billingSet.addBillingValue(getBillingValue(billingSetId, index++, dc)); // import active power aggregate
            billingSet.addBillingValue(getBillingValue(billingSetId, index++, dc)); // export active power aggregate
            billingSet.addBillingValue(getBillingValue(billingSetId, index++, dc)); // import reactive power aggregate
            billingSet.addBillingValue(getBillingValue(billingSetId, index++, dc)); // export reactive power aggregate
            billingSet.addBillingValues(getBillingValues(billingSetId, index++, dc)); // excess demand
            billingSets.add(billingSet);
        }
    }

    private BillingSet createNewBillingSet(int billingSetId, DataContainer dc) {
        int daysSinceLastReset = dc.getRoot().getStructure(billingSetId).getStructure(EOB_STATUS).getInteger(1);
        int nrOfResets = dc.getRoot().getStructure(billingSetId).getStructure(EOB_STATUS).getStructure(3).getInteger(0);
        int billingReason = dc.getRoot().getStructure(billingSetId).getStructure(EOB_STATUS).getStructure(3).getInteger(1);
        Date billingDate = dc.getRoot().getStructure(billingSetId).getStructure(EOB_STATUS).getOctetString(4).toDate(cof.getProtocolLink().getTimeZone());
        BillingSet billingSet = new BillingSet(billingDate, billingReason, daysSinceLastReset, nrOfResets);
        return billingSet;
    }

    private List getBillingValues(int billingSetId,int typeId,DataContainer dc) {
           List billingValues = new ArrayList();

           DataStructure billingStructure = dc.getRoot().getStructure(billingSetId);
           if (!billingStructure.isStructure(typeId)) return billingValues;
           DataStructure typeIdStructure = billingStructure.getStructure(typeId);
           if (!typeIdStructure.isStructure(0)) return billingValues;

           DataStructure ds = typeIdStructure.getStructure(0);
           int entries = ds.getNrOfElements();
           for(int id=0;id<entries;id++) {
               if (!ds.isStructure(id)) continue;
               if (!ds.getStructure(id).isOctetString(0)) continue;
               ObisCode obisCode = ds.getStructure(id).getOctetString(0).toObisCode();
               long value = ds.getStructure(id).getValue(1);
               ScalerUnit scalerUnit = new ScalerUnit(ds.getStructure(id).getStructure(2).getInteger(0),
                                                      ds.getStructure(id).getStructure(2).getInteger(1));
               Date date=null;
               if (ds.getStructure(id).getNrOfElements() > 3) {
                  if (ds.getStructure(id).isOctetString(4))
                      date = ds.getStructure(id).getOctetString(4).toDate(cof.getProtocolLink().getTimeZone());
               }
               BillingValue billingValue = new BillingValue(date,value,scalerUnit,obisCode);
               billingValues.add(billingValue);
           }
           return billingValues;
       }

    private BillingValue getBillingValue(int billingSetId, int typeId, DataContainer dc) {
        DataStructure billingSetStructure = dc.getRoot().getStructure(billingSetId);
        if (!billingSetStructure.isStructure(typeId)) {
            return null;
        }
        DataStructure ds = billingSetStructure.getStructure(typeId);
        if (!ds.isOctetString(0)) {
            return null;
        }
        ObisCode obisCode = ds.getOctetString(0).toObisCode();
        long value = ds.getValue(1);
        ScalerUnit scalerUnit = new ScalerUnit(ds.getStructure(2).getInteger(0),
                ds.getStructure(2).getInteger(1));
        Date date = null;
        if (ds.getNrOfElements() > 3) {
            date = ds.getOctetString(4).toDate(cof.getProtocolLink().getTimeZone());
        }
        BillingValue billingValue = new BillingValue(date, value, scalerUnit, obisCode);
        return billingValue;
    }

    private BillingValue getAveragePF(int billingSetId, int typeId, DataContainer dc) {
        DataStructure billingSetStructure = dc.getRoot().getStructure(billingSetId);
        if (!billingSetStructure.isStructure(typeId)) {
            if (!billingSetStructure.isOctetString(typeId)) {
                return null;
            } else {
                ObisCode obisCode = billingSetStructure.getOctetString(typeId).toObisCode();
                long value = billingSetStructure.getValue(typeId + 1);
                ScalerUnit scalerUnit = new ScalerUnit(billingSetStructure.getStructure(typeId + 2).getInteger(0),
                        billingSetStructure.getStructure(typeId + 2).getInteger(1));
                BillingValue billingValue = new BillingValue(null, value, scalerUnit, obisCode);
                return billingValue;
            }
        }
        DataStructure ds = billingSetStructure.getStructure(typeId);
        if (!ds.isOctetString(0)) {
            return null;
        }
        ObisCode obisCode = ds.getOctetString(0).toObisCode();
        long value = ds.getValue(1);
        ScalerUnit scalerUnit = new ScalerUnit(ds.getStructure(2).getInteger(0),
                ds.getStructure(2).getInteger(1));
        Date date = null;
        if (ds.getNrOfElements() > 3) {
            date = ds.getOctetString(4).toDate(cof.getProtocolLink().getTimeZone());
        }
        BillingValue billingValue = new BillingValue(date, value, scalerUnit, obisCode);
        return billingValue;
    }

    private List getAllMaximumDemands(int billingSetId, int typeId, DataContainer dc) {
        List billingValues = new ArrayList();

        for (int id = typeId; id < (typeId + MD_RANGE * 2); id += 2) {
            ObisCode maximumDemandObisCode = dc.getRoot().getStructure(billingSetId).getOctetString(id).toObisCode();
            if (dc.getRoot().getStructure(billingSetId).getStructure(id + 1).isStructure(0)) {
                DataStructure ds = dc.getRoot().getStructure(billingSetId).getStructure(id + 1).getStructure(0);
                long value = ds.getValue(0);
                ScalerUnit scalerUnit = new ScalerUnit(ds.getStructure(1).getInteger(0),
                        ds.getStructure(1).getInteger(1));
                Date date = ds.getOctetString(2).toDate(cof.getProtocolLink().getTimeZone());
                BillingValue billingValue = new BillingValue(date, value, scalerUnit, maximumDemandObisCode);
                billingValues.add(billingValue);
            }
        }
        return billingValues;
    }

    public MaximumDemandRegisterProfileMapper getMaximumDemandRegisterProfileMapper() {
        if (maximumDemandRegisterProfileMapper == null) {
            maximumDemandRegisterProfileMapper = new MaximumDemandRegisterProfileMapper(meterProtocol);
}
        return maximumDemandRegisterProfileMapper;
    }
}
