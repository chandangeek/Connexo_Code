package com.energyict.mdc.upl.messages.legacy;

import com.energyict.mdc.upl.meterdata.LoadProfile;

import java.util.List;

/**
 * Extracts information that pertains to {@link LoadProfile}s
 * from message related objects for the purpose
 * of formatting it as an old-style device message.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-30 (15:11)
 */
public interface LoadProfileExtractor {
    /**
     * Extracts the unique identifier of a {@link LoadProfile}
     * and returns it as a String for easy formatting in XML based content.
     *
     * @param loadProfile The LoadProfile
     * @return The String representation of the LoadProfile's identifier
     */
    String id(LoadProfile loadProfile);

    /**
     * Extracts the deviceObisCode from the {@link LoadProfile}'s specification.
     *
     * @param loadProfile The LoadProfile
     * @return The string representation of the spec's deviceObisCode
     */
    String specDeviceObisCode(LoadProfile loadProfile);

    /**
     * Extracts the device's serial number from the {@link LoadProfile}.
     *
     * @param loadProfile The LoadProfile
     * @return The device's serial number
     */
    String deviceSerialNumber(LoadProfile loadProfile);

    /**
     * Extracts the {@link LoadProfile}'s channels.
     *
     * @param loadProfile The LoadProfile
     * @return The {@link Channel}s
     */
    List<Channel> channels(LoadProfile loadProfile);

    /**
     * Extracts the {@link LoadProfile}'s registers.
     *
     * @param loadProfile The LoadProfile
     * @return The {@link Register}s
     */
    List<Register> registers(LoadProfile loadProfile);

    interface Channel {
        String deviceSerialNumber();
        String obisCode();
        String unit();
    }

    interface Register {
        String deviceSerialNumber();
        String obisCode();
    }
}