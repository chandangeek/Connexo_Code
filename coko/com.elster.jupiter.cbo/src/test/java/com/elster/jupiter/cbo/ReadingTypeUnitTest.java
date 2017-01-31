/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadingTypeUnitTest {

    @Test
    public void testCimCode() {
        for (ReadingTypeUnit each : ReadingTypeUnit.values()) {
            assertThat(each).isEqualTo(ReadingTypeUnit.get(each.getId()));
        }
    }
    
    @Test
    public void testCimCodeUnique() {
    	Set<Integer> codes = new HashSet<>();
    	for (ReadingTypeUnit each : ReadingTypeUnit.values()) {
    		codes.add(each.getId());
        }
    	assertThat(codes).hasSize(ReadingTypeUnit.values().length);
    }

}
