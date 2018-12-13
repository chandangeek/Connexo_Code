/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
}
