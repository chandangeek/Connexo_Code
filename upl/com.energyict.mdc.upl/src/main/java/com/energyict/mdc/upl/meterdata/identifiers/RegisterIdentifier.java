package com.energyict.mdc.upl.meterdata.identifiers;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.meterdata.Register;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Optional;
import java.util.Set;

/**
 * Provides functionality to identify a {@link Register}.
 * Below is a list of currently known type names:
 * <table>
 * <tr><th>type name</th><th>comprising parts and their roles</th></tr>
 * <tr><td>DatabaseId</td><td>databaseValue -&gt; the register's database identifier<br>device -&gt; the {@link DeviceIdentifier register's device identifier}<br>obisCode -&gt; the ObisCode</td></tr>
 * <tr><td>DeviceIdentifierAndObisCode</td><td>device -&gt; the {@link DeviceIdentifier register's device identifier}<br>obisCode -&gt; the ObisCode</td></tr>
 * <tr><td>PrimeRegisterForChannel</td><td>device -&gt; the {@link DeviceIdentifier register's device identifier}<br>channelIndex -&gt; the channel index<br>obisCode -&gt; the ObisCode</td></tr>
 * <tr><td>Actual</td><td>actual -&gt; the register</td></tr>
 * </table>
 * <b>Remark:</b><br>
 * when introducing a new type of RegisterIdentifier, please don't forget to
 * add a new test case to com.energyict.mdc.device.data.impl.RegisterIdentifierResolvingTest
 * <p>
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

    DeviceIdentifier getDeviceIdentifier();

    interface Finder {
        Optional<Register> find(RegisterIdentifier identifier);
    }

    /**
     * Start of fluent API to check if the specified RegisterIdentifier
     * represents the same Register as another RegisterIdentifier.
     * We avoid implementing this on the RegisterIdentifier implementation
     * classes because returning <code>true</code> in equals method
     * requires that both objects return the same hash code
     * but different implementations may use completely different
     * register properties to calculate the hash.
     *
     * @param subject The subject of the equals check
     * @return <code>true</code> iff the subject represents the same Register
     */
    static RegisterIdentityChecker is(RegisterIdentifier subject) {
        return new RegisterIdentityChecker(subject);
    }

    class RegisterIdentityChecker {
        private final RegisterIdentifier subject;

        private RegisterIdentityChecker(RegisterIdentifier subject) {
            this.subject = subject;
        }

        public boolean equalTo(RegisterIdentifier other) {
            if (this.subject.forIntrospection().getTypeName().equals(other.forIntrospection().getTypeName())) {
                return sameRegister(this.subject, other, this.subject.forIntrospection().getRoles());
            } else {
                return sameRegister(this.subject, other);
            }
        }

        private boolean sameRegister(RegisterIdentifier id1, RegisterIdentifier id2, Set<String> roles) {
            if (id1.forIntrospection().getTypeName().equals(id2.forIntrospection().getTypeName())) {
                return id1.forIntrospection().roleEqualsTo(id2.forIntrospection(), roles);
            } else {
                return sameRegister(id1, id2);
            }
        }

        private boolean sameRegister(RegisterIdentifier id1, RegisterIdentifier id2) {
            if (Services.registerFinder() != null) {
                Optional<Register> r1 = Services.registerFinder().find(id1);
                Optional<Register> r2 = Services.registerFinder().find(id2);
                return r1.isPresent() && r2.isPresent() && r1.get().equals(r2.get());
            } else {
                // Avoid NullPointerException in beacon context
                return false;
            }
        }
    }
}