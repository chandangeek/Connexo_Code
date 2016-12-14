package com.energyict.mdc.upl.meterdata.identifiers;

import com.energyict.mdc.upl.meterdata.Device;

/**
 * Identifies a device that started inbound communication
 * and is also capable of finding that device.
 * <br>
 * As mentioned in {@link Identifier}, the introspection mechanism
 * was designed with compatibility in mind.
 * Below is a list of currently known type names:
 * <table>
 * <tr><th>type name</th><th>comprising parts and their roles</th></tr>
 * <tr><td>SerialNumber</td><td>serialNumber -&gt; the device's serial number</td></tr>
 * <tr><td>LikeSerialNumber</td><td>serialNumberGrepPattern -&gt; the grep pattern to match the device's serial number</td></tr>
 * <tr><td>DatabaseId</td><td>databaseValue -&gt; the device's database identifier</td></tr>
 * <tr><td>CallHomeId</td><td>callHomeId -&gt; the device's callHomeId property</td></tr>
 * <tr><td>PhoneNumber</td><td>phoneNumber -&gt; the device's phoneNumber property</td></tr>
 * <tr><td>Actual</td><td>databaseValue -&gt; the device's database identifier<br>actual -&gt; the device</td></tr>
 * </table>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-12 (10:56)
 */
public interface DeviceIdentifier extends Identifier {

    /**
     * Finds the device that is uniquely identified by this DeviceIdentifier.
     * Note that this may throw a runtime exception when the Device could
     * either not be found or multiple devices were found in which case
     * this identifier was not as unique as you thought it was.
     *
     * @return The device
     */
    Device findDevice();

}