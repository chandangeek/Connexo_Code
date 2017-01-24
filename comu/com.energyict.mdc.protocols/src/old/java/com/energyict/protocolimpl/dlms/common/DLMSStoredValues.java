package com.energyict.protocolimpl.dlms.common;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import com.energyict.dlms.DataStructure;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.OctetString;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 23/04/12
 * Time: 9:12
 */
public class DLMSStoredValues implements StoredValues {

    public static final ObisCode CLOCK_OBIS = ObisCode.fromString("0.0.1.0.0.255");

    private ProfileGeneric profileGeneric;
    private ObisCode profileObiscode;
    private DataStructure buffer;
    protected DlmsSession session;

    /** HashMap containing CosemObjects linked to their base ObisCode, used for caching purposes.
     * By using this map, we prevent we have to read out the ScalerUnit attribute of the same CosemObject multiple times
     **/
    private Map<ObisCode, CosemObject> cosemObjectMap = new HashMap<ObisCode, CosemObject>();

    public DLMSStoredValues(DlmsSession session, ObisCode profileObisCode) {
        this.profileObiscode = profileObisCode;
        this.session = session;
    }

    public HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException {
        int index = getCapturedObjectIndex(obisCode);
        if (index == -1) {
            throw new NoSuchRegisterException("StoredValues, register with ObisCode " + obisCode + " not found in the Capture Objects list of the billing profile.");
        }
        ObisCode baseObiscode = ProtocolTools.setObisCodeField(obisCode, 5, (byte) 255);
        int reversedBillingPoint = getReversedBillingPoint(Math.abs(obisCode.getF()));
        CapturedObject cao = getProfileGeneric().getCaptureObjects().get(index);

        DataStructure intervalData = (DataStructure) getBuffer().getElement(reversedBillingPoint);     //Get an interval
        HistoricalValue historicalValue = new HistoricalValue();
        historicalValue.setBillingDate(getBillingPointTimeDate(Math.abs(obisCode.getF())));

        CosemObject cosemObject = cosemObjectMap.get(baseObiscode);

        if (cao.getClassId() == Register.CLASSID) {
            Register register = (cosemObject != null)
                    ? (Register) cosemObject
                    : getCosemObjectFactory().getRegister(baseObiscode);
            cosemObjectMap.put(baseObiscode, register);
            register.setValue(intervalData.convert2Long(index));
            historicalValue.setCosemObject(register);
        } else if (cao.getClassId() == ExtendedRegister.CLASSID) {
            ExtendedRegister extendedRegister = (cosemObject != null)
                    ? (ExtendedRegister) cosemObject
                    : getCosemObjectFactory().getExtendedRegister(baseObiscode);
            cosemObjectMap.put(baseObiscode, extendedRegister);
            extendedRegister.setValue(intervalData.convert2Long(index));
            historicalValue.setCosemObject(extendedRegister);

            Clock clock = new Clock(getCosemObjectFactory().getProtocolLink(), getCosemObjectFactory().getObjectReference(baseObiscode, 5));
            clock.setDateTime(intervalData.getOctetString(index + 1));
            historicalValue.setEventTime(clock.getDateTime());
        } else {
            throw new NoSuchRegisterException("StoredValues, getHistoricalValue, error invalid classId " + cao.getClassId());
        }
        return historicalValue;
    }

    public Date getBillingPointTimeDate(int billingPoint) throws IOException {
        DataStructure ds = (DataStructure) getBuffer().getElement(getReversedBillingPoint(billingPoint));
        Clock clock = new Clock(getCosemObjectFactory().getProtocolLink(), getCosemObjectFactory().getObjectReference(CLOCK_OBIS));
        clock.setDateTime((OctetString) ds.getElement(getCapturedObjectIndex(CLOCK_OBIS)));
        return clock.getDateTime();
    }

    public int getBillingPointCounter() throws IOException {
        return getBuffer().getNrOfElements();
    }

    public void retrieve() throws IOException {
        //Not implemented
    }

    public ProfileGeneric getProfileGeneric() {
        if (profileGeneric == null) {
            try {
                profileGeneric = getCosemObjectFactory().getProfileGeneric(profileObiscode);
            } catch (IOException e) {
                //Absorb exception
            }
        }
        return profileGeneric;
    }

    protected CosemObjectFactory getCosemObjectFactory() {
        return session.getCosemObjectFactory();
    }

    private DataStructure getBuffer() throws IOException {
        if (buffer == null) {
            buffer = profileGeneric.getBuffer().getRoot();
        }
        return buffer;
    }

    /**
     * Find out which channel in the EOB load profile matches the requested register
     */
    protected int getCapturedObjectIndex(ObisCode obisCode) throws IOException {
        List<CapturedObject> captureObjects = getProfileGeneric().getCaptureObjects();
        ObisCode baseObisCode = ProtocolTools.setObisCodeField(obisCode, 5, (byte) 255);
        for (int i = 0; i < captureObjects.size(); i++) {
            CapturedObject cao = captureObjects.get(i);
            if (baseObisCode.equals(cao.getObisCode())) {
                return i;
            }
        }
        return -1;
    }

    public boolean isObiscodeCaptured(ObisCode obisCode) throws IOException {
        return (getCapturedObjectIndex(obisCode) != -1);

    }

    protected int getReversedBillingPoint(int billingPoint) throws IOException {
        int point = billingPoint > 11 ? (billingPoint - 12) : billingPoint;
        int reversedPoint = getBillingPointCounter() - point - 1;

        if (reversedPoint < 0) {
            throw new NoSuchRegisterException("StoredValues, invalid billing point " + billingPoint);
        }
        return reversedPoint;
    }
}