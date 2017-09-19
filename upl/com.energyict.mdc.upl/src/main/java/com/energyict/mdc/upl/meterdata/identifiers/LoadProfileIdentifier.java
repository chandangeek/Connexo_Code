package com.energyict.mdc.upl.meterdata.identifiers;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.meterdata.LoadProfile;

import com.energyict.obis.ObisCode;

import java.util.Optional;
import java.util.Set;

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
 * <tr><td>FirstLoadProfileOnDevice</td><td>device -&gt; the {@link DeviceIdentifier device's identifier}<br>obisCode -&gt; the ObisCode</td></tr>
 * <tr><td>Actual</td><td>databaseValue -&gt; the load profile's database identifier<br>actual -&gt; the load profile</td></tr>
 * </table>
 * <b>Remark:</b><br>
 * when introducing a new type of LoadProfileIdentifier, please don't forget to
 * add a new test case to com.energyict.mdc.device.data.impl.LoadProfileIdentifierResolvingTest
 * <p>
 * Date: 15/10/12
 * Time: 14:01
 */
public interface LoadProfileIdentifier extends Identifier {

    ObisCode getProfileObisCode();

    DeviceIdentifier getDeviceIdentifier();

    interface Finder {
        Optional<LoadProfile> find(LoadProfileIdentifier identifier);
    }

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

        public boolean equalTo(LoadProfileIdentifier other) {
            if (this.subject.forIntrospection().getTypeName().equals(other.forIntrospection().getTypeName())) {
                return sameLoadProfile(this.subject, other, this.subject.forIntrospection().getRoles());
            } else {
                return sameLoadProfile(this.subject, other);
            }
        }

        private boolean sameLoadProfile(LoadProfileIdentifier id1, LoadProfileIdentifier id2, Set<String> roles) {
            if (id1.forIntrospection().getTypeName().equals(id2.forIntrospection().getTypeName())) {
                return id1.forIntrospection().roleEqualsTo(id2.forIntrospection(), roles);
            } else {
                return sameLoadProfile(id1, id2);
            }
        }

        private boolean sameLoadProfile(LoadProfileIdentifier id1, LoadProfileIdentifier id2) {
            if (Services.loadProfileFinder() != null) {
                Optional<LoadProfile> lp1 = Services.loadProfileFinder().find(id1);
                Optional<LoadProfile> lp2 = Services.loadProfileFinder().find(id2);
                return lp1.isPresent() && lp2.isPresent() && lp1.get().equals(lp2.get());
            } else {
                // Avoid NullPointerException in beacon context
                return false;
            }
        }
    }
}