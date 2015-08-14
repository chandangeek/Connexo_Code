package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.time.Interval;

import java.time.Instant;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DomainExtensionAccessor} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-12 (09:46)
 */
@RunWith(MockitoJUnitRunner.class)
public class DomainExtensionAccessorTest {

    @Mock
    private CustomPropertySet<TestDomain, DomainExtensionForTestingPurposes> customPropertySet;
    @Mock
    private CustomPropertySet<TestDomain, VersionedDomainExtensionForTestingPurposes> versionedCustomPropertySet;
    @Mock
    private RegisteredCustomPropertySet registeredCustomPropertySet;

    @Test(expected = MappingException.class)
    public void getIntervalFromBusinessObjectFailsWhenIntervalFieldIsMissing() {
        when(this.customPropertySet.isVersioned()).thenReturn(true);

        DomainExtensionForTestingPurposes extension = new DomainExtensionForTestingPurposes(new TestDomain(1L), this.registeredCustomPropertySet);

        // Business method
        DomainExtensionAccessor.getInterval(extension);

        // Asserts: see expected exception rule
    }

    @Test
    public void getIntervalFromVersionedExtension() {
        when(this.versionedCustomPropertySet.isVersioned()).thenReturn(true);

        Instant start = Instant.ofEpochSecond(1000L);
        Instant end = Instant.ofEpochSecond(1000000L);
        Interval expectedInterval = Interval.of(Ranges.closedOpen(start, end));
        VersionedDomainExtensionForTestingPurposes extension = new VersionedDomainExtensionForTestingPurposes(new TestDomain(1L), this.registeredCustomPropertySet, expectedInterval);

        // Business method
        Interval interval = DomainExtensionAccessor.getInterval(extension);

        // Asserts
        assertThat(interval).isEqualTo(expectedInterval);
    }

    @Test(expected = MappingException.class)
    public void setIntervalIntoBusinessObjectFailsWhenIntervalFieldIsMissing() {
        when(this.customPropertySet.isVersioned()).thenReturn(true);

        DomainExtensionForTestingPurposes extension = new DomainExtensionForTestingPurposes(new TestDomain(1L), this.registeredCustomPropertySet);

        // Business method
        DomainExtensionAccessor.setInterval(extension, Interval.forever());

        // Asserts: see expected exception rule
    }

    @Test
    public void getIntervalIntoVersionedExtension() {
        when(this.versionedCustomPropertySet.isVersioned()).thenReturn(true);

        Instant start = Instant.ofEpochSecond(1000L);
        Instant end = Instant.ofEpochSecond(1000000L);
        Interval expectedInterval = Interval.of(Ranges.closedOpen(start, end));
        VersionedDomainExtensionForTestingPurposes extension = new VersionedDomainExtensionForTestingPurposes(new TestDomain(1L), this.registeredCustomPropertySet, expectedInterval);

        // Business method
        DomainExtensionAccessor.setInterval(extension, expectedInterval);

        // Asserts
        assertThat(extension.getInterval()).isEqualTo(expectedInterval);
    }

    @Test
    public void setRegisteredCustomPropertySetIntoBusinessObjectFailsWhenIntervalFieldIsMissing() {
        DomainExtensionForTestingPurposes extension = new DomainExtensionForTestingPurposes(new TestDomain(1L));

        // Business method
        DomainExtensionAccessor.setRegisteredCustomPropertySet(extension, this.registeredCustomPropertySet);

        // Asserts
        assertThat(extension.getRegisteredCustomPropertySet()).isEqualTo(this.registeredCustomPropertySet);
    }

}