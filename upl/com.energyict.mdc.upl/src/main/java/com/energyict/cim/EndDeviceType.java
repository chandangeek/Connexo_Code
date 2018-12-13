/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.cim;

/**
 * Created by IntelliJ IDEA.
 * User: jbr
 * Date: 16-aug-2011
 * Time: 15:35:56
 */
public enum EndDeviceType implements CimMnemonicProvider {

    NOT_APPLICABLE(0, "NotApplicable"),
    DAPDEVICE(1, "DAPDevice"),
    DERDEVICE(2, "DERDevice"),
    ELECTRICMETER(3, "ElectricMeter"),
    GASMETER(4, "GasMeter"),
    GATEWAY(5, "Gateway"),
    DSPDEVICE(6, "DSPDevice"),
    PANGATEWAY(7, "PANGateway"),
    TRANSFORMER(8, "Transformer"),
    COLLECTOR(10, "Collector"),
    NETWORKROUTER(11, "NetworkRouter"),
    PANDEVICE(12, "PANDevice"),
    FEEDER(13, "Feeder"),
    GENERATOR(14, "Generator"),
    INPREMISESDISPLAY(15, "InPremisesDisplay"),
    LOADCONTROLDEVICE(16, "LoadControlDevice"),
    PANMETER(17, "PANMeter"),
    PREPAYMENTTERMINAL(18, "PrepaymentTerminal"),
    PROGCTLTHERMOSTAT(19, "ProgCtlThermostat"),
    RANGEEXTENDER(20, "RangeExtender"),
    REGULATOR(21, "Regulator"),
    SUBSTATION(22, "Substation"),
    ENERGYROUTER(23, "EnergyRouter"),
    WATERMETER(24, "WaterMeter"),
    WASTEWATERMETER(25, "WasteWaterMeter"),
    COMDEVICE(26, "ComDevice"),
    ELECTRICVEHICLE(58, "ElectricVehicle");

    private int value;
    private String mnemonic;

    private EndDeviceType(int value, String mnemonic) {
        this.value = value;
        this.mnemonic = mnemonic;
    }

    public int getValue() {
        return value;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public String getTranslationKey() {
        return "dt" + mnemonic;
    }

    public static EndDeviceType fromValue(int value) {
        for (int i = 0; i < EndDeviceType.values().length; i++) {
            EndDeviceType type = EndDeviceType.values()[i];
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getMnemonic();
    }
}
