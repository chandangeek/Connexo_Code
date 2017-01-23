package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.UnsupportedException;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.OctetString;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 14/12/11
 * Time: 9:30
 */
public class StoredValuesImpl implements StoredValues {

    ProtocolLink protocolLink;
    ProfileGeneric profileGeneric;
    CosemObjectFactory cof;

    private static final int INDEX_CLOCK=0;
    private static final int INDEX_RESET_COUNTER=1;

    DataContainer buffer=null; // cached
    int vZ=-1; // cached

    /** Creates a new instance of StoredValues */
    public StoredValuesImpl(CosemObjectFactory cof) {
        this.protocolLink=cof.getProtocolLink();
        this.cof=cof;
    }

    public void retrieve() throws IOException {
        profileGeneric = new ProfileGeneric(protocolLink,cof.getObjectReference(DLMSCOSEMGlobals.HISTORIC_VALUES_OBJECT_LN,protocolLink.getMeterConfig().getHistoricValuesSN()));
    }

    /**
     * Getter for property profileGeneric.
     * @return Value of property profileGeneric.
     */
    public com.energyict.dlms.cosem.ProfileGeneric getProfileGeneric() {
        return profileGeneric;
    }

    private int getVZ() throws IOException {
        if (vZ == -1) {
            vZ = (int) cof.getRegister(protocolLink.getMeterConfig().getResetCounterSN()).getValue();
        }
        return vZ;
    }


    private DataContainer getBuffer() throws IOException {
        if (buffer == null) {
            buffer = profileGeneric.getBuffer();
        }
        return buffer;
    }

    public Date getBillingPointTimeDate(int billingPoint) throws IOException {
        int resetCounter = getVZ() - billingPoint;
        for (int i=0;i<getBuffer().getRoot().getNrOfElements();i++) {
            DataStructure ds = (DataStructure)buffer.getRoot().getElement(i);
             if (((Integer)ds.getElement(INDEX_RESET_COUNTER)).intValue() == resetCounter) {
                Clock clock = new Clock(protocolLink,cof.getObjectReference(DLMSCOSEMGlobals.CLOCK_OBJECT_LN,protocolLink.getMeterConfig().getClockSN()));
                clock.setDateTime((OctetString)ds.getElement(INDEX_CLOCK));
                return clock.getDateTime();
             }
        }
        throw new NoSuchRegisterException("StoredValues, getBillingPointTimeDate, invalid billingpoint "+billingPoint);
    }


    public HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException {

        int baseObject = protocolLink.getMeterConfig().getSN(obisCode);
        int index = getCapturedObjectIndex(obisCode);
        CapturedObject cao = getCapturedObject(index);


        int resetCounter = getVZ() - Math.abs(obisCode.getF());

        HistoricalValue historicalValue = new HistoricalValue();

        for (int i=0;i<getBuffer().getRoot().getNrOfElements();i++) {
            DataStructure ds = (DataStructure)buffer.getRoot().getElement(i);

            // if the obiscode F field contains the right resetcounter historical value
            if (((Integer)ds.getElement(INDEX_RESET_COUNTER)).intValue() == resetCounter) {
                historicalValue.setResetCounter(((Integer)ds.getElement(INDEX_RESET_COUNTER)).intValue());
                Clock clock = new Clock(protocolLink,cof.getObjectReference(DLMSCOSEMGlobals.CLOCK_OBJECT_LN,protocolLink.getMeterConfig().getClockSN()));
                clock.setDateTime((OctetString)ds.getElement(INDEX_CLOCK));
                historicalValue.setBillingDate(clock.getDateTime());

                if (cao.getClassId() == Register.CLASSID) {
                   Register register = cof.getRegister(protocolLink.getMeterConfig().getSN(obisCode));
                   register.setValue(ds.convert2Long(index));
                   historicalValue.setCosemObject(register);
                }
                else if (cao.getClassId() == ExtendedRegister.CLASSID) {
                   ExtendedRegister extendedRegister = cof.getExtendedRegister(protocolLink.getMeterConfig().getSN(obisCode));
                   extendedRegister.setValue(ds.convert2Long(index));
                   extendedRegister.setCaptureTime((OctetString)ds.getElement(index+1));
                   historicalValue.setCosemObject(extendedRegister);
                }
                else {
                    throw new IOException("StoredValues, getHistoricalValue, error invalid classId " + cao.getClassId());
                }
                return historicalValue;
            }
        }
        throw new NoSuchRegisterException("StoredValues, getHistoricalValue, no register for obisCode "+obisCode);
    }

    private int getCapturedObjectIndex(ObisCode obisCode) throws IOException {
        int index=0;
        int nrOfEntries = profileGeneric.getProfileEntries();
        if (profileGeneric.getCaptureObjects().size() == 0) {
            return -1;
        }
        for (CapturedObject cao : profileGeneric.getCaptureObjects()) {
            if ((obisCode.getA() == cao.getLogicalName().getA()) &&
                    (obisCode.getB() == cao.getLogicalName().getB()) &&
                    (obisCode.getC() == cao.getLogicalName().getC()) &&
                    (obisCode.getD() == cao.getLogicalName().getD()) &&
                    (obisCode.getE() == cao.getLogicalName().getE()) &&
                    ((obisCode.getF() >= 0 && obisCode.getF() <= (nrOfEntries - 1)) || (obisCode.getF() <= 0 && obisCode.getF() >= -(nrOfEntries - 1))) &&
                    (cao.getAttributeIndex() == 2)) {
                break;
            }
            index++;
        }
        if (index == profileGeneric.getCaptureObjects().size()) //return -1;
        {
            throw new NoSuchRegisterException("StoredValues, register with obiscode " + obisCode + " not found in the historic values list.");
        }

        return index;
    }

    private CapturedObject getCapturedObject(int index) throws IOException {
        if ((index >= profileGeneric.getCaptureObjects().size()) || (index<0)) {
            throw new IOException("StoredValues, getCapturedObject, invalid index in CapturedObject list, " + index);
        }
        return profileGeneric.getCaptureObjects().get(index);
    }

    public int getBillingPointCounter() throws IOException {
        throw new UnsupportedException();
    }

}
