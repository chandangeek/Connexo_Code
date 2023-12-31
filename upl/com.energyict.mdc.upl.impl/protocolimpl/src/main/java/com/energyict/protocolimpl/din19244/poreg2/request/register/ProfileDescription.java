package com.energyict.protocolimpl.din19244.poreg2.request.register;

/**
 * The device stores multiple profile definitions.
 * Each entry is an instance of this class, containing a number of registers.
 *
 * Copyrights EnergyICT
 * Date: 5-mei-2011
 * Time: 11:07:12
 */
public class ProfileDescription {

    private int gid;
    private int registerAddresses;
    private int fieldAddresses;
    private int numberOfRegisters;
    private int numberOfFields;
    private int profileId;
    private int interval;
    private int length;

    public ProfileDescription(int length, int interval, int profileId, int fieldAddresses, int gid, int numberOfFields, int numberOfRegisters, int registerAddresses) {
        this.length = length;
        this.interval = interval;
        this.profileId = profileId;
        this.fieldAddresses = fieldAddresses;
        this.gid= gid;
        this.numberOfFields = numberOfFields;
        this.numberOfRegisters = numberOfRegisters;
        this.registerAddresses = registerAddresses;
    }

    public int getLength() {
        return length;
    }

    public int getProfileId() {
        return profileId;
    }

    public int getInterval() {
        return interval;
    }

    public int getFieldAddresses() {
        return fieldAddresses;
    }

    public int getGid() {
        return gid;
    }

    public int getNumberOfFields() {
        return numberOfFields;
    }

    public int getNumberOfRegisters() {
        return numberOfRegisters;
    }

    public int getRegisterAddresses() {
        return registerAddresses;
    }
}
