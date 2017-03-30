/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.common.topology;

public class DeviceMapping {

    private final String serialNumber;
    private final int physicalAddress;
    private boolean ghostDevice = false;


    public DeviceMapping(String serialNumber, int physicalAddress, boolean ghostDevice) {
        this.serialNumber = serialNumber;
        this.physicalAddress = physicalAddress;
        this.ghostDevice = ghostDevice;
    }

    /**
     * 
     * @param serialNumber
     * @param physicalAddress
     */
    public DeviceMapping(String serialNumber, int physicalAddress) {
        this(serialNumber, physicalAddress, false);
    }

    /**
     *
     * @param serialNumber
     */
    public DeviceMapping(String serialNumber) {
        this(serialNumber, -1, true);
    }

    public DeviceMapping(String serialNumber, boolean ghostDevice) {
        this(serialNumber, -1, ghostDevice);
    }

    /**
     *
     * @return
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     *
     * @return
     */
    public int getPhysicalAddress() {
        return physicalAddress;
    }

    /**
     *
     * @return
     */
    public boolean isGhostDevice() {
        return ghostDevice;
    }

    /**
     * 
     * @param ghostDevice
     */
    public void setGhostDevice(boolean ghostDevice) {
        this.ghostDevice = ghostDevice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeviceMapping that = (DeviceMapping) o;
        if (serialNumber != null ? !serialNumber.equals(that.serialNumber) : that.serialNumber != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return  getSerialNumber().hashCode();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[").append(physicalAddress != -1 ? physicalAddress : "?").append("] = ").append(getSerialNumber());
        if (isGhostDevice()) {
            sb.append(" [GHOST]");
        }
        return sb.toString();
    }

}
