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
    		if (!set.add(dimension.getName())) {
    			System.out.println("Duplicate name: " + dimension.getName());
    		}
    	}
    	assertThat(set).hasSize(Dimension.values().length);
    }

    @Test
    public void testDuplicates() {
    	List<Dimension> dimensions = new ArrayList<>();
    	for (Dimension dimension : Dimension.values()) {
    		boolean match = false;
    		for (Dimension each : dimensions) {
    			if (dimension.hasSameDimensions(each)) {
    				System.out.println(dimension.getName() + " has same dimensions as " + each.getName());
    				match = true;
    			}
    		}
    		if (!match) {
    			dimensions.add(dimension);
    		}
    	}
    }
}
