package com.energyict.mdc.upl.meterdata.identifiers;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.meterdata.LogBook;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Optional;
import java.util.Set;

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
 * <b>Remark:</b><br>
 * when introducing a new type of LogBookIdentifier, please don't forget to
 * add a new test case to com.energyict.mdc.device.data.impl.LogBookIdentifierResolvingTest
 * <p>
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

    interface Finder {
        Optional<LogBook> find(LogBookIdentifier identifier);
    }

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
            if (this.subject.forIntrospection().getTypeName().equals(other.forIntrospection().getTypeName())) {
                return sameLogBook(this.subject, other, this.subject.forIntrospection().getRoles());
            } else {
                return sameLogBook(this.subject, other);
            }
        }

        private boolean sameLogBook(LogBookIdentifier id1, LogBookIdentifier id2, Set<String> roles) {
            if (id1.forIntrospection().getTypeName().equals(id2.forIntrospection().getTypeName())) {
                return id1.forIntrospection().roleEqualsTo(id2.forIntrospection(), roles);
            } else {
                return sameLogBook(id1, id2);
            }
        }

        private boolean sameLogBook(LogBookIdentifier id1, LogBookIdentifier id2) {
            if (Services.logBookFinder() != null) {
                Optional<LogBook> lb1 = Services.logBookFinder().find(id1);
                Optional<LogBook> lb2 = Services.logBookFinder().find(id2);
                return lb1.isPresent() && lb2.isPresent() && lb1.get().equals(lb2.get());
            } else {
                // Avoid NullPointerException in beacon context
                return false;
            }
        }
    }
}