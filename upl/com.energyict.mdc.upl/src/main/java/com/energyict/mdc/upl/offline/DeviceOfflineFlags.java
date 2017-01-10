package com.energyict.mdc.upl.offline;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Copyrights EnergyICT
 * Date: 15/03/13
 * Time: 13:19
 */
@XmlRootElement
public class DeviceOfflineFlags implements OfflineDeviceContext, Serializable {

    public static final int REGISTERS_FLAG = 0b0000_0000_0000_0000_0000_0000_0000_0001;
    public static final int SLAVE_DEVICES_FLAG = 0b0000_0000_0000_0000_0000_0000_0000_0010;
    public static final int MASTER_LOAD_PROFILES_FLAG = 0b0000_0000_0000_0000_0000_0000_0000_0100;
    public static final int ALL_LOAD_PROFILES_FLAG = 0b0000_0000_0000_0000_0000_0000_0000_1000;
    public static final int LOG_BOOKS_FLAG = 0b0000_0000_0000_0000_0000_0000_0001_0000;
    public static final int PENDING_MESSAGES_FLAG = 0b0000_0000_0000_0000_0000_0000_0010_0000;
    public static final int SENT_MESSAGES_FLAG = 0b0000_0000_0000_0000_0000_0000_0100_0000;
    public static final int ALL_FLAG = 0b0000_0000_0000_0000_0000_0000_0111_1111;

    private final int flags;

    public DeviceOfflineFlags(int... flags) {
        this.flags = or(flags);
    }

    /**
     * For JSON serializing only
     */
    private DeviceOfflineFlags() {
        this.flags = 0;
    }

    @XmlAttribute
    public int getFlags() {
        return flags;
    }

    public DeviceOfflineFlags or(OfflineDeviceContext context) {
        if (context instanceof DeviceOfflineFlags) {
            DeviceOfflineFlags other = (DeviceOfflineFlags) context;
            return new DeviceOfflineFlags(flags | other.flags);
        }
        int flag = flags;
        if (context.needsLogBooks()) {
            flag |= LOG_BOOKS_FLAG;
        }
        if (context.needsMasterLoadProfiles()) {
            flag |= MASTER_LOAD_PROFILES_FLAG;
        }
        if (context.needsAllLoadProfiles()) {
            flag |= ALL_LOAD_PROFILES_FLAG;
        }
        if (context.needsSlaveDevices()) {
            flag |= SLAVE_DEVICES_FLAG;
        }
        if (context.needsRegisters()) {
            flag |= REGISTERS_FLAG;
        }
        if (context.needsPendingMessages()) {
            flag |= PENDING_MESSAGES_FLAG;
        }
        if (context.needsSentMessages()) {
            flag |= SENT_MESSAGES_FLAG;
        }
        return new DeviceOfflineFlags(flag);
    }

    @Override
    public boolean needsAllLoadProfiles() {
        return isSet(ALL_LOAD_PROFILES_FLAG);
    }

    @Override
    public boolean needsLogBooks() {
        return isSet(LOG_BOOKS_FLAG);
    }

    @Override
    public boolean needsMasterLoadProfiles() {
        return isSet(MASTER_LOAD_PROFILES_FLAG);
    }

    @Override
    public boolean needsPendingMessages() {
        return isSet(PENDING_MESSAGES_FLAG);
    }

    @Override
    public boolean needsRegisters() {
        return isSet(REGISTERS_FLAG);
    }

    @Override
    public boolean needsSentMessages() {
        return isSet(SENT_MESSAGES_FLAG);
    }

    @Override
    public boolean needsSlaveDevices() {
        return isSet(SLAVE_DEVICES_FLAG);
    }

    private boolean isSet(int flag) {
        return (flags & flag) != 0;
    }

    private int or(int[] flags) {
        int or = 0;
        for (int flag : flags) {
            or |= flag;
        }
        return or;
    }

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For JSON serializing only
    }

}