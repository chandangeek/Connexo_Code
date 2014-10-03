package com.elster.jupiter.cbo;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class QualityCodeIndexTest {

    @Test
    public void testIndexUnique() {
    	Map<QualityCodeCategory,Set<Integer>> map = new HashMap<>();
    	Arrays.stream(QualityCodeCategory.values()).forEach(category -> map.put(category, new HashSet<>()));
    	for (QualityCodeIndex codeIndex : QualityCodeIndex.values()) {
    		Set<Integer> indexes = map.get(codeIndex.category());
    		if (indexes.contains(codeIndex.index())) {
    			throw new RuntimeException("Duplicate index: " + codeIndex); 
    		}
    		indexes.add(codeIndex.index());
    	}
    }

}
