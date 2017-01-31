/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.object;

import com.energyict.mdc.common.Unit;

import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class GasAnalysisCategory extends AbstractUnsignedBINObject<GasAnalysisCategory> {

    private static final int[] DEFAULT_LENGTHS = gi(3);

    private static final Map<String, String> SYMBOLS = new HashMap<String, String>();

    static {
        SYMBOLS.put("B.0.0", "ALL_an");
        SYMBOLS.put("B.0.1", "ALL_an_15");
        SYMBOLS.put("B.0.2", "ALL_an_h");
        SYMBOLS.put("B.0.3", "ALL_an_g");
        SYMBOLS.put("B.0.4", "ALL_an_m");
        SYMBOLS.put("B.0.5", "ALL_an_y");

        SYMBOLS.put("B.1.0", "PCS_an");
        SYMBOLS.put("B.1.1", "PCS_an_15");
        SYMBOLS.put("B.1.2", "PCS_an_h");
        SYMBOLS.put("B.1.3", "PCS_an_g");
        SYMBOLS.put("B.1.4", "PCS_an_m");
        SYMBOLS.put("B.1.5", "PCS_an_y");

        SYMBOLS.put("B.2.0", "PCI_an");
        SYMBOLS.put("B.2.1", "PCI_an_15");
        SYMBOLS.put("B.2.2", "PCI_an_h");
        SYMBOLS.put("B.2.3", "PCI_an_g");
        SYMBOLS.put("B.2.4", "PCI_an_m");
        SYMBOLS.put("B.2.5", "PCI_an_y");

        SYMBOLS.put("B.2.6", "PCI_i");

    }

    private static final Map<String, Unit> UNITS = new HashMap<String, Unit>();

    static {
        UNITS.put("B.0.0", Unit.get("mol%"));
        UNITS.put("B.0.1", Unit.get("mol%"));
        UNITS.put("B.0.2", Unit.get("mol%"));
        UNITS.put("B.0.3", Unit.get("mol%"));
        UNITS.put("B.0.4", Unit.get("mol%"));
        UNITS.put("B.0.5", Unit.get("mol%"));

        UNITS.put("B.1.0", Unit.get("MJ/Nm3"));
        UNITS.put("B.1.1", Unit.get("MJ/Nm3"));
        UNITS.put("B.1.2", Unit.get("MJ/Nm3"));
        UNITS.put("B.1.3", Unit.get("MJ/Nm3"));
        UNITS.put("B.1.4", Unit.get("MJ/Nm3"));
        UNITS.put("B.1.5", Unit.get("MJ/Nm3"));

        UNITS.put("B.2.0", Unit.get("MJ/Nm3"));
        UNITS.put("B.2.1", Unit.get("MJ/Nm3"));
        UNITS.put("B.2.2", Unit.get("MJ/Nm3"));
        UNITS.put("B.2.3", Unit.get("MJ/Nm3"));
        UNITS.put("B.2.4", Unit.get("MJ/Nm3"));
        UNITS.put("B.2.5", Unit.get("MJ/Nm3"));

        UNITS.put("B.2.6", Unit.get("MJ/Nm3"));

    }

    private static final Map<String, int[]> VALUE_LENGTHS = new HashMap<String, int[]>();

    static {
        VALUE_LENGTHS.put("B.0.0", gi(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3));
        VALUE_LENGTHS.put("B.0.1", gi(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3));
        VALUE_LENGTHS.put("B.0.2", gi(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3));
        VALUE_LENGTHS.put("B.0.3", gi(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3));
        VALUE_LENGTHS.put("B.0.4", gi(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3));
        VALUE_LENGTHS.put("B.0.5", gi(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3));
    }


    public GasAnalysisCategory(CTRObjectID id) {
        this.setId(id);
    }

    @Override
    public Unit getUnit(CTRObjectID id, int valueNumber) {
        Unit unit = UNITS.get(id.toString());
        return unit == null ? DEFAULT_UNIT : unit;
    }

    @Override
    protected String getSymbol(CTRObjectID id) {
        String symbol = SYMBOLS.get(id.toString());
        return symbol == null ? DEFAULT_SYMBOL : symbol;
    }

    @Override
    public int[] getValueLengths(CTRObjectID id) {
        int[] lengths = VALUE_LENGTHS.get(id.toString());
        return lengths == null ? DEFAULT_LENGTHS : lengths;
    }

    @Override
    public BigDecimal getOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        return DEFAULT_OVERFLOW;
    }
}
