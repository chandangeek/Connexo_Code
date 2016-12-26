package com.energyict.mdc.upl.messages.legacy;

import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.NumberLookup;
import com.energyict.mdc.upl.properties.TariffCalender;

import com.energyict.obis.ObisCode;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

/**
 * Extracts information from message related objects
 * for the purpose of formatting it as an old-style device message.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-23 (13:30)
 */
public interface Extractor {

    /**
     * Extracts the serial number from the {@link Device}.
     *
     * @param device The Device
     * @return The serial number
     */
    String serialNumber(Device device);

    /**
     * Extracts the {@link com.energyict.mdc.upl.meterdata.Register} with the specified {@link ObisCode}
     * from the {@link Device}.
     *
     * @param device The Device
     * @param obisCode The ObisCode
     * @return The Register or an empty Optional if the Device does not contain a register for the ObisCode
     */
    Optional<com.energyict.mdc.upl.meterdata.Register> register(Device device, ObisCode obisCode);

    /**
     * Extracts the last reading from the {@link com.energyict.mdc.upl.meterdata.Register}.
     *
     * @param register The Register
     * @return The last registered reading or an empty Optional not readings have been registered yet
     */
    Optional<RegisterReading> lastReading(com.energyict.mdc.upl.meterdata.Register register);

    /**
     * Extracts the unique identifier of a {@link DeviceMessageFile}
     * and returns it as a String for easy formatting in XML based content.
     *
     * @param deviceMessageFile The DeviceMessageFile
     * @return The String representation of the DeviceMessageFile's identifier
     */
    String id(DeviceMessageFile deviceMessageFile);

    /**
     * Extracts the entire contents of the {@link DeviceMessageFile}
     * and returns it as a String for easy formatting in XML based content.
     * Note that calling this may consume a lot of memory if the file is big.
     *
     * @param deviceMessageFile The DeviceMessageFile
     * @return The entire contents of the DeviceMessageFile
     */
    String contents(DeviceMessageFile deviceMessageFile);

    /**
     * Extracts the entire contents of the {@link DeviceMessageFile}
     * and returns it as a String for easy formatting in XML based content.
     * Note that calling this may consume a lot of memory if the file is big.
     *
     * @param deviceMessageFile The DeviceMessageFile
     * @return The entire contents of the DeviceMessageFile
     */
    String contents(DeviceMessageFile deviceMessageFile, Charset charset);

    /**
     * Extracts the unique identifier of a {@link TariffCalender}
     * and returns it as a String for easy formatting in XML based content.
     *
     * @param calender The TariffCalender
     * @return The String representation of the TariffCalender's identifier
     */
    String id(TariffCalender calender);

    /**
     * Extracts the unique identifier of a {@link NumberLookup}
     * and returns it as a String for easy formatting in XML based content.
     *
     * @param numberLookup The NumberLookup
     * @return The String representation of the NumberLookup's identifier
     */
    String id(NumberLookup numberLookup);

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

    interface RegisterReading {
        String text();
    }

}