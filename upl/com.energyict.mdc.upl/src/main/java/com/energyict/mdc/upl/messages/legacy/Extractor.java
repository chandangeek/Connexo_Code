package com.energyict.mdc.upl.messages.legacy;

import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.NumberLookup;
import com.energyict.mdc.upl.properties.TariffCalender;

import java.nio.charset.Charset;

/**
 * Extracts information from message related objects
 * for the purpose of formatting it as an old-style device message.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-23 (13:30)
 */
public interface Extractor {

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

}