package com.elster.jupiter.cbo;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QualityCodeSystemTest {

    @Test
    public void testQualityCodeSystemOfStringRepresentation() {
        assertThat(QualityCodeSystem.of("MDC")).isEqualTo(QualityCodeSystem.MDC);
        assertThat(QualityCodeSystem.of("ENDDEVICE")).isEqualTo(QualityCodeSystem.ENDDEVICE);
        assertThat(QualityCodeSystem.of(null)).isEqualTo(QualityCodeSystem.NOTAPPLICABLE);
        assertThat(QualityCodeSystem.of("")).isEqualTo(QualityCodeSystem.NOTAPPLICABLE);
        assertThat(QualityCodeSystem.of("BOOTLEG")).isEqualTo(QualityCodeSystem.OTHER);
    }

    @Test
    public void testQualityCodeSystemOfApplication() {
        assertThat(QualityCodeSystem.ofApplication("MDC")).isEqualTo(QualityCodeSystem.MDC);
        assertThat(QualityCodeSystem.ofApplication("INS")).isEqualTo(QualityCodeSystem.MDM);
        assertThat(QualityCodeSystem.ofApplication(null)).isEqualTo(QualityCodeSystem.NOTAPPLICABLE);
        assertThat(QualityCodeSystem.ofApplication("")).isEqualTo(QualityCodeSystem.NOTAPPLICABLE);
        assertThat(QualityCodeSystem.ofApplication("BOOTLEG")).isEqualTo(QualityCodeSystem.OTHER);
    }
}
