package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.HardCodedFieldNames;
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
 * Tests the {@link IntervalExtractor} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-12 (09:46)
 */
@RunWith(MockitoJUnitRunner.class)
public class IntervalExtractorTest {

    @Mock
    private CustomPropertySet<TestDomain, DomainExtensionForTestingPurposes> customPropertySet;
    @Mock
    private CustomPropertySet<TestDomain, VersionedDomainExtensionForTestingPurposes> versionedCustomPropertySet;

    @Test(expected = MappingException.class)
    public void extractionFailsWhenIntervalFieldIsMissing() {
        when(this.customPropertySet.isVersioned()).thenReturn(true);

        DomainExtensionForTestingPurposes extension = new DomainExtensionForTestingPurposes(new TestDomain(1L), this.customPropertySet);
        CustomPropertySetValues properties = CustomPropertySetValues.empty();

        // Business method
        IntervalExtractor.from(extension).into(properties);

        // Asserts: see expected exception rule
    }

    @Test
    public void extractFromVersionedExtension() {
        when(this.versionedCustomPropertySet.isVersioned()).thenReturn(true);

        Instant start = Instant.ofEpochSecond(1000L);
        Instant end = Instant.ofEpochSecond(1000000L);
        Interval expectedInterval = Interval.of(Ranges.closedOpen(start, end));
        VersionedDomainExtensionForTestingPurposes extension = new VersionedDomainExtensionForTestingPurposes(new TestDomain(1L), this.versionedCustomPropertySet, expectedInterval);
        CustomPropertySetValues properties = CustomPropertySetValues.empty();

        // Business method
        IntervalExtractor.from(extension).into(properties);

        // Asserts
        Object extractedInterval = properties.getProperty(HardCodedFieldNames.INTERVAL.javaName());
        assertThat(extractedInterval).isEqualTo(expectedInterval);
    }

}