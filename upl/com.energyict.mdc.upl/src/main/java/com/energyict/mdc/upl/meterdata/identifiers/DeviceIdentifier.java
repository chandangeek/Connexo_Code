package com.energyict.mdc.upl.meterdata.identifiers;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.meterdata.Device;

import java.util.Optional;
import java.util.Set;

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
 * <tr><td>SystemTitle</td><td>systemTitle -&gt; the device's systemTitle property</td></tr>
 * <tr><td>PropertyBased</td><td>propertyName -&gt; the name of the general property<br>
 * propertyValue -&gt; the value of the general protocol property</td></tr>
 * <tr><td>ConnectionTypePropertyBased</td><td>connectionTypeClass -&gt; the connection type class that defines the property<br>
 * propertyName -&gt; the name of the connection type's property<br>
 * propertyValue -&gt; the value of the connection type's property</td></tr>
 * <tr><td>mRID</td><td>databaseValue -&gt; the device's mRID property</td></tr>
 * <tr><td>Actual</td><td>databaseValue -&gt; the device's database identifier<br>
 * actual -&gt; the device</td></tr>
 * </table>
 * <p>
 * <b>Remark:</b><br> when introducing a new type of DeviceIdentifier, please don't forget to
 * add a new test case to com.energyict.mdc.device.data.impl.DeviceIdentifierResolvingTest
 * <p>
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-12 (10:56)
 */
public interface DeviceIdentifier extends Identifier {

    interface Finder {
        Optional<Device> find(DeviceIdentifier identifier);
    }

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

        /**
         * Checks if this DeviceIdentifier represents the same {@link Device}
         * as the other DeviceIdentifier.<br>
         * <b>Note:</b> be very careful when using this directly in
         * protocol code that is likely to be run on embedded devices
         * such as the beacon because the Services class may not
         * have been initialized in that environment.
         *
         * @param other The other DeviceIdentifier
         * @return <code>true</code> iff this DeviceIdentifier represents the same Device as the other DeviceIdentifier
         */
        public boolean equalTo(DeviceIdentifier other) {
            if (this.subject.forIntrospection().getTypeName().equals(other.forIntrospection().getTypeName())) {
                return sameDevice(this.subject, other, this.subject.forIntrospection().getRoles());
            } else {
                return sameDevice(this.subject, other);
            }
        }

        private boolean sameDevice(DeviceIdentifier id1, DeviceIdentifier id2, Set<String> roles) {
            if (id1.forIntrospection().getTypeName().equals(id2.forIntrospection().getTypeName())) {
                return id1.forIntrospection().roleEqualsTo(id2.forIntrospection(), roles);
            } else {
                return sameDevice(id1, id2);
            }
        }

        private boolean sameDevice(DeviceIdentifier id1, DeviceIdentifier id2) {
            if (Services.deviceFinder() != null) {
                Optional<Device> device1 = Services.deviceFinder().find(id1);
                Optional<Device> device2 = Services.deviceFinder().find(id2);
                return device1.isPresent() && device2.isPresent() && device1.get().equals(device2.get());
            } else {
                // Avoid NullPointerException in beacon context
                return false;
            }
        }
    }
}