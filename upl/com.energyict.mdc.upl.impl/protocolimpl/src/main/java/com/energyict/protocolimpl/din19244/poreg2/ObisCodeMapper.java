package com.energyict.protocolimpl.din19244.poreg2;

import com.energyict.cbo.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

import java.io.IOException;
import java.util.*;

/**
 * Class for reading out registers.
 * Unsupported registers throw an exception.
 * <p/>
 * Copyrights EnergyICT
 * Date: 2-mei-2011
 * Time: 13:26:34
 */
public class ObisCodeMapper {

    static Map<ObisCode, String> registerMaps = new HashMap<ObisCode, String>();

    private static final ObisCode OBISCODE_SERIAL_NUMBER = ObisCode.fromString("0.0.96.1.0.255");
    private static final ObisCode OBISCODE_FIRMWARE = ObisCode.fromString("0.0.96.1.5.255");
    private static final ObisCode OBISCODE_LAST_DEMAND_RESET_TIMESTAMP = ObisCode.fromString("1.0.0.1.2.0");
    private static final ObisCode OBISCODE_BILLING_COUNTER = ObisCode.fromString("1.0.0.1.0.255");

    private static final ObisCode OBISCODE_MAX_ACTIVE_ENERGY_TOTAL = ObisCode.fromString("1.0.1.6.0.255");
    private static final ObisCode OBISCODE_MAX_ACTIVE_ENERGY_TOTAL2 = ObisCode.fromString("1.0.1.6.4.255");
    private static final ObisCode OBISCODE_MAX_ACTIVE_ENERGY_RATE1 = ObisCode.fromString("1.0.1.6.1.255");
    private static final ObisCode OBISCODE_MAX_ACTIVE_ENERGY_RATE2 = ObisCode.fromString("1.0.1.6.2.255");

    private static final ObisCode OBISCODE_CUMUL_ACTIVE_ENERGY_TOTAL = ObisCode.fromString("1.0.1.8.0.255");
    private static final ObisCode OBISCODE_CUMUL_ACTIVE_ENERGY_TOTAL2 = ObisCode.fromString("1.0.1.8.4.255");
    private static final ObisCode OBISCODE_CUMUL_ACTIVE_ENERGY_RATE1 = ObisCode.fromString("1.0.1.8.1.255");
    private static final ObisCode OBISCODE_CUMUL_ACTIVE_ENERGY_RATE2 = ObisCode.fromString("1.0.1.8.2.255");

    private static final ObisCode OBISCODE_MAX_APPARENT_ENERGY = ObisCode.fromString("1.0.9.6.0.255");
    private static final ObisCode OBISCODE_MAX_APPARENT_ENERGY2 = ObisCode.fromString("1.0.9.6.4.255");
    private static final ObisCode OBISCODE_MAX_APPARENT_ENERGY_RATE1 = ObisCode.fromString("1.0.9.6.1.255");
    private static final ObisCode OBISCODE_MAX_APPARENT_ENERGY_RATE2 = ObisCode.fromString("1.0.9.6.2.255");

    private static final ObisCode OBISCODE_CUMUL_APPARENT_ENERGY = ObisCode.fromString("1.0.9.8.0.255");
    private static final ObisCode OBISCODE_CUMUL_APPARENT_ENERGY2 = ObisCode.fromString("1.0.9.8.4.255");
    private static final ObisCode OBISCODE_CUMUL_APPARENT_ENERGY_RATE1 = ObisCode.fromString("1.0.9.8.1.255");
    private static final ObisCode OBISCODE_CUMUL_APPARENT_ENERGY_RATE2 = ObisCode.fromString("1.0.9.8.2.255");

    private static final ObisCode OBISCODE_MAX_ACTIVE_ENERGY_TOTAL_BILLING = ObisCode.fromString("1.0.1.6.0.0");
    private static final ObisCode OBISCODE_MAX_ACTIVE_ENERGY_TOTAL2_BILLING = ObisCode.fromString("1.0.1.6.4.0");
    private static final ObisCode OBISCODE_MAX_ACTIVE_ENERGY_RATE1_BILLING = ObisCode.fromString("1.0.1.6.1.0");
    private static final ObisCode OBISCODE_MAX_ACTIVE_ENERGY_RATE2_BILLING = ObisCode.fromString("1.0.1.6.2.0");

    private static final ObisCode OBISCODE_CUMUL_ACTIVE_ENERGY_TOTAL_BILLING = ObisCode.fromString("1.0.1.8.0.0");
    private static final ObisCode OBISCODE_CUMUL_ACTIVE_ENERGY_TOTAL2_BILLING = ObisCode.fromString("1.0.1.8.4.0");
    private static final ObisCode OBISCODE_CUMUL_ACTIVE_ENERGY_RATE1_BILLING = ObisCode.fromString("1.0.1.8.1.0");
    private static final ObisCode OBISCODE_CUMUL_ACTIVE_ENERGY_RATE2_BILLING = ObisCode.fromString("1.0.1.8.2.0");

    private static final ObisCode OBISCODE_MAX_APPARENT_ENERGY_BILLING = ObisCode.fromString("1.0.9.6.0.0");
    private static final ObisCode OBISCODE_MAX_APPARENT_ENERGY2_BILLING = ObisCode.fromString("1.0.9.6.4.0");
    private static final ObisCode OBISCODE_MAX_APPARENT_ENERGY_RATE1_BILLING = ObisCode.fromString("1.0.9.6.1.0");
    private static final ObisCode OBISCODE_MAX_APPARENT_ENERGY_RATE2_BILLING = ObisCode.fromString("1.0.9.6.2.0");

    private static final ObisCode OBISCODE_CUMUL_APPARENT_ENERGY_BILLING = ObisCode.fromString("1.0.9.8.0.0");
    private static final ObisCode OBISCODE_CUMUL_APPARENT_ENERGY2_BILLING = ObisCode.fromString("1.0.9.8.4.0");
    private static final ObisCode OBISCODE_CUMUL_APPARENT_ENERGY_RATE1_BILLING = ObisCode.fromString("1.0.9.8.1.0");
    private static final ObisCode OBISCODE_CUMUL_APPARENT_ENERGY_RATE2_BILLING = ObisCode.fromString("1.0.9.8.2.0");


    static {
        registerMaps.put(OBISCODE_SERIAL_NUMBER, "Meter ID (serial number)");
        registerMaps.put(OBISCODE_LAST_DEMAND_RESET_TIMESTAMP, "Time of last demand reset");
        registerMaps.put(OBISCODE_BILLING_COUNTER, "Demand reset counter");
        registerMaps.put(OBISCODE_FIRMWARE, "Active firmware version");

        registerMaps.put(OBISCODE_MAX_APPARENT_ENERGY, "Max apparent energy total");
        registerMaps.put(OBISCODE_MAX_APPARENT_ENERGY2, "Max apparent energy total");
        registerMaps.put(OBISCODE_MAX_APPARENT_ENERGY_RATE1, "Max apparent energy total, rate 1");
        registerMaps.put(OBISCODE_MAX_APPARENT_ENERGY_RATE2, "Max apparent energy total, rate 2");

        registerMaps.put(OBISCODE_CUMUL_APPARENT_ENERGY, "Cumulative apparent energy total");
        registerMaps.put(OBISCODE_CUMUL_APPARENT_ENERGY2, "Cumulative apparent energy total");
        registerMaps.put(OBISCODE_CUMUL_APPARENT_ENERGY_RATE1, "Cumulative apparent energy total, rate 1");
        registerMaps.put(OBISCODE_CUMUL_APPARENT_ENERGY_RATE2, "Cumulative apparent energy total, rate 2");

        registerMaps.put(OBISCODE_CUMUL_ACTIVE_ENERGY_TOTAL, "Active energy (import) total");
        registerMaps.put(OBISCODE_CUMUL_ACTIVE_ENERGY_TOTAL2, "Active energy (import) total");
        registerMaps.put(OBISCODE_CUMUL_ACTIVE_ENERGY_RATE1, "Active energy (import) rate 1");
        registerMaps.put(OBISCODE_CUMUL_ACTIVE_ENERGY_RATE2, "Active energy (import) rate 2");

        registerMaps.put(OBISCODE_MAX_ACTIVE_ENERGY_TOTAL, "Max demand active energy (import) total");
        registerMaps.put(OBISCODE_MAX_ACTIVE_ENERGY_TOTAL2, "Max demand active energy (import) total");
        registerMaps.put(OBISCODE_MAX_ACTIVE_ENERGY_RATE1, "Max demand active energy (import) rate 1");
        registerMaps.put(OBISCODE_MAX_ACTIVE_ENERGY_RATE2, "Max demand active energy (import) rate 2");

        registerMaps.put(OBISCODE_MAX_APPARENT_ENERGY_BILLING, "Max apparent energy total at last billing point");
        registerMaps.put(OBISCODE_MAX_APPARENT_ENERGY2_BILLING, "Max apparent energy total at last billing point");
        registerMaps.put(OBISCODE_MAX_APPARENT_ENERGY_RATE1_BILLING, "Max apparent energy total, rate 1 at last billing point");
        registerMaps.put(OBISCODE_MAX_APPARENT_ENERGY_RATE2_BILLING, "Max apparent energy total, rate 2 at last billing point");

        registerMaps.put(OBISCODE_CUMUL_APPARENT_ENERGY_BILLING, "Cumulative apparent energy total at last billing point");
        registerMaps.put(OBISCODE_CUMUL_APPARENT_ENERGY2_BILLING, "Cumulative apparent energy total at last billing point");
        registerMaps.put(OBISCODE_CUMUL_APPARENT_ENERGY_RATE1_BILLING, "Cumulative apparent energy total, rate 1 at last billing point");
        registerMaps.put(OBISCODE_CUMUL_APPARENT_ENERGY_RATE2_BILLING, "Cumulative apparent energy total, rate 2 at last billing point");

        registerMaps.put(OBISCODE_MAX_ACTIVE_ENERGY_TOTAL_BILLING, "Max demand active energy (import) total at last billing point");
        registerMaps.put(OBISCODE_MAX_ACTIVE_ENERGY_TOTAL2_BILLING, "Max demand active energy (import) total at last billing point");
        registerMaps.put(OBISCODE_MAX_ACTIVE_ENERGY_RATE1_BILLING, "Max demand active energy (import) rate 1 at last billing point");
        registerMaps.put(OBISCODE_MAX_ACTIVE_ENERGY_RATE2_BILLING, "Max demand active energy (import) rate 2 at last billing point");

        registerMaps.put(OBISCODE_CUMUL_ACTIVE_ENERGY_TOTAL_BILLING, "Cumulative active energy (import) total at last billing point");
        registerMaps.put(OBISCODE_CUMUL_ACTIVE_ENERGY_TOTAL2_BILLING, "Cumulative active energy (import) total at last billing point");
        registerMaps.put(OBISCODE_CUMUL_ACTIVE_ENERGY_RATE1_BILLING, "Cumulative active energy (import) rate 1 at last billing point");
        registerMaps.put(OBISCODE_CUMUL_ACTIVE_ENERGY_RATE2_BILLING, "Cumulative active energy (import) rate 2 at last billing point");
    }

    private Poreg poreg;

    public ObisCodeMapper(final Poreg poreg) {
        this.poreg = poreg;
    }

    public final String getRegisterExtendedLogging() {
        StringBuilder strBuilder = new StringBuilder();
        for (Map.Entry<ObisCode, String> obisCodeStringEntry : registerMaps.entrySet()) {
            poreg.getLogger().info(obisCodeStringEntry.getKey().toString() + ", " + obisCodeStringEntry.getValue());
        }
        return strBuilder.toString();
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        String info = registerMaps.get(obisCode);
        return new RegisterInfo(info);
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        if (obisCode.equals(OBISCODE_FIRMWARE)) {
            String firmwareVersion = poreg.getFirmwareVersion();
            return new RegisterValue(obisCode, new Quantity(0, Unit.get("")), new Date(), new Date(), new Date(), new Date(), 0, firmwareVersion);
        } else if (obisCode.equals(OBISCODE_SERIAL_NUMBER)) {
            String id = poreg.getRegisterFactory().readSerialNumber();
            return new RegisterValue(obisCode, new Quantity(Integer.parseInt(id), Unit.get("")), new Date(), new Date(), new Date(), new Date(), 0, id);
        } else if (obisCode.equals(OBISCODE_BILLING_COUNTER)) {
            int counter = poreg.getRegisterFactory().readBillingCounter();
            return new RegisterValue(obisCode, new Quantity(counter, Unit.get("")), new Date());
        } else if (obisCode.equals(OBISCODE_LAST_DEMAND_RESET_TIMESTAMP)) {
            Date timeStamp = poreg.getRegisterFactory().readBillingDataLastPeriodTimeStamp();
            return new RegisterValue(obisCode, new Quantity(0, Unit.get("")), timeStamp, new Date(), new Date(), new Date(), 0, timeStamp.toString());
        } else if (isApparentEnergyBillingData(obisCode)) {
            long totalApparentEnergyLastBillingPeriod = poreg.getRegisterFactory().readBillingData(obisCode.getE(), 3, obisCode.getD());
            Date timeStamp = poreg.getRegisterFactory().readBillingDataLastPeriodTimeStamp();
            return new RegisterValue(obisCode, new Quantity(totalApparentEnergyLastBillingPeriod, Unit.get(BaseUnit.VOLTAMPEREHOUR, 3)), new Date(), timeStamp);
        } else if (isActiveEnergyBillingData(obisCode)) {
            if (poreg.getApparentEnergyResultLevel() == 0) {
                throw new NoSuchRegisterException("No active energy available, inputs contain apparent energy.");
            }
            long totalTotalActiveEnergyLastBillingPeriod = poreg.getRegisterFactory().readBillingData(obisCode.getE(), 0, obisCode.getD());
            Date timeStamp = poreg.getRegisterFactory().readBillingDataLastPeriodTimeStamp();
            return new RegisterValue(obisCode, new Quantity(totalTotalActiveEnergyLastBillingPeriod, Unit.get(BaseUnit.WATTHOUR, 3)), new Date(), timeStamp);
        } else if (isApparentEnergyCurrentData(obisCode)) {
            long totalApparentEnergyCurrentPeriod = poreg.getRegisterFactory().readCurrentData(obisCode.getE(), 3, obisCode.getD());
            Date timeStamp = poreg.getRegisterFactory().readBillingDataCurrentPeriodTimeStamp();
            return new RegisterValue(obisCode, new Quantity(totalApparentEnergyCurrentPeriod, Unit.get(BaseUnit.VOLTAMPEREHOUR, 3)), new Date(), timeStamp);
        } else if (isActiveEnergyCurrentData(obisCode)) {
            if (poreg.getApparentEnergyResultLevel() == 0) {
                throw new NoSuchRegisterException("No active energy available, inputs contain apparent energy.");
            }
            long totalTotalActiveEnergyCurrentBillingPeriod = poreg.getRegisterFactory().readCurrentData(obisCode.getE(), 0, obisCode.getD());
            Date timeStamp = poreg.getRegisterFactory().readBillingDataCurrentPeriodTimeStamp();
            return new RegisterValue(obisCode, new Quantity(totalTotalActiveEnergyCurrentBillingPeriod, Unit.get(BaseUnit.WATTHOUR, 3)), new Date(), timeStamp);
        } else {
            throw new NoSuchRegisterException("Register with obiscode [" + obisCode + "] is not supported");
        }
    }

    private boolean isActiveEnergyBillingData(ObisCode obisCode) {
        return isActiveEnergy(obisCode) && (obisCode.getF() == 0);
    }

    private boolean isActiveEnergyCurrentData(ObisCode obisCode) {
        return isActiveEnergy(obisCode) && (obisCode.getF() == 255);
    }

    private boolean isApparentEnergyBillingData(ObisCode obisCode) {
        return isApparentEnergy(obisCode) && (obisCode.getF() == 0);
    }

    private boolean isApparentEnergyCurrentData(ObisCode obisCode) {
        return isApparentEnergy(obisCode) && (obisCode.getF() == 255);
    }

    private boolean isApparentEnergy(ObisCode obisCode) {
        return (obisCode.getA() == 1) && (obisCode.getB() == 0) && (obisCode.getC() == 9) && ((obisCode.getD() == 8) || (obisCode.getD() == 6));
    }

    private boolean isActiveEnergy(ObisCode obisCode) {
        return (obisCode.getA() == 1) && (obisCode.getB() == 0) && (obisCode.getC() == 1) && (obisCode.getD() == 8 || obisCode.getD() == 6);
    }
}