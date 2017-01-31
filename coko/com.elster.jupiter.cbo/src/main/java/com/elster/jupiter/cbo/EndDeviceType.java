/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

/**
 * See CIM Annex E, table E.1
 */
public enum EndDeviceType implements HasNumericCode {
    NA("NA", 0, "Not applicable. Use when a device type is not known."),
    COLLECTOR("Collector", 10, "A device that acts as a central point of communication between HES and devices located on premises."),
    COM_DEVICE("ComDevice", 26, "A communication device"),
    DAP_DEVICE("DAPDevice", 1, "A data aggregation point device"),
    DER_DEVICE("DERDevice", 2, "A demand response device"),
    DSP_DEVICE("DSPDevice", 6, "A digital signal processing device"),
    ELECTRIC_METER("ElectricMeter", 3, "A device located on premises to measure electricity usage."),
    ELECTRIC_VEHICLE("ElectricVehicle", 58, "(or Plug-in Electric Vehicle, PEV) a vehicle that can be plugged into the grid."),
    ENERGY_ROUTER("EnergyRouter", 23, "An energy router, analogous to the familiar data and communications router, automatically detects demand for power and delivers processed electricity in the required form (AC or DC) at the correct voltage and frequency on an electrical power system."),
    FEEDER("Feeder", 13, "Feeders carry three-phase power, and tend to follow the major streets near the substation."),
    GAS_METER("GasMeter", 4, "A device located on premises to measure gas usage."),
    GATEWAY("Gateway", 5, "A gateway device."),
    GENERATOR("Generator", 14, "Typically a spinning electrical generator. Something has to spin the generator -- it might be a water wheel in a hydroelectric dam, a large diesel engine or a gas turbine."),
    IN_PREMISES_DISPLAY("InPremisesDisplay(IPD/IHD)", 15, "The In-Premises (In-Home) Display (IPD/IHD) allows utility customers to track their energy usage in chart or graph form based upon kwH used."),
    LOAD_CONTROL_DEVICE("LoadControlDevice", 16, "A device used to implement “deferrable” services – commonly referred to as “off-peak”."),
    NETWORK_ROUTER("NetworkRouter", 11, "A router distributes Digital computer information that is contained within a data packet on a network."),
    PAN_DEVICE("PANDevice", 12, "A “premises area network” device that is not specifically described in further detail."),
    PAN_GATEWAY("PANGateway", 7, "A PAN gateway connects an external communications network to energy management devices within the premises."),
    PAN_METER("PANMeter", 17, "A “premises area network” device whose function is to measure (e.g. electricity usage)."),
    PREPAYMENT_TERMINAL("PrepaymentTerminal", 18, "A device that enables the customer to make advance payment before energy can be used."),
    PROG_CTL_THERMOSTAT("ProgCtlThermostat(PCT)", 19, "A thermostat device whose settings can be controlled via an API (ie. without human intervention)."),
    RANGE_EXTENDER("RangeExtender", 20, "Wireless range-extenders or wireless repeaters can extend the range of an existing wireless network."),
    REGULATOR("Regulator", 21, "A voltage regulator is an electrical regulator designed to automatically maintain a constant voltage level."),
    SUBSTATION("Substation", 22, "An electrical substation is a subsidiary station of an electricity generation, transmission and distribution system where voltage is transformed from high to low or the reverse using transformers."),
    TRANSFORMER("Transformer", 8, "A device that converts a generator's voltage (which is at the thousands of volts level) up to extremely high voltages for long-distance transmission on the transmission grid."),
    WASTE_WATER_METER("WasteWaterMeter", 25, "A device that measures waste water usage."),
    WATER_METER("WaterMeter", 24, "A device that measures water usage.");

    private final String description;
    private final int value;
    private final String mnemonic;

    EndDeviceType(String mnemonic, int value, String description) {
        this.description = description;
        this.value = value;
        this.mnemonic = mnemonic;
    }

    public String getDescription() {
        return description;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public int getValue() {
        return value;
    }

    public boolean isApplicable() {
        return NA != this;
    }

    public static EndDeviceType get(int value) {
        for (EndDeviceType endDeviceType : EndDeviceType.values()) {
            if (endDeviceType.getValue() == value) {
                return endDeviceType;
            }
        }
        throw new IllegalEnumValueException(EndDeviceType.class, value);
    }

    @Override
    public int getCode() {
        return value;
    }
}
