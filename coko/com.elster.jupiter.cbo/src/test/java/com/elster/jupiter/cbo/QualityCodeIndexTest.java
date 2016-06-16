package com.elster.jupiter.cbo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QualityCodeIndexTest {

    @Test
    public void testIndexUnique() {
        Map<QualityCodeCategory, Set<Integer>> map = new HashMap<>();
        Arrays.stream(QualityCodeCategory.values()).forEach(category -> map.put(category, new HashSet<>()));
        for (QualityCodeIndex codeIndex : QualityCodeIndex.values()) {
            Set<Integer> indexes = map.get(codeIndex.category());
            if (indexes.contains(codeIndex.index())) {
                throw new RuntimeException("Duplicate index: " + codeIndex);
            }
            indexes.add(codeIndex.index());
        }
    }

    @Test
    public void testPrescribedQualityCodeIndexIsFound() {
        assertThat(QualityCodeIndex.get(QualityCodeCategory.VALIDATION, 4).get()).isEqualTo(QualityCodeIndex.USAGEBELOW);
        assertThat(QualityCodeIndex.get(QualityCodeCategory.EDITED, 3).get()).isEqualTo(QualityCodeIndex.REJECTED);
    }

    @Test
    public void testCustomValidation() {
        assertThat(QualityCodeIndex.get(QualityCodeCategory.VALIDATION, 1001).isPresent()).isFalse();
    }
}
