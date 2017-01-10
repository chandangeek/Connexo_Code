package com.energyict.mdc.upl.meterdata.identifiers;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Provides functionality to identify a specific Register by it's ObisCode.
 * Below is a list of currently known type names:
 * <table>
 * <tr><th>type name</th><th>comprising parts and their roles</th></tr>
 * <tr><td>DatabaseId</td><td>databaseValue -&gt; the register's database identifier</td></tr>
 * <tr><td>DeviceIdentifierAndObisCode</td><td>device -&gt; the {@link DeviceIdentifier register's identifier}<br>obisCode -&gt; the ObisCode</td></tr>
 * <tr><td>PrimeRegisterForChannel</td><td>device -&gt; the {@link DeviceIdentifier register's identifier}<br>channelIndex -&gt; the channel index<br>obisCode -&gt; the ObisCode</td></tr>
 * <tr><td>Actual</td><td>databaseValue -&gt; the register's database identifier<br>register -&gt; the register<br>actual -&gt; the register</td></tr>
 * </table>
 * <p/>
 * Copyrights EnergyICT
 * Date: 15/10/12
 * Time: 13:51
 */
public interface RegisterIdentifier extends Identifier {
    @Override
    Introspector forIntrospection();

    /**
     * Gets the ObisCode of the register that is uniquely identified by this RegisterIdentifier.
     */
    @XmlAttribute
    ObisCode getRegisterObisCode();

}