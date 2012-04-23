package com.energyict.protocolimpl.dlms.elster.as300d;

import com.energyict.dlms.*;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 20/04/12
 * Time: 17:17
 */
public class AS300DStoredValues implements StoredValues {

    public static final ObisCode CLOCK_OBIS = ObisCode.fromString("0.0.1.0.0.255");

    private ProfileGeneric profileGeneric;
    private ObisCode profileObiscode;
    private DataContainer buffer;
    private DlmsSession session;

    public AS300DStoredValues(DlmsSession session, ObisCode profileObisCode) {
        this.profileObiscode = profileObisCode;
        this.session = session;
    }

    public HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException {
        int index = getCapturedObjectIndex(obisCode);
        if (index == -1) {
            throw new NoSuchRegisterException("StoredValues, register with obiscode " + obisCode + " not found in the Capture Objects list of the billing profile.");
        }
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

            Clock clock = new Clock(getCosemObjectFactory().getProtocolLink(), getCosemObjectFactory().getObjectReference(baseObiscode, 5));
            clock.setDateTime(ds.getOctetString(index + 1));
            historicalValue.setEventTime(clock.getDateTime());
        } else {
            throw new IOException("StoredValues, getHistoricalValue, error invalid classId " + cao.getClassId());
        }
        return historicalValue;
    }

    public Date getBillingPointTimeDate(int billingPoint) throws IOException {
        DataStructure ds = (DataStructure) getBuffer().getRoot().getElement(getReversedBillingPoint(billingPoint));
        Clock clock = new Clock(getCosemObjectFactory().getProtocolLink(), getCosemObjectFactory().getObjectReference(CLOCK_OBIS));
        clock.setDateTime((OctetString) ds.getElement(getCapturedObjectIndex(CLOCK_OBIS)));
        return clock.getDateTime();
    }

    public int getBillingPointCounter() throws IOException {
        return getProfileGeneric().getEntriesInUse();
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

    private CosemObjectFactory getCosemObjectFactory() {
        return session.getCosemObjectFactory();
    }

    private DataContainer getBuffer() throws IOException {
        if (buffer == null) {
            buffer = profileGeneric.getBuffer();
        }
        return buffer;
    }

    private int getCapturedObjectIndex(ObisCode obisCode) throws IOException {
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
        return (getCapturedObjectIndex(obisCode) == -1) ? false : true;

    }

    private int getReversedBillingPoint(int billingPoint) throws IOException {
        int point = billingPoint > 11 ? (billingPoint -12) : billingPoint;
        int reversedPoint = getBillingPointCounter() - point - 1;

        if (reversedPoint < 0) {
            throw new NoSuchRegisterException("Invalid billing point");
        }
        return reversedPoint;
    }
}