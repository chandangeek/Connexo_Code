package com.energyict.mdc.upl.meterdata.identifiers;

import com.energyict.mdc.upl.meterdata.Register;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Provides functionality to identify a {@link Register}.
 * Below is a list of currently known type names:
 * <table>
 * <tr><th>type name</th><th>comprising parts and their roles</th></tr>
 * <tr><td>DatabaseId</td><td>databaseValue -&gt; the register's database identifier<br>device -&gt; the {@link DeviceIdentifier register's identifier}<br>obisCode -&gt; the ObisCode</td></tr>
 * <tr><td>DeviceIdentifierAndObisCode</td><td>device -&gt; the {@link DeviceIdentifier register's identifier}<br>obisCode -&gt; the ObisCode</td></tr>
 * <tr><td>PrimeRegisterForChannel</td><td>device -&gt; the {@link DeviceIdentifier register's identifier}<br>channelIndex -&gt; the channel index<br>obisCode -&gt; the ObisCode</td></tr>
 * <tr><td>Actual</td><td>databaseValue -&gt; the register's database identifier<br>register -&gt; the register<br>actual -&gt; the register</td></tr>
 * </table>
 * <p/>
 *
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

        boolean equalTo(RegisterIdentifier other) {
            Introspector subjectIntrospector = this.subject.forIntrospection();
            Introspector otherIntrospector = other.forIntrospection();
            switch (otherIntrospector.getTypeName()) {
                case "DatabaseId": // Intentional fall-through
                case "PrimeRegisterForChannel": // Intentional fall-through
                case "DeviceIdentifierAndObisCode": {
                    DeviceIdentifier otherDeviceIdentifier = other.getDeviceIdentifier();
                    ObisCode otherObisCode = other.getRegisterObisCode();
                    if ("Actual".equals(subjectIntrospector.getTypeName())) {
                        Register subject = (Register) subjectIntrospector.getValue("actual");
                        return DeviceIdentifier.is(subject.getDeviceIdentifier()).equalTo(otherDeviceIdentifier)
                            && subject.getObisCode().equals(otherObisCode);
                    } else {
                        DeviceIdentifier subjectIdentifier = this.subject.getDeviceIdentifier();
                        ObisCode subjectObisCode = this.subject.getRegisterObisCode();
                        return DeviceIdentifier.is(subjectIdentifier).equalTo(otherDeviceIdentifier)
                            && subjectObisCode.equals(otherObisCode);
                    }
                }
                case "Actual": {
                    Register otherRegister = (Register) otherIntrospector.getValue("actual");
                    if ("Actual".equals(subjectIntrospector.getTypeName())) {
                        Register subject = (Register) subjectIntrospector.getValue("actual");
                        return subject.equals(otherRegister);
                    } else {
                        DeviceIdentifier otherDeviceIdentifier = otherRegister.getDeviceIdentifier();
                        ObisCode otherObisCode = otherRegister.getObisCode();
                        DeviceIdentifier subjectIdentifier = this.subject.getDeviceIdentifier();
                        ObisCode subjectObisCode = this.subject.getRegisterObisCode();
                        return DeviceIdentifier.is(subjectIdentifier).equalTo(otherDeviceIdentifier)
                            && subjectObisCode.equals(otherObisCode);
                    }
                }
                default: {
                    return this.subject.equals(other);
                }
            }
        }

    }

}