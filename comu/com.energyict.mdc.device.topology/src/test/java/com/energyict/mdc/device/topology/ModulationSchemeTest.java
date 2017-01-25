package com.energyict.mdc.device.topology;

import java.util.stream.Stream;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tests the {@link ModulationScheme} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-15 (13:16)
 */
public class ModulationSchemeTest {

    @Test
    public void allEnumValuesHaveDifferentId() {
        long numberOfUniqueIds = Stream.of(ModulationScheme.values()).mapToInt(ModulationScheme::getId).distinct().count();
        assertThat(numberOfUniqueIds).isEqualTo(ModulationScheme.values().length);
    }

    @Test
    public void byIdReturnsSameEnumValue() {
        for (ModulationScheme modulationScheme : ModulationScheme.values()) {
            if (!ModulationScheme.fromId(modulationScheme.getId()).equals(modulationScheme)) {
                fail("ModulationScheme#fromId(" + modulationScheme.name() +") does not return the same enum value");
            }
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void byIdForUnknownIdThrowIllegalArgumentException() {
        // Business method
        ModulationScheme.fromId(Integer.MAX_VALUE);
        // Asserts: see expected error
    }

}