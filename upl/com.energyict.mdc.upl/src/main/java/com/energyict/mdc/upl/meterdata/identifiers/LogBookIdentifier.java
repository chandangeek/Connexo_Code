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
 *
 * Date: 16/10/12
 * Time: 8:32
 */
public interface LogBookIdentifier extends Identifier {

    /**
     * Returns the ObisCode of the LogBook referenced by this identifier.
     *
     * @return The ObisCode
     */
    @XmlAttribute
    ObisCode getLogBookObisCode();

    DeviceIdentifier getDeviceIdentifier();

    /**
     * Start of fluent API to check if the specified LogBookIdentifier
     * represents the same LogBook as another LogBookIdentifier.
     * We avoid implementing this on the LogBookIdentifier implementation
     * classes because returning <code>true</code> in equals method
     * requires that both objects return the same hash code
     * but different implementations may use completely different
     * LogBook properties to calculate the hash.
     *
     * @param subject The subject of the equals check
     * @return <code>true</code> iff the subject represents the same LogBook
     */
    static LogBookIdentityChecker is(LogBookIdentifier subject) {
        return new LogBookIdentityChecker(subject);
    }

    class LogBookIdentityChecker {
        private final LogBookIdentifier subject;

        private LogBookIdentityChecker(LogBookIdentifier subject) {
            this.subject = subject;
        }

        public boolean equalTo(LogBookIdentifier other) {
            return DeviceIdentifier.is(this.subject.getDeviceIdentifier()).equalTo(other.getDeviceIdentifier())
                && this.subject.getLogBookObisCode().equals(other.getLogBookObisCode());
        }

    }

}