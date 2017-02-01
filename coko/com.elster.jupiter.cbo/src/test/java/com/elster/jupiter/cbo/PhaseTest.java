/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bbl on 27/01/2016.
 */
public class PhaseTest {

    @Test
    public void testAllEnumsDefined() {
        assertThat(Phase.values()).hasSize(32);
    }

    @Test
    public void testUniqueDescription() {
        assertThat(Arrays.stream(Phase.values()).map(Phase::getDescription).collect(Collectors.toSet())).hasSize(Phase.values().length);
    }

    @Test
    public void testCheckIds() {
        assertThat(Phase.PHASENETWORKED.getId()).isEqualTo(17153);
        assertThat(Phase.PHASEFOURWIREOPENDELTA.getId()).isEqualTo(10465);
    }
}
