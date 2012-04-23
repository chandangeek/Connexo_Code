package com.energyict.protocolimpl.dlms.elster.as300d;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.Register;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

import java.io.IOException;
import java.util.*;

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
        if (obisCode.equals(ObisCode.fromString("1.1.94.34.100.255"))) {
            Data data = cof.getData(obisCode);
            long value = data.getValue();
            String text = "Active Quadrant: " + (value != 0 ? value : "No quadrant detected");
            return new RegisterValue(obisCode, new Quantity(value, Unit.getUndefined()), null, null, null, new Date(), 0, text);
        } else if (obisCode.equals(ObisCode.fromString("1.1.94.34.104.255"))) {
            Data data = cof.getData(obisCode);
            long value = data.getValue();
            String text = "Phase undefined";
            if ((value & 0x01) == 0x01) {
                text = "Phase 1";
            } else if ((value & 0x02) == 0x02) {
                text = "Phase 2";
            } else if ((value & 0x04) == 0x04) {
                text = "Phase 3";
            }
            return new RegisterValue(obisCode, new Quantity(value, Unit.getUndefined()), null, null, null, new Date(), 0, text);
        } else if (obisCode.equals(ObisCode.fromString("0.0.96.3.10.255")) || obisCode.equals(ObisCode.fromString("0.1.94.34.20.255")))  {
            final Disconnector disconnector = cof.getDisconnector(obisCode);
            TypeEnum controlState = disconnector.getControlState();
            String text = "Unknown control state: " + controlState.getValue();
            if (controlState.getValue() == 0) {
                text = "Disconnected";
            } else  if (controlState.getValue() == 1) {
                text = "Connected";
            } else  if (controlState.getValue() == 2) {
                text = "Ready for Re-connection";
            }
            return new RegisterValue(obisCode, new Quantity(controlState.getValue(), Unit.getUndefined()), null, null, null, new Date(), 0, text);
        }
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
