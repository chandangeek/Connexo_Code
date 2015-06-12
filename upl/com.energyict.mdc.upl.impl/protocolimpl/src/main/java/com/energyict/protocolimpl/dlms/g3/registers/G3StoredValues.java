package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.dlms.DataStructure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.Register;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocolimpl.dlms.common.DLMSStoredValues;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 28/03/13
 * Time: 15:04
 * Author: khe
 */
public class G3StoredValues extends DLMSStoredValues {

    private final TimeZone timeZone;
    private final boolean daily;  //True: daily EOB. False: Monthly EOB

    public G3StoredValues(CosemObjectFactory cosemObjectFactory, TimeZone timeZone, ObisCode profileObisCode, boolean daily) {
        super(cosemObjectFactory, profileObisCode);
        this.timeZone = timeZone;
        this.daily = daily;
    }

    public HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException {
        int channelIndex = getCapturedObjectIndex(obisCode);
        if (channelIndex == -1) {
            throw new NoSuchRegisterException("StoredValues, register with obiscode " + obisCode + " was not found in the Capture Objects list of the billing profile.");
        }

        int billingPoint = Math.abs(obisCode.getF());
        billingPoint = billingPoint > 11 ? billingPoint - 12 : billingPoint; //Remove 12 offset in case of daily, this merely indicates it's a daily billing register
        ObisCode baseObiscode = ProtocolTools.setObisCodeField(obisCode, 5, (byte) 255);

        //Selective access: request an entry based on a from and to date that is nearest to this one entry
        Calendar properFromDate = getProperFromDate(billingPoint);
        Calendar properToDate = getProperToDate(billingPoint);
        DataStructure intervalData = getProfileGeneric().getBuffer(properFromDate, properToDate).getRoot();//Use selective access by entries

        HistoricalValue historicalValue = new HistoricalValue();
        if (intervalData.getNrOfElements() == 0 || intervalData.getStructure(0) == null) {
            throw new NoSuchRegisterException(obisCode.toString());
        }
        DataStructure structure = intervalData.getStructure(0);
        Register register = getCosemObjectFactory().getRegister(baseObiscode);
        register.setValue(structure.getValue(channelIndex));
        Date timestamp = structure.getOctetString(0).toDate(timeZone);
        historicalValue.setBillingDate(timestamp);
        historicalValue.setEventTime(timestamp);
        historicalValue.setCosemObject(register);
        return historicalValue;
    }

    private Calendar getProperToDate(int billingPoint) {
        Calendar toDate = getNow();
        if (daily) {
            toDate.add(Calendar.DATE, -billingPoint);   //Go back X days
            toDate.add(Calendar.HOUR_OF_DAY, 12);
        } else {
            toDate.add(Calendar.MONTH, -billingPoint);   //Go back X months
            toDate.add(Calendar.DATE, 7);
        }
        return toDate;
    }

    private Calendar getProperFromDate(int billingPoint) {
        Calendar toDate = getNow();
        if (daily) {
            toDate.add(Calendar.DATE, -billingPoint);   //Go back X days
            toDate.add(Calendar.HOUR_OF_DAY, -12);
        } else {
            toDate.add(Calendar.MONTH, -billingPoint);   //Go back X months
            toDate.add(Calendar.DATE, -7);
        }
        return toDate;
    }

    private Calendar getNow() {
        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (!daily) {
            cal.set(Calendar.DATE, 0);
        }
        cal.setLenient(true);
        return cal;
    }

    @Override
    public ProfileGeneric getProfileGeneric() {
        ProfileGeneric profileGeneric = super.getProfileGeneric();
        profileGeneric.setProfileCaching(false);
        return profileGeneric;
    }

    public Date getBillingPointTimeDate(int billingPoint) throws IOException {
        return null;
    }

    public int getBillingPointCounter() throws IOException {
        return 0;    //Not used here
    }

    public void retrieve() throws IOException {
        //Not implemented
    }
}
