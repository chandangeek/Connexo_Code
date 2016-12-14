package com.energyict.mdc.upl.meterdata.identifiers;

import com.energyict.mdc.upl.meterdata.LogBook;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Provides functionality to identify a specific {@link LogBook} of a Device.
 * As mentioned in {@link Identifier}, the introspection mechanism
 * was designed with compatibility in mind.
 * Below is a list of currently known type names:
 * <table>
 * <tr><th>type name</th><th>comprising parts and their roles</th></tr>
 * <tr><td>DatabaseId</td><td>databaseValue -&gt; the log book's database identifier</td></tr>
 * <tr><td>DeviceIdentifierAndObisCode</td><td>device -&gt; the {@link DeviceIdentifier device's identifier}<br>obisCode -&gt; the ObisCode</td></tr>
 * <tr><td>Actual</td><td>databaseValue -&gt; the log book's database identifier<br>actual -&gt; the log book</td></tr>
 * </table>
 * <p/>
 * Copyrights EnergyICT
 * Date: 16/10/12
 * Time: 8:32
 */
public interface LogBookIdentifier extends Identifier {

    /**
     * Returns the LogBook that is uniquely identified by this identifier.
     * Note that this may throw a runtime exception when the LogBook could
     * either not be found or multiple log books were found in which case
     * this identifier was not as unique as you thought it was.
     *
     * @return the referenced LogBook
     */
    LogBook getLogBook();

    /**
     * Returns the ObisCode of the LogBook referenced by this identifier.
     *
     * @return The ObisCode
     */
    @XmlAttribute
    ObisCode getLogBookObisCode();

    /**
     * @return the DeviceIdentifier for this LogBookIdentifier
     */
    DeviceIdentifier getDeviceIdentifier();

}