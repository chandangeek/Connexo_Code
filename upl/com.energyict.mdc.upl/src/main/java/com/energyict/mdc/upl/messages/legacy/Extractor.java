package com.energyict.mdc.upl.messages.legacy;

import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.NumberLookup;
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.energyict.obis.ObisCode;
import com.google.common.collect.Range;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

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

    String name (DeviceMessageFile deviceMessageFile);

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
    byte[] binaryContents(DeviceMessageFile deviceMessageFile);

    /**
     * Extracts the entire contents of the {@link DeviceMessageFile}
     * and returns it as a String for easy formatting in XML based content.
     * Note that calling this may consume a lot of memory if the file is big.
     *
     * @param deviceMessageFile The DeviceMessageFile
     * @param charSetName The name of the CharSet that should be used to convert bytes to String
     * @return The entire contents of the DeviceMessageFile
     * @throws UnsupportedEncodingException Thrown when the charSetName is not supported
     */
    String contents(DeviceMessageFile deviceMessageFile, String charSetName) throws UnsupportedEncodingException;

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
     * Extracts the unique identifier of a {@link TariffCalendar}
     * and returns it as a String for easy formatting in XML based content.
     *
     * @param calender The TariffCalendar
     * @return The String representation of the TariffCalendar's identifier
     */
    String id(TariffCalendar calender);

    String name (TariffCalendar calender);

    String seasonSetId(TariffCalendar calender);

    TimeZone definitionTimeZone(TariffCalendar calender);

    TimeZone destinationTimeZone(TariffCalendar calender);

    int intervalInSeconds(TariffCalendar calender);

    Range<Year> range(TariffCalendar calender);

    Optional<CalendarSeasonSet> season(TariffCalendar calender);

    List<CalendarDayType> dayTypes(TariffCalendar calender);

    List<CalendarRule> rules(TariffCalendar calender);

    /**
     * Extracts the unique identifier of a {@link NumberLookup}
     * and returns it as a String for easy formatting in XML based content.
     *
     * @param numberLookup The NumberLookup
     * @return The String representation of the NumberLookup's identifier
     */
    String id(NumberLookup numberLookup);

    List<String> keys(NumberLookup numberLookup);

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

    interface CalendarSeasonSet {
        String id();
        String name();
        List<CalendarSeason> seasons();
    }

    interface CalendarSeason {
        String id();
        String name();
        List<CalendarSeasonTransition> transistions();
    }

    interface CalendarSeasonTransition {
        String id();
        Optional<Instant> start();
    }

    interface CalendarDayType {
        String id();
        String name();
        List<CalendarDayTypeSlice> slices();
    }

    interface CalendarDayTypeSlice {
        String tariffCode();
        LocalTime start();
    }

    interface CalendarRule {
        String dayTypeId();
        String dayTypeName();
        Optional<String> seasonId();
        int year();
        int month();
        int day();
        int dayOfWeek();
    }

}