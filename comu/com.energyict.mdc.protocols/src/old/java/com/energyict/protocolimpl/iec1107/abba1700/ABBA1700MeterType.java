/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ABBA1700MeterType.java
 *
 * Created on 24 juni 2004, 8:35
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.mdc.protocol.api.inbound.MeterType;

/**
 * @author Koen
 */
public class ABBA1700MeterType {

    /**
     * List of extended meters. This is the 'Device No.' The 'Device No' is used for the firmware issue.
     */
    private static final String[] EXTENDED_TOU_TYPES = new String[]{"010", "012", "017", "020", "026"};
    private static final String[] EXTENDED_CDR_TYPES = new String[]{"026"};

    public static final int METERTYPE_UNASSIGNED = -1;
    public static final int METERTYPE_16_TOU = 0;
    public static final int METERTYPE_32_TOU = 1;
    public static final int METERTYPE_32_TOU_5_CDR = 2;

    private int nrOfTariffRegisters = -1;
    private int extraOffsetHistoricDisplayScaling = -1;
    private String productRange = "";
    private String deviceNr = "";
    private String issueNr = "";
    private boolean hasExtendedCustomerRegisters = false;
    private int sizeOfScalingSet;
    private int sizeOfCDSource;

    private int type;

    /**
     * Creates a new instance of MeterType
     */
    public ABBA1700MeterType(int type) {
        if (type == METERTYPE_16_TOU) {
            nrOfTariffRegisters = 16;
            extraOffsetHistoricDisplayScaling = 0;
            hasExtendedCustomerRegisters = false;
            sizeOfScalingSet = 8*2;
            sizeOfCDSource = 6;
        } else if (type == METERTYPE_32_TOU) {
            nrOfTariffRegisters = 32;
            extraOffsetHistoricDisplayScaling = 124;
            hasExtendedCustomerRegisters = false;
            sizeOfScalingSet = 8*2;
            sizeOfCDSource = 6;
        } else if (type == METERTYPE_32_TOU_5_CDR) {
            nrOfTariffRegisters = 32;
            extraOffsetHistoricDisplayScaling = 124;
            hasExtendedCustomerRegisters = true;
            sizeOfScalingSet = 8*2;
            sizeOfCDSource = 15;
        }
        this.type = type;
    }

    public int getType(){
        return this.type;
    }

    /**
     * Create a new meter type using the ident string from the MeterType object
     *
     * @param meterType
     */
    public ABBA1700MeterType(MeterType meterType) {
        updateWith(meterType);
    }

    /**
     * Initialise this object with a new meter type.
     * The exact meter type is extracted from the Ident string from the meter.
     * <p/>
     * <pre>
     * /GEC5090100100400@000
     * GEC        Manufacturers id (We were GEC, then ABB now Elster, but the original one has been kept for continuity)
     * 5          Baud Rate ( 0 - 300, 1 - 600, 2 - 1200, 3 - 2400, 4 - 4800, 5 - 9600)
     * 09         Master Unit Id (09 - A1700), 01 - 08 PPM)
     * 010        Product Range (010 - A1700, 001 - PPM)
     * 010        Device No. The Device No is used for the firmware issue.
     * 04         Issue No.
     * </pre>
     *
     * @param meterType
     */
    public void updateWith(MeterType meterType) {
        this.deviceNr = getDeviceNrFromMeterType(meterType);
        this.issueNr = getIssueNrFromMeterType(meterType);
        this.productRange = getProductRangeFromMeterType(meterType);
        hasExtendedCustomerRegisters = isExtendedCdrMeterType();
        if (isExtendedTouMeterType()) {
            if (hasExtendedCustomerRegisters) { // METERTYPE_32_TOU_5_CDR
            nrOfTariffRegisters = 32;
            extraOffsetHistoricDisplayScaling = 124;
                sizeOfScalingSet = 8 * 2;
                sizeOfCDSource = 15;
            } else {                            // METERTYPE_32_TOU
                nrOfTariffRegisters = 32;
                extraOffsetHistoricDisplayScaling = 124;
                sizeOfScalingSet = 8 * 2;
                sizeOfCDSource = 6;
            }
        } else {                                // METERTYPE_16_TOU
            nrOfTariffRegisters = 16;
            extraOffsetHistoricDisplayScaling = 0;
            sizeOfScalingSet = 8*2;
            sizeOfCDSource = 6;
        }
    }

    /**
     * @return the firmware version of the meter based on the MeterType id
     */
    public String getFirmwareVersion(){
        return "ProductRange: " + productRange + " Device No. " + deviceNr.concat(".").concat(issueNr);
    }

    /**
     * Get the DeviceNr from the meterType string.
     * ex. metertype = GEC5090100100400@000 -> returnString = 010
     *
     * @param meterType the given Metertype
     * @return the deviceNr.
     */
    protected String getDeviceNrFromMeterType(final MeterType meterType) {
        return meterType.getReceivedIdent().substring(10, 13);
    }

    /**
     * Get the product range from the meterType string.
     * ex. meterType = GEC5090100100400@000 -> productRange = 010
     *
     * @param meterType the given Metertype
     * @return the product range
     */
    protected String getProductRangeFromMeterType(final MeterType meterType) {
        return meterType.getReceivedIdent().substring(7, 10);
    }

    /**
     * Get the issueNr from the meterType string.
     * ex. meterType = GEC5090100100400@000 -> issueNr = 04
     *
     * @param meterType the given MeteRType
     * @return the issue nr.
     */
    protected String getIssueNrFromMeterType(final MeterType meterType) {
        return meterType.getReceivedIdent().substring(13, 15);
    }

    /**
     * Checks if this meter is an extended type by checking the deviceNr
     *
     * @return true is the meter is an extended type.
     */
    private boolean isExtendedTouMeterType() {
        for (int i = 0; i < EXTENDED_TOU_TYPES.length; i++) {
            String meterType = EXTENDED_TOU_TYPES[i];
            if (meterType.compareTo(deviceNr) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if this meter is an extended type by checking the deviceNr
     *
     * @return true is the meter is an extended type.
     */
    private boolean isExtendedCdrMeterType() {
        for (int i = 0; i < EXTENDED_CDR_TYPES.length; i++) {
            String meterType = EXTENDED_CDR_TYPES[i];
            if (meterType.compareTo(deviceNr) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the object is initialised with a correct meter type
     *
     * @return
     */
    public boolean isAssigned() {
        return ((getExtraOffsetHistoricDisplayScaling() != -1) &&
                (getNrOfTariffRegisters() != -1));
    }

    /**
     * Getter for property nrOfTariffRegisters.
     *
     * @return Value of property nrOfTariffRegisters.
     */
    public int getNrOfTariffRegisters() {
        return nrOfTariffRegisters;
    }

    /**
     * Getter for property extraOffsetHistoricDisplayScaling.
     *
     * @return Value of property extraOffsetHistoricDisplayScaling.
     */
    public int getExtraOffsetHistoricDisplayScaling() {
        return extraOffsetHistoricDisplayScaling;
    }

    /**
     * Checks if this meterType has extended customer defined registers.
     * These extended registers can combine up to 5 registers to one.
     * Normal CDR's can only combine two registers
     *
     * @return
     */
    public boolean hasExtendedCustomerRegisters() {
        return hasExtendedCustomerRegisters;
    }

    public int getDisplayScalingTOUOffset(){
        return this.sizeOfScalingSet + this.sizeOfCDSource;
}
}
