/*
 * StoredValues.java
 *
 * Created on 12 oktober 2004, 13:08
 */

package com.energyict.protocolimpl.dlms.iskrame37x;

import com.energyict.dlms.*;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;
import java.util.List;


/**
 *
 * @author  Koen
 */
public class StoredValuesImpl implements StoredValues {

    public static final ObisCode OBISCODE_CLOCK = ObisCode.fromString("0.0.1.0.0.255");

    private ProfileGeneric profileGeneric;
    private CosemObjectFactory cof;
    private DataContainer buffer;

    public StoredValuesImpl(CosemObjectFactory cof, ObisCode profileObis) throws IOException {
        this.cof = cof;
        this.profileGeneric = cof.getProfileGeneric(profileObis);
    }

    public HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException {
        int index = getCapturedObjectIndex(obisCode);
        ObisCode baseObiscode = ProtocolTools.setObisCodeField(obisCode, 5, (byte) 255);
        int reversedBillingPoint = getReversedBillingPoint(Math.abs(obisCode.getF()));
        CapturedObject cao = getProfileGeneric().getCaptureObjects().get(index);

        DataStructure ds = (DataStructure) getBuffer().getRoot().getElement(reversedBillingPoint);
        HistoricalValue historicalValue = new HistoricalValue();
        historicalValue.setBillingDate(getBillingPointTimeDate(Math.abs(obisCode.getF())));

        if (cao.getClassId() == Register.CLASSID) {
            Register register = getCosemObjectFactory().getRegister(baseObiscode);
            register.setValue(ds.convert2Long(index));
            historicalValue.setCosemObject(register);
        } else if (cao.getClassId() == ExtendedRegister.CLASSID) {
            ExtendedRegister extendedRegister = getCosemObjectFactory().getExtendedRegister(baseObiscode);
            extendedRegister.setValue(ds.convert2Long(index));
            historicalValue.setCosemObject(extendedRegister);
        } else {
            throw new IOException("StoredValues, getHistoricalValue, error invalid classId " + cao.getClassId());
        }
        return historicalValue;
    }

    public Date getBillingPointTimeDate(int billingPoint) throws IOException {
        DataStructure ds = (DataStructure) getBuffer().getRoot().getElement(getReversedBillingPoint(billingPoint));
        Clock clock = new Clock(getCosemObjectFactory().getProtocolLink(),getCosemObjectFactory().getObjectReference(getCosemObjectFactory().CLOCK_OBJECT_LN, getCosemObjectFactory().getProtocolLink().getMeterConfig().getClockSN()));
        clock.setDateTime((OctetString) ds.getElement(getCapturedObjectIndex(OBISCODE_CLOCK)));
        return clock.getDateTime();
    }

    public int getBillingPointCounter() throws IOException {
        return getProfileGeneric().getEntriesInUse();
    }

    public void retrieve() throws IOException {
        //Not implemented
    }

    public ProfileGeneric getProfileGeneric() {
        return profileGeneric;
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return cof;
    }

    private DataContainer getBuffer() throws IOException {
        if (buffer == null) {
            buffer = profileGeneric.getBuffer();
        }
        return buffer;
    }

    private int getCapturedObjectIndex(ObisCode obisCode) throws IOException {
        List<CapturedObject> captureObjects = getProfileGeneric().getCaptureObjects();
        for (int i = 0; i < captureObjects.size(); i++) {
            CapturedObject cao = captureObjects.get(i);
            ObisCode baseObisCode = ProtocolTools.setObisCodeField(obisCode, 5, (byte) 255);
            if (baseObisCode.equals(cao.getObisCode())) {
                return i;
            }
        }
        throw new NoSuchRegisterException("StoredValues, register with obiscode " + obisCode + " not found in the Capture Objects list of the billing profile.");
    }

    private int getReversedBillingPoint(int billingPoint) throws IOException {
        return getBillingPointCounter() - billingPoint - 1;
    }
}