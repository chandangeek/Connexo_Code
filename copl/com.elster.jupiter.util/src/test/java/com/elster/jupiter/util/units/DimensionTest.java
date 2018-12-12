/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.units;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DimensionTest {

    @Test
    public void testName() {
    	Set<String> set = new HashSet<>();
    	for (Dimension dimension : Dimension.values()) {
    		set.add(dimension.getName());
    	}
    	assertThat(set).hasSize(Dimension.values().length);
    }

}
