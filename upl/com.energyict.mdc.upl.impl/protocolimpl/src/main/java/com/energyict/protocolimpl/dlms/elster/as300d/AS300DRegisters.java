package com.energyict.protocolimpl.dlms.elster.as300d;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.Register;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 27/02/12
 * Time: 13:22
 */
public class AS300DRegisters {

    private final DlmsSession session;
    private List<AS300DStoredValues> storedValuesList = new ArrayList<AS300DStoredValues>();

    public static final ObisCode DAILY_BILLING_OBIS_1 = ObisCode.fromString("0.0.98.2.1.255");
    public static final ObisCode DAILY_BILLING_OBIS_2 = ObisCode.fromString("0.0.98.2.2.255");
    public static final ObisCode DAILY_BILLING_OBIS_3 = ObisCode.fromString("0.0.98.2.3.255");

    public static final ObisCode MONTHLY_BILLING_OBIS_1 = ObisCode.fromString("0.0.98.1.1.255");
    public static final ObisCode MONTHLY_BILLING_OBIS_2 = ObisCode.fromString("0.0.98.1.2.255");
    public static final ObisCode MONTHLY_BILLING_OBIS_3 = ObisCode.fromString("0.0.98.1.3.255");

    public AS300DRegisters(DlmsSession session) {
        this.session = session;
        initStoredValuesList();
    }

    public void initStoredValuesList() {
        storedValuesList.add(new AS300DStoredValues(session,  DAILY_BILLING_OBIS_1));
        storedValuesList.add(new AS300DStoredValues(session,  DAILY_BILLING_OBIS_2));
        storedValuesList.add(new AS300DStoredValues(session,  DAILY_BILLING_OBIS_3));

        storedValuesList.add(new AS300DStoredValues(session,  MONTHLY_BILLING_OBIS_1));
        storedValuesList.add(new AS300DStoredValues(session,  MONTHLY_BILLING_OBIS_2));
        storedValuesList.add(new AS300DStoredValues(session,  MONTHLY_BILLING_OBIS_3));
    }

    public static RegisterInfo translateRegister(ObisCode obisCode) {
        return obisCode == null ? null : new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        if (obisCode.getF() != 255) {
            HistoricalValue historicalValue = getStoredValues(obisCode).getHistoricalValue(obisCode);
            return new RegisterValue(obisCode, historicalValue.getQuantityValue(), historicalValue.getEventTime(), historicalValue.getBillingDate());
        }

        CosemObjectFactory cof = session.getCosemObjectFactory();
        Register register = cof.getRegister(obisCode);
        Quantity value = register.getQuantityValue();
        return new RegisterValue(obisCode, value);
    }

    private AS300DStoredValues getStoredValues(ObisCode obisCode) throws IOException {
        int[] bounds = new int[2];

        if (Math.abs(obisCode.getF()) < 12) {   //Monthly billing
            bounds[0] = 3;
            bounds[1] = 6;
        } else {                                // Daily billing
            bounds[0] = 0;
            bounds[1] = 3;
        }

        for (int i = bounds[0]; i < bounds[1]; i++) {
            AS300DStoredValues storedValues = storedValuesList.get(i);
            if (storedValues.isObiscodeCaptured(obisCode)) {
                return storedValues;
            }
        }
        throw new NoSuchRegisterException("StoredValues, register with obiscode " + obisCode + " not found in the Capture Objects list of the billing profiles.");
    }
}
