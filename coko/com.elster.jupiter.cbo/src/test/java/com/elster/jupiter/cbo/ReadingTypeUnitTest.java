package com.elster.jupiter.cbo;

import org.junit.Test;

import com.elster.jupiter.util.units.Unit;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ReadingTypeUnitTest {

    @Test
    public void testCimCode() {
        for (ReadingTypeUnit each : ReadingTypeUnit.values()) {
            assertThat(each == ReadingTypeUnit.get(each.getId()));
        }
    }
    
    @Test
    public void testUnits() {
    	 Set<Unit> units = new HashSet<>();
    	 for (ReadingTypeUnit each : ReadingTypeUnit.values()) {
    		 units.add(each.getUnit());
    	 }
         assertThat(ReadingTypeUnit.values().length == units.size());
    }
    
    @Test
    public void testSymbols() {
    	 Set<String> symbols = new HashSet<>();
    	 for (ReadingTypeUnit each : ReadingTypeUnit.values()) {
    		 symbols.add(each.getSymbol());
    	 }
         assertThat(ReadingTypeUnit.values().length == symbols.size());
    }

}
