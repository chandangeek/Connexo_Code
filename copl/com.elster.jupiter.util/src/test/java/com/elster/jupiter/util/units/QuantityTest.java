package com.elster.jupiter.util.units;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class QuantityTest {

    @Test
    public void testName() {
    	for (Unit unit : Unit.values()) {
    		Quantity quantity = unit.amount(BigDecimal.ONE);
    		quantity.asSi();
    	}
    }

}
