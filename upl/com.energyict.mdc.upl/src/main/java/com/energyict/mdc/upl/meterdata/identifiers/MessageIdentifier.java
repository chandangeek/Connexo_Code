package com.energyict.mdc.upl.meterdata.identifiers;

import com.energyict.mdc.upl.messages.DeviceMessage;

import java.util.Arrays;

/**
 * Provides functionality to uniquely identify a {@link DeviceMessage}.
 * Below is a list of currently known type names:
 * <table>
 * <tr><th>type name</th><th>comprising parts and their roles</th></tr>
 * <tr><td>DatabaseId</td><td>databaseValue -&gt; the message's database identifier<br>device -&gt; the {@link DeviceIdentifier device's identifier}</td></tr>
 * <tr><td>DeviceIdentifierAndProtocolInfoParts</td><td>device -&gt; the {@link DeviceIdentifier device's identifier}<br>protocolInfo -&gt; protocol specific information in the form of String[]</td></tr>
 * <tr><td>Actual</td><td>databaseValue -&gt; the message's database identifier<br>actual -&gt; the device message<br>device -&gt; the {@link DeviceIdentifier device's identifier}</td></tr>
 * </table>
 * <p>
 *
 * Date: 22/03/13
 * Time: 8:59
 */
public interface MessageIdentifier extends Identifier {

    DeviceIdentifier getDeviceIdentifier();

    /**
     * Start of fluent API to check if the specified MessageIdentifier
     * represents the same Message as another MessageIdentifier.
     * We avoid implementing this on the MessageIdentifier implementation
     * classes because returning <code>true</code> in equals method
     * requires that both objects return the same hash code
     * but different implementations may use completely different
     * Message properties to calculate the hash.
     *
     * @param subject The subject of the equals check
     * @return <code>true</code> iff the subject represents the same Message
     */
    static MessageIdentityChecker is(MessageIdentifier subject) {
        return new MessageIdentityChecker(subject);
    }

    class MessageIdentityChecker {
        private final MessageIdentifier subject;

        private MessageIdentityChecker(MessageIdentifier subject) {
            this.subject = subject;
        }

        public boolean equalTo(MessageIdentifier other) {
            Introspector subjectIntrospector = this.subject.forIntrospection();
            Introspector otherIntrospector = other.forIntrospection();
            switch (otherIntrospector.getTypeName()) {
                case "DatabaseId": // Intentional fall-through
                case "Actual": {
                    if ("DeviceIdentifierAndProtocolInfoParts".equals(subjectIntrospector.getTypeName())) {
                        // Best option is to compare the device identifier
                        DeviceIdentifier subjectDeviceIdentifier = this.subject.getDeviceIdentifier();
                        DeviceIdentifier otherDeviceIdentifier = other.getDeviceIdentifier();
                        return DeviceIdentifier.is(subjectDeviceIdentifier).equalTo(otherDeviceIdentifier);
                    } else {
                        Number otherDatabaseValue = (Number) otherIntrospector.getValue("databaseValue");
                        Number subjectDatabaseValue = (Number) subjectIntrospector.getValue("databaseValue");
                        return subjectDatabaseValue.equals(otherDatabaseValue);
                    }
                }
                case "DeviceIdentifierAndProtocolInfoParts": {
                    if ("DeviceIdentifierAndProtocolInfoParts".equals(subjectIntrospector.getTypeName())) {
                        DeviceIdentifier subjectDeviceIdentifier = this.subject.getDeviceIdentifier();
                        String[] subjectProtocolInfo = (String[]) subjectIntrospector.getValue("protocolInfo");
                        DeviceIdentifier otherDeviceIdentifier = other.getDeviceIdentifier();
                        String[] otherProtocolInfo = (String[]) otherIntrospector.getValue("protocolInfo");
                        return DeviceIdentifier.is(subjectDeviceIdentifier).equalTo(otherDeviceIdentifier)
                            && Arrays.equals(subjectProtocolInfo, otherProtocolInfo);
                    } else {
                        // Best option is to compare the device identifier
                        DeviceIdentifier subjectDeviceIdentifier = this.subject.getDeviceIdentifier();
                        DeviceIdentifier otherDeviceIdentifier = other.getDeviceIdentifier();
                        return DeviceIdentifier.is(subjectDeviceIdentifier).equalTo(otherDeviceIdentifier);
                    }
                }
                default: {
                    return this.subject.equals(other);
                }
            }
        }

    }

}