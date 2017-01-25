package com.energyict.mdc.device.topology;

import java.util.stream.Stream;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests the {@link PhaseInfo} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-15 (13:16)
 */
public class PhaseInfoTest {

    @Test
    public void allEnumValuesHaveDifferentId() {
        long numberOfUniqueIds = Stream.of(PhaseInfo.values()).mapToInt(PhaseInfo::getId).distinct().count();
        assertThat(numberOfUniqueIds).isEqualTo(PhaseInfo.values().length);
    }

    @Test
    public void byIdReturnsSameEnumValue() {
        for (PhaseInfo modulationScheme : PhaseInfo.values()) {
            if (!PhaseInfo.fromId(modulationScheme.getId()).equals(modulationScheme)) {
                fail("PhaseInfo#fromId(" + modulationScheme.name() +") does not return the same enum value");
            }
        }

    }

    @Test
    public void byIdForUnknownIdReturnsUnknown() {
        // Business method
        PhaseInfo phaseInfo = PhaseInfo.fromId(Integer.MAX_VALUE);

        // Asserts
        assertThat(phaseInfo).isEqualTo(PhaseInfo.UNKNOWN);
    }

}