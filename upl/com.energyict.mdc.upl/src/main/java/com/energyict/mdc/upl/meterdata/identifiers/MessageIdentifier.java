package com.energyict.mdc.upl.meterdata.identifiers;

import com.energyict.mdc.upl.messages.DeviceMessage;

/**
 * Provides functionality to uniquely identify a {@link DeviceMessage}.
 * Below is a list of currently known type names:
 * <table>
 * <tr><th>type name</th><th>comprising parts and their roles</th></tr>
 * <tr><td>DatabaseId</td><td>databaseValue -&gt; the message's database identifier</td></tr>
 * <tr><td>DeviceIdentifierAndProtocolInfoParts</td><td>device -&gt; the {@link DeviceIdentifier device's identifier}<br>protocolInfo -&gt; protocol specific information in the form of String[]</td></tr>
 * <tr><td>Actual</td><td>databaseValue -&gt; the message's database identifier<br>actual -&gt; the device message</td></tr>
 * </table>
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/03/13
 * Time: 8:59
 */
public interface MessageIdentifier extends Identifier {
}