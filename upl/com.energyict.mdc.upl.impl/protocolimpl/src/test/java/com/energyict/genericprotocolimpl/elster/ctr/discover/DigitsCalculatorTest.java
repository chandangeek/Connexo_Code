package com.energyict.genericprotocolimpl.elster.ctr.discover;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import org.junit.Test;

import java.math.BigDecimal;

import static com.energyict.genericprotocolimpl.elster.ctr.discover.DigitsCalculator.getByRuleOfThumb;
import static org.junit.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 21/02/11
 * Time: 10:59
 */
public class DigitsCalculatorTest {

    private static final Unit UNIT = Unit.getUndefined();

    @Test
    public void testGetByRuleOfThumb() throws Exception {
        // Calculated values
        assertEquals(9, getByRuleOfThumb(qty("000.01")));
        assertEquals(9, getByRuleOfThumb(qty("000.09")));
        assertEquals(8, getByRuleOfThumb(qty("000.10")));
        assertEquals(8, getByRuleOfThumb(qty("000.99")));
        assertEquals(7, getByRuleOfThumb(qty("001.00")));
        assertEquals(7, getByRuleOfThumb(qty("009.99")));
        assertEquals(6, getByRuleOfThumb(qty("010.00")));
        assertEquals(6, getByRuleOfThumb(qty("099.99")));
        assertEquals(5, getByRuleOfThumb(qty("100.00")));
        assertEquals(5, getByRuleOfThumb(qty("999.99")));

        // Default values
        assertEquals(8, getByRuleOfThumb(null)); // Quantity == null
        assertEquals(8, getByRuleOfThumb(new Quantity((BigDecimal) null, UNIT))); // Quantity.amount == null
        assertEquals(8, getByRuleOfThumb(qty("0")));
        assertEquals(8, getByRuleOfThumb(qty("0.001")));
        assertEquals(8, getByRuleOfThumb(qty("-1")));
        assertEquals(8, getByRuleOfThumb(qty("1000")));
    }

    private Quantity qty(String amount) {
        return new Quantity(new BigDecimal(amount), UNIT);
    }

}
