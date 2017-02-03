package com.energyict.protocolimplv2.dlms.idis.iskra.mx382;

import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.HistoricalRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.idis.am500.registers.IDISStoredValues;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * Created by cisac on 1/14/2016.
 */
public class Mx382StoredValues extends IDISStoredValues{

    public static final ObisCode OBISCODE_DAILY_BILLING = ObisCode.fromString("0.0.98.2.0.255");
    public ObisCode profileGenericObisCode = ObisCode.fromString("0.0.98.1.0.255");
    boolean profileGenericObisCodeChanged = true;
    private ProfileGeneric profileGeneric = null;

    public Mx382StoredValues(Mx382 mx382) {
        super(mx382);
    }

    public HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException {
        ObisCode baseObisCode = ProtocolTools.setObisCodeField(obisCode, 5, (byte) 255);
        updateProfileGenericObisCode(obisCode);
        int channelIndex = checkIfObisCodeIsCaptured(baseObisCode);
        int billingPoint = obisCode.getF() > 11 ? (obisCode.getF() - 12) : obisCode.getF();
        if (!isValidBillingPoint(billingPoint)) {
            throw new NoSuchRegisterException("Billing point " + obisCode.getF() + " doesn't exist for obiscode " + baseObisCode + ".");
        }
        int value = getProfileData().getIntervalData(getReversedBillingPoint(billingPoint)).getIntervalValues().get(channelIndex - 1).getNumber().intValue();
        HistoricalRegister cosemValue = new HistoricalRegister();
        cosemValue.setQuantityValue(BigDecimal.valueOf(value), getUnit(baseObisCode));

        return new HistoricalValue(cosemValue, getBillingPointTimeDate(getReversedBillingPoint(billingPoint)), new Date(), 0);
    }

    private Unit getUnit(ObisCode baseObisCode) throws IOException {
        Map<ObisCode, Unit> unitMap = ((Mx382)getProtocol()).getIDISProfileDataReader().readUnits(null, Arrays.asList(baseObisCode));
        return unitMap.get(baseObisCode);
    }

    public ProfileGeneric getProfileGeneric() throws NotInObjectListException {
        if (profileGeneric == null || profileGenericObisCodeChanged) {
            profileGeneric = getCosemObjectFactory().getProfileGeneric(profileGenericObisCode);
        }
        return profileGeneric;
    }

    private void setProfileGenericObisCode(ObisCode profileGenericObisCode) {
        profileGenericObisCodeChanged = false;
        if(!this.profileGenericObisCode.equals(profileGenericObisCode)){
            this.profileGenericObisCode = profileGenericObisCode;
            profileGenericObisCodeChanged = true;
        }
    }

    private void updateProfileGenericObisCode(ObisCode obisCode) {
        if (obisCode.getF() < 12) {
            setProfileGenericObisCode(OBISCODE_BILLING_PROFILE);
        } else {
            setProfileGenericObisCode(OBISCODE_DAILY_BILLING);
        }
    }

    private boolean isValidBillingPoint(int point) {
        try {
            int billingPoint = point > 11 ? (point - 12) : point;   // If point is > 11, then daily historics are requested.

            return (billingPoint >= 0) && (billingPoint < getBillingPointCounter());
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getProtocol().getDlmsSession().getProperties().getRetries() + 1);
        }
    }

}
