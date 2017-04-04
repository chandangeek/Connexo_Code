package com.energyict.mdc.upl.meterdata.identifiers;

import com.energyict.mdc.upl.meterdata.LoadProfile;

import com.energyict.obis.ObisCode;

/**
 * Provides functionality to identify a specific LoadProfile of a Device.
 * <br>
 * As mentioned in {@link Identifier}, the introspection mechanism
 * was designed with compatibility in mind.
 * Below is a list of currently known type names:
 * <table>
 * <tr><th>type name</th><th>comprising parts and their roles</th></tr>
 * <tr><td>DatabaseId</td><td>databaseValue -&gt; the load profile's database identifier</td></tr>
 * <tr><td>DeviceIdentifierAndObisCode</td><td>device -&gt; the {@link DeviceIdentifier device's identifier}<br>obisCode -&gt; the ObisCode</td></tr>
 * <tr><td>FirstLoadProfileOnDevice</td><td>device -&gt; the {@link DeviceIdentifier device's identifier}</td></tr>
 * <tr><td>Actual</td><td>databaseValue -&gt; the load profile's database identifier<br>actual -&gt; the load profile</td></tr>
 * </table>
 * <p/>
 *
 * Date: 15/10/12
 * Time: 14:01
 */
public interface LoadProfileIdentifier extends Identifier {

    ObisCode getProfileObisCode();

    DeviceIdentifier getDeviceIdentifier();

    /**
     * Start of fluent API to check if the specified LoadProfileIdentifier
     * represents the same LoadProfile as another LoadProfileIdentifier.
     * We avoid implementing this on the LoadProfileIdentifier implementation
     * classes because returning <code>true</code> in equals method
     * requires that both objects return the same hash code
     * but different implementations may use completely different
     * load profile properties to calculate the hash.
     *
     * @param subject The subject of the equals check
     * @return <code>true</code> iff the subject represents the same LoadProfile
     */
    static LoadProfileIdentityChecker is(LoadProfileIdentifier subject) {
        return new LoadProfileIdentityChecker(subject);
    }

    class LoadProfileIdentityChecker {
        private final LoadProfileIdentifier subject;

        private LoadProfileIdentityChecker(LoadProfileIdentifier subject) {
            this.subject = subject;
        }

        boolean equalTo(LoadProfileIdentifier other) {
            Introspector subjectIntrospector = this.subject.forIntrospection();
            Introspector otherIntrospector = other.forIntrospection();
            switch (otherIntrospector.getTypeName()) {
                case "DatabaseId": // Intentional fall-through
                case "DeviceIdentifierAndObisCode": {
                    if ("Actual".equals(subjectIntrospector.getTypeName())) {
                        LoadProfile subjectLoadProfile = (LoadProfile) subjectIntrospector.getValue("actual");
                        DeviceIdentifier deviceIdentifier = (DeviceIdentifier) otherIntrospector.getValue("device");
                        ObisCode obisCode = (ObisCode) otherIntrospector.getValue("obisCode");
                        return DeviceIdentifier.is(deviceIdentifier).equalTo(subjectLoadProfile.getDeviceIdentifier())
                            && obisCode.equals(subjectLoadProfile.getObisCode());
                    } else {
                        return subjectIntrospector.roleEqualsTo(otherIntrospector, "device", "obisCode");
                    }
                }
                case "FirstLoadProfileOnDevice": {
                    if ("Actual".equals(subjectIntrospector.getTypeName())) {
                        LoadProfile otherLoadProfile = (LoadProfile) otherIntrospector.getValue("actual");
                        return DeviceIdentifier.is(otherLoadProfile.getDeviceIdentifier()).equalTo((DeviceIdentifier) subjectIntrospector.getValue("device"));
                    } else {
                        return subjectIntrospector.roleEqualsTo(otherIntrospector, "device");
                    }
                }
                case "Actual": {
                    LoadProfile otherLoadProfile = (LoadProfile) otherIntrospector.getValue("actual");
                    if ("Actual".equals(subjectIntrospector.getTypeName())) {
                        LoadProfile subjectLoadProfile = (LoadProfile) subjectIntrospector.getValue("actual");
                        return subjectLoadProfile.equals(otherLoadProfile);
                    } else {
                        DeviceIdentifier deviceIdentifier = (DeviceIdentifier) subjectIntrospector.getValue("device");
                        ObisCode obisCode = (ObisCode) subjectIntrospector.getValue("obisCode");
                        return DeviceIdentifier.is(deviceIdentifier).equalTo(otherLoadProfile.getDeviceIdentifier())
                            && obisCode.equals(otherLoadProfile.getObisCode());
                    }
                }
                default: {
                    return this.subject.equals(other);
                }
            }
        }

    }

}