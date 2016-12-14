package com.energyict.mdc.upl.meterdata.identifiers;

import com.energyict.mdc.upl.meterdata.LoadProfile;

import com.energyict.obis.ObisCode;

/**
 * Provides functionality to identify a specific LoadProfile of a Device.
 * <br>
 * As mentioned in {@link Identifier}, the introspection mechanism
 * was designed with compatibility in mind.
 * Below is a list of currently known type names:
 * <table>
 * <tr><th>type name</th><th>comprising parts and their roles</th></tr>
 * <tr><td>DatabaseId</td><td>databaseValue -&gt; the load profile's database identifier</td></tr>
 * <tr><td>DeviceIdentifierAndObisCode</td><td>device -&gt; the {@link DeviceIdentifier device's identifier}<br>obisCode -&gt; the ObisCode</td></tr>
 * <tr><td>FirstLoadProfileOnDevice</td><td>device -&gt; the {@link DeviceIdentifier device's identifier}</td></tr>
 * <tr><td>Actual</td><td>databaseValue -&gt; the load profile's database identifier<br>actual -&gt; the load profile</td></tr>
 * </table>
 * <p/>
 * Copyrights EnergyICT
 * Date: 15/10/12
 * Time: 14:01
 */
public interface LoadProfileIdentifier extends Identifier {

    /**
     * Returns the LoadProfile that is uniquely identified by this identifier.
     * Note that this may throw a runtime exception when the LoadProfile could
     * either not be found or multiple load profiles were found in which case
     * this identifier was not as unique as you thought it was.
     *
     * @return the LoadProfile
     */
    LoadProfile getLoadProfile();

    ObisCode getProfileObisCode();

    /**
     * @return the DeviceIdentifier for this LoadProfileIdentifier
     */
    DeviceIdentifier getDeviceIdentifier();

}