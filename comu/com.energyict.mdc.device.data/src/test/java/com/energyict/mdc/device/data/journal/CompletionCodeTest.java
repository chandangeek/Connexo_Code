package com.energyict.mdc.device.data.journal;

import com.energyict.mdc.common.ApplicationException;
import org.junit.*;

import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link CompletionCode} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-01-15 (15:50)
 */
public class CompletionCodeTest {

    @Test
    public void testValueFromDb () {
        for (CompletionCode completionCode : CompletionCode.values()) {
            assertThat(CompletionCode.valueFromDb(completionCode.dbValue())).
                    as("Conversion from an to db for CompletionCode " + completionCode + " fails").
                    isEqualTo(completionCode);
        }
    }

    @Test(expected = ApplicationException.class)
    public void testUnknownValueFromDb () {
        CompletionCode.valueFromDb(CompletionCode.values().length * 10);
    }

    @Test
    public void testAlwaysUpgradeFromLowestPriority () {
        Set<CompletionCode> allButLowestPriority = EnumSet.complementOf(EnumSet.of(CompletionCode.Ok));
        for (CompletionCode notLowestPriority : allButLowestPriority) {
            assertThat(CompletionCode.Ok.upgradeTo(notLowestPriority)).isEqualTo(notLowestPriority);
        }
    }

    @Test
    public void testNeverUpgradeFromHighestPriority () {
        Set<CompletionCode> allButHighestPriority = EnumSet.complementOf(EnumSet.of(CompletionCode.ConnectionError));
        for (CompletionCode notHighestPriority : allButHighestPriority) {
            assertThat(CompletionCode.ConnectionError.upgradeTo(notHighestPriority)).isEqualTo(CompletionCode.ConnectionError);
        }
    }

    @Test
    public void testUpgradeToSame () {
        for (CompletionCode completionCode : CompletionCode.values()) {
            assertThat(completionCode.upgradeTo(completionCode)).
                    as("Upgrade to the same CompletionCode " + completionCode + " fails").
                    isEqualTo(completionCode);
        }
    }

}