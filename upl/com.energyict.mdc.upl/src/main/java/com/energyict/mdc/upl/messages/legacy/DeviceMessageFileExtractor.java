package com.energyict.mdc.upl.messages.legacy;

import com.energyict.mdc.upl.properties.DeviceMessageFile;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Extracts information that pertains to {@link DeviceMessageFile}s
 * from message related objects for the purpose
 * of formatting it as an old-style device message.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-30 (15:02)
 */
public interface DeviceMessageFileExtractor {
    /**
     * Extracts the unique identifier of a {@link DeviceMessageFile}
     * and returns it as a String for easy formatting in XML based content.
     *
     * @param deviceMessageFile The DeviceMessageFile
     * @return The String representation of the DeviceMessageFile's identifier
     */
    String id(DeviceMessageFile deviceMessageFile);

    String name(DeviceMessageFile deviceMessageFile);

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
}