package com.energyict.mdc.upl.messages;

import java.util.EnumSet;
import java.util.Set;

/**
 * Models the status of a {@link DeviceMessage} where each status
 * is actually an element of the lifecycle of a DeviceMessage.
 * The normal lifecycle of a DeviceMessage is described by the DeviceMessage class.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-15 (16:21)
 */
public enum DeviceMessageStatus {

    /**
     * Applies to a {@link DeviceMessage} that was created with a release date in the future
     * and is waiting for that date to expire.
     */
    WAITING {
        @Override
        Set<DeviceMessageStatus> predecessors() {
            return EnumSet.noneOf(DeviceMessageStatus.class);
        }

        @Override
        Set<DeviceMessageStatus> successors() {
            return EnumSet.of(PENDING, CANCELED);
        }
    },

    /**
     * Applies to a {@link DeviceMessage} whose release date has expired
     * (or maybe did not have a release date) and is waiting to be sent
     * to the device.
     */
    PENDING {
        @Override
        Set<DeviceMessageStatus> predecessors() {
            return EnumSet.of(WAITING);
        }

        @Override
        Set<DeviceMessageStatus> successors() {
            return EnumSet.of(CANCELED, CONFIRMED, FAILED, INDOUBT, SENT);
        }
    },

    /**
     * Applies to a {@link DeviceMessage} that was canceled by the user.
     */
    CANCELED {
        @Override
        Set<DeviceMessageStatus> predecessors() {
            return EnumSet.of(WAITING, PENDING);
        }

        @Override
        Set<DeviceMessageStatus> successors() {
            return EnumSet.noneOf(DeviceMessageStatus.class);
        }
    },

    /**
     * Applies to a {@link DeviceMessage} that was sent successfully to the device
     * but is awaiting processing or execution by the device.
     */
    SENT {
        @Override
        Set<DeviceMessageStatus> predecessors() {
            return EnumSet.of(PENDING);
        }

        @Override
        Set<DeviceMessageStatus> successors() {
            return EnumSet.of(CONFIRMED, FAILED, INDOUBT);
        }
    },

    /**
     * Applies to a {@link DeviceMessage} that was executed succesfully by the device.
     */
    CONFIRMED {
        @Override
        Set<DeviceMessageStatus> predecessors() {
            return EnumSet.of(PENDING, SENT);
        }

        @Override
        Set<DeviceMessageStatus> successors() {
            return EnumSet.noneOf(DeviceMessageStatus.class);
        }
    },

    /**
     * Applies to a {@link DeviceMessage} whose execution by the device failed.
     */
    FAILED {
        @Override
        Set<DeviceMessageStatus> predecessors() {
            return EnumSet.of(PENDING, SENT);
        }

        @Override
        Set<DeviceMessageStatus> successors() {
            return EnumSet.noneOf(DeviceMessageStatus.class);
        }
    },

    /**
     * Applies to a {@link DeviceMessage} whose execution result cannot be retrieved
     * and it is therefore unclear what the result was.
     */
    INDOUBT {
        @Override
        Set<DeviceMessageStatus> predecessors() {
            return EnumSet.of(PENDING, SENT);
        }

        @Override
        Set<DeviceMessageStatus> successors() {
            return EnumSet.noneOf(DeviceMessageStatus.class);
        }
    };

    public int dbValue () {
        return this.ordinal();
    }

    public static DeviceMessageStatus fromDb (int value) {
        for (DeviceMessageStatus deviceMessageStatus : values()) {
            if (deviceMessageStatus.dbValue() == value) {
                return deviceMessageStatus;
            }
        }
        return null;
    }


    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

    abstract Set<DeviceMessageStatus> predecessors();

    abstract Set<DeviceMessageStatus> successors();

    public boolean isSuccessorOf(DeviceMessageStatus other) {
        return predecessors().contains(other);
    }

    public boolean isPredecessorOf(DeviceMessageStatus other) {
        return successors().contains(other);
    }

}