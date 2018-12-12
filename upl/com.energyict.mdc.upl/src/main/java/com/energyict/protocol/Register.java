package com.energyict.protocol;

import com.energyict.obis.ObisCode;

/**
 *
 * Date: 4-feb-2011
 * Time: 9:54:07
 */
public class Register {

    /**
     * Holds the ID of the AMR system his register
     */
    private final int rtuRegisterId;

    /**
     * The serial number of the rtu
     */
    private final String serialNumber;

    /**
     * The obiscode of the register
     */
    private final ObisCode obisCode;

    /**
     * This class identifies a register in an smart meter by its obis code and serial number of the (slave)device
     *
     * @param rtuRegisterId the ID of the Register BusinessObject
     * @param obisCode      the obisCode of the Register
     * @param serialNumber  the serialNumber of the device containing the Register
     */
    public Register(final int rtuRegisterId, final ObisCode obisCode, final String serialNumber) {
        this.rtuRegisterId = rtuRegisterId;
        this.obisCode = obisCode;
        this.serialNumber = serialNumber;
    }

    /**
     * The obiscode of the register
     *
     * @return the <code>ObisCode</code> of the Device
     */
    public ObisCode getObisCode() {
        return obisCode;
    }

    /**
     * The serial number of the (slave)device that has this register
     *
     * @return the serialNumber of the Device
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Getter for the ID of the Register
     *
     * @return the requested ID
     */
    public int getRtuRegisterId() {
        return this.rtuRegisterId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Register)) {
            return false;
        }

        Register register = (Register) o;

        if (obisCode != null ? !obisCode.equals(register.obisCode) : register.obisCode != null) {
            return false;
        } else if (serialNumber != null ? !serialNumber.equals(register.serialNumber) : register.serialNumber != null) {
            return false;
        } else if (this.rtuRegisterId != register.getRtuRegisterId()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = serialNumber != null ? serialNumber.hashCode() : 0;
        result = 31 * result + (obisCode != null ? obisCode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Register");
        sb.append("{obisCode=").append(obisCode);
        sb.append(", serialNumber='").append(serialNumber).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
