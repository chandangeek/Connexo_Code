package com.energyict.mdc.upl.meterdata.identifiers;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.DeviceMessage;

import java.util.Optional;
import java.util.Set;

/**
 * Provides functionality to uniquely identify a {@link DeviceMessage}.
 * Below is a list of currently known type names:
 * <table>
 * <tr><th>type name</th><th>comprising parts and their roles</th></tr>
 * <tr><td>DatabaseId</td><td>databaseValue -&gt; the message's database identifier<br>device -&gt; the {@link DeviceIdentifier device's identifier}</td></tr>
 * <tr><td>DeviceIdentifierAndProtocolInfoParts</td><td>device -&gt; the {@link DeviceIdentifier device's identifier}<br>protocolInfo -&gt; protocol specific information in the form of String[]</td></tr>
 * <tr><td>Actual</td><td>databaseValue -&gt; the message's database identifier<br>actual -&gt; the device message<br>device -&gt; the {@link DeviceIdentifier device's identifier}</td></tr>
 * </table>
 * <b>Remark:</b><br>
 * when introducing a new type of MessageIdentifier, please don't forget to
 * add a new test case to com.energyict.mdc.device.data.impl.MessageIdentifierResolvingTest
 * <p>
 * Date: 22/03/13
 * Time: 8:59
 */
public interface MessageIdentifier extends Identifier {

    DeviceIdentifier getDeviceIdentifier();

    interface Finder {
        Optional<DeviceMessage> find(MessageIdentifier identifier);
    }

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
            if (this.subject.forIntrospection().getTypeName().equals(other.forIntrospection().getTypeName())) {
                return sameMessage(this.subject, other, this.subject.forIntrospection().getRoles());
            } else {
                return sameMessage(this.subject, other);
            }
        }

        private boolean sameMessage(MessageIdentifier id1, MessageIdentifier id2, Set<String> roles) {
            if (id1.forIntrospection().getTypeName().equals(id2.forIntrospection().getTypeName())) {
                return id1.forIntrospection().roleEqualsTo(id2.forIntrospection(), roles);
            } else {
                return sameMessage(id1, id2);
            }
        }

        private boolean sameMessage(MessageIdentifier id1, MessageIdentifier id2) {
            if (Services.deviceMessageFinder() != null) {
                Optional<DeviceMessage> m1 = Services.deviceMessageFinder().find(id1);
                Optional<DeviceMessage> m2 = Services.deviceMessageFinder().find(id2);
                return m1.isPresent() && m2.isPresent() && m1.get().equals(m2.get());
            } else {
                // Avoid NullPointerException in beacon context
                return false;
            }
        }
    }
}