/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

public enum EndDeviceDomain implements HasNumericCode {
    NA("NA", 0, "Not applicable. Use when a domain is not needed. This should rarely be used."),
    ASSOCIATEDDEVICE("AssociatedDevice", 39, "A device(for example, a relay)that can be associated with an end device."),
    BATTERY("Battery", 2, "Any events or controls related to a device battery."),
    BILLING("Billing", 20, "Events or controls related to cost of energy (Including Pricing, Tariff, TOU, etc.)."),
    CARTRIDGE("Cartridge", 3, "Events or controls related to cartridge fuses."),
    CLOCK("Clock", 36, "Events or controls related to a device internal clock."),
    COMMUNICATION("Communication", 1, "Events or controls related to purely communication issues. Consider other domains before using this one."),
    CONFIGURATION("Configuration", 7, "Events or controls related to device configuration."),
    DEMAND("Demand", 8, "Events or controls related to demand (ie. kW) and demand settings (as opposed to consumption (ie. kWh)."),
    FIRMWARE("Firmware", 11, "Events or controls related to device firmware."),
    GASSUPPLY("GasSupply", 4, "Events or controls related to the supply of natural gas or propane."),
    INSTALLATION("Installation", 6, "Events or controls related to device installation."),
    KYZPULSECOUNTER("KYZPulseCounter", 38, "Pulse counting function inside a meter or other end device."),
    LOADCONTROL("LoadControl", 15, "Events or controls related to the automatic restriction or control of a customer’s energy consumption."),
    LOADPROFILE("LoadProfile", 16, "Events or controls related to the energy consumption (ie. “load”) over time on a device."),
    LOGS("Logs", 17, "Events or controls related to device internal logs."),
    MEMORY("Memory", 18, "Events or controls related to device memory."),
    METROLOGY("Metrology", 21, "Events or controls related to any type of measurement captured by a device."),
    MOBILESECURITY("MobileSecurity", 14, "Events or controls related to device security when the device is accessed via a mobile tool or device."),
    MODEM("Modem", 19, "Events or controls related to a device’s modem."),
    MODULEFIRMWARE("ModuleFirmware", 9, "Events or controls related to firmware on a module contained by a device."),
    NETWORK("Network", 23, "Events or controls generally related to a device’s status on the network. Also used for general network events, such as commissioning of a PAN Area network."),
    PAIRING("Pairing", 10, "Events or controls related to linking devices together (e.g. PANDevice to Meter, ComDevice to Meter, etc.)."),
    POWER("Power", 26, "Events or controls related to device energization status."),
    PRESSURE("Pressure", 29, "Events or controls related to device pressure thresholds."),
    RCDSWITCH("RCDSwitch", 31, "Events or controls related to device remote connect/disconnect activities."),
    RECODER("Recoder", 41, "A device for encoding"),
    SECURITY("Security", 12, "Events or controls related to device security (including SecurityKey, HMAC, Parity, Rotation, other TamperDetection, etc.)."),
    TEMPERATURE("Temperature", 35, "Events or controls related to device"),
    VIDEODISPLAY("VideoDisplay", 13, "Events or controls related to device CRT/display."),
    VOLUME("Volume", 40, "A quantity of 3-dimensional space enclosed by a boundary; the space occupied by a liquid or gas."),
    WATERSUPPLY("WaterSupply", 5, "Events or controls related to the supply of water."),
    WATCHDOG("Watchdog", 37, "A hardware or software function triggered by a timer expiring.");


    EndDeviceDomain(String mnemonic, int value, String description) {
        this.description = description;
        this.mnemonic = mnemonic;
        this.value = value;
    }

    private final String mnemonic;
    private final int value;
    private final String description;

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

    public static EndDeviceDomain get(int value) {
        for (EndDeviceDomain endDeviceDomain : EndDeviceDomain.values()) {
            if (endDeviceDomain.getValue() == value) {
                return endDeviceDomain;
            }
        }
        throw new IllegalEnumValueException(EndDeviceDomain.class, value);
    }

    @Override
    public int getCode() {
        return value;
    }
}
