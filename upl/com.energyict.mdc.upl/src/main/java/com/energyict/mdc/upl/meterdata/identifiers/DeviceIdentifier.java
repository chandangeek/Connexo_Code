package com.energyict.mdc.upl.meterdata.identifiers;

/**
 * Identifies a device that started inbound communication.
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
 * <tr><td>PhoneNumber</td><td>phoneNumber -&gt; the device's phoneNumber property value<br>connectionTypeClass -&gt; the connection type class that defines the phoneNumber property<br>propertyName -&gt; the name of the connection type's property that holds the phone number value</td></tr>
 * <tr><td>mRID</td><td>databaseValue -&gt; the device's mRID property</td></tr>
 * <tr><td>Actual</td><td>databaseValue -&gt; the device's database identifier<br>actual -&gt; the device</td></tr>
 * </table>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-12 (10:56)
 */
public interface DeviceIdentifier extends Identifier {

    boolean equals(Object other);

    /**
     * Start of fluent API to check if the specified DeviceIdentifier
     * represents the same Device as another DeviceIdentifier.
     * We avoid implementing this on the DeviceIdentifier implementation
     * classes because returning <code>true</code> in equals method
     * requires that both objects return the same hash code
     * but different implementations may use completely different
     * device properties to calculate the hash.
     *
     * @param subject The subject of the equals check
     * @return <code>true</code> iff the subject represents the same Device
     */
    static DeviceIdentityChecker is(DeviceIdentifier subject) {
        return new DeviceIdentityChecker(subject);
    }

    class DeviceIdentityChecker {
        private final DeviceIdentifier subject;

        private DeviceIdentityChecker(DeviceIdentifier subject) {
            this.subject = subject;
        }

        public boolean equalTo(DeviceIdentifier other) {
            Introspector subjectIntrospector = this.subject.forIntrospection();
            Introspector otherIntrospector = other.forIntrospection();
            switch (otherIntrospector.getTypeName()) {
                case "mRID": {
                }
                case "Actual": {
                }
                case "DatabaseId": {
                }
                case "CallHomeId": {
                }
                case "PhoneNumber": {
                }
                case "SerialNumber": {
                }
                case "LikeSerialNumber": {
                }
                default: {
                    return this.subject.equals(other);
                }
            }
        }

    }

}