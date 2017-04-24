package com.energyict.protocolimplv2.elster.ctr.MTU155.object;

import com.energyict.cbo.Unit;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 14:29:16
 */
public class VolumeConverterCategory extends AbstractUnsignedBINObject<VolumeConverterCategory> {

    private static final int[] DEFAULT_LENGTH = gi(3);

    private static final Map<String, String> SYMBOLS = new HashMap<String, String>();

    static {
        SYMBOLS.put("A.0.0", "C");
        SYMBOLS.put("A.0.1", "C_15");
        SYMBOLS.put("A.0.2", "C_h");
        SYMBOLS.put("A.0.3", "C_g");
        SYMBOLS.put("A.0.4", "C_m");
        SYMBOLS.put("A.0.5", "C_y");

        SYMBOLS.put("A.1.0", "Z1");
        SYMBOLS.put("A.1.1", "Z1_15");
        SYMBOLS.put("A.1.2", "Z1_h");
        SYMBOLS.put("A.1.3", "Z1_g");
        SYMBOLS.put("A.1.4", "Z1_m");
        SYMBOLS.put("A.1.5", "Z1_y");

        SYMBOLS.put("A.1.6", "Z_i");
        SYMBOLS.put("A.1.7", "Zb");

        SYMBOLS.put("A.2.0", "Z");
        SYMBOLS.put("A.2.1", "Z_15");
        SYMBOLS.put("A.2.2", "Z_h");
        SYMBOLS.put("A.2.3", "Z_g");
        SYMBOLS.put("A.2.4", "Z_m");
        SYMBOLS.put("A.2.5", "Z_y");

        SYMBOLS.put("A.3.0", "density_gas");
        SYMBOLS.put("A.3.1", "density_gas_15");
        SYMBOLS.put("A.3.2", "density_gas_h");
        SYMBOLS.put("A.3.3", "density_gas_g");
        SYMBOLS.put("A.3.4", "density_gas_m");
        SYMBOLS.put("A.3.5", "density_gas_y");

        SYMBOLS.put("A.4.0", "density_air");
        SYMBOLS.put("A.4.1", "density_air_15");
        SYMBOLS.put("A.4.2", "density_air_h");
        SYMBOLS.put("A.4.3", "density_air_g");
        SYMBOLS.put("A.4.4", "density_air_m");
        SYMBOLS.put("A.4.5", "density_air_y");

        SYMBOLS.put("A.5.0", "Dgas");
        SYMBOLS.put("A.5.1", "Dgas_15");
        SYMBOLS.put("A.5.2", "Dgas_h");
        SYMBOLS.put("A.5.3", "Dgas_g");
        SYMBOLS.put("A.5.4", "Dgas_m");
        SYMBOLS.put("A.5.5", "Dgas_y");

        SYMBOLS.put("A.6.0", "N2");
        SYMBOLS.put("A.6.1", "N2_15");
        SYMBOLS.put("A.6.2", "N2_h");
        SYMBOLS.put("A.6.3", "N2_g");
        SYMBOLS.put("A.6.4", "N2_m");
        SYMBOLS.put("A.6.5", "N2_y");

        SYMBOLS.put("A.7.0", "CO2");
        SYMBOLS.put("A.7.1", "CO2_15");
        SYMBOLS.put("A.7.2", "CO2_h");
        SYMBOLS.put("A.7.3", "CO2_g");
        SYMBOLS.put("A.7.4", "CO2_m");
        SYMBOLS.put("A.7.5", "CO2_y");

        SYMBOLS.put("A.8.0", "H2");
        SYMBOLS.put("A.8.1", "H2_15");
        SYMBOLS.put("A.8.2", "H2_h");
        SYMBOLS.put("A.8.3", "H2_g");
        SYMBOLS.put("A.8.4", "H2_m");
        SYMBOLS.put("A.8.5", "H2_y");

        SYMBOLS.put("A.9.0", "CO");
        SYMBOLS.put("A.9.1", "CO_15");
        SYMBOLS.put("A.9.2", "CO_h");
        SYMBOLS.put("A.9.3", "CO_g");
        SYMBOLS.put("A.9.4", "CO_m");
        SYMBOLS.put("A.9.5", "CO_y");

        SYMBOLS.put("A.B.1", "Met_sp_Z");
        SYMBOLS.put("A.B.2", "Met_Z");
        SYMBOLS.put("A.B.3", "Met_sp_V");
        SYMBOLS.put("A.B.4", "Met_V");

    }

    private static final Map<String, Unit> UNITS = new HashMap<String, Unit>();

    static {
        UNITS.put("A.6.0", Unit.get("mol%"));
        UNITS.put("A.6.1", Unit.get("mol%"));
        UNITS.put("A.6.2", Unit.get("mol%"));
        UNITS.put("A.6.3", Unit.get("mol%"));
        UNITS.put("A.6.4", Unit.get("mol%"));
        UNITS.put("A.6.5", Unit.get("mol%"));

        UNITS.put("A.7.0", Unit.get("mol%"));
        UNITS.put("A.7.1", Unit.get("mol%"));
        UNITS.put("A.7.2", Unit.get("mol%"));
        UNITS.put("A.7.3", Unit.get("mol%"));
        UNITS.put("A.7.4", Unit.get("mol%"));
        UNITS.put("A.7.5", Unit.get("mol%"));

        UNITS.put("A.8.0", Unit.get("mol%"));
        UNITS.put("A.8.1", Unit.get("mol%"));
        UNITS.put("A.8.2", Unit.get("mol%"));
        UNITS.put("A.8.3", Unit.get("mol%"));
        UNITS.put("A.8.4", Unit.get("mol%"));
        UNITS.put("A.8.5", Unit.get("mol%"));

        UNITS.put("A.9.0", Unit.get("mol%"));
        UNITS.put("A.9.1", Unit.get("mol%"));
        UNITS.put("A.9.2", Unit.get("mol%"));
        UNITS.put("A.9.3", Unit.get("mol%"));
        UNITS.put("A.9.4", Unit.get("mol%"));
        UNITS.put("A.9.5", Unit.get("mol%"));

        UNITS.put("A.A.1", Unit.get("mm"));
        UNITS.put("A.A.2", Unit.get("mm"));

        UNITS.put("A.A.9", Unit.get("%"));
        UNITS.put("A.A.A", Unit.get("%"));
        UNITS.put("A.C.0", Unit.get("%"));
        UNITS.put("A.C.1", Unit.get("%"));
        UNITS.put("A.C.2", Unit.get("%"));
        UNITS.put("A.C.3", Unit.get("%"));
        UNITS.put("A.C.4", Unit.get("%"));
        UNITS.put("A.C.8", Unit.get("MJ/Nm3"));
        UNITS.put("A.C.A", Unit.get("%"));
        UNITS.put("A.C.B", Unit.get("%"));

    }

    private static final Map<String, int[]> VALUE_LENGTHS = new HashMap<String, int[]>();

    static {
        VALUE_LENGTHS.put("A.A.3", gi(1));

        VALUE_LENGTHS.put("A.A.9", gi(1, 3));
        VALUE_LENGTHS.put("A.A.A", gi(1, 3));

        VALUE_LENGTHS.put("A.B.1", gi(2));

        VALUE_LENGTHS.put("A.B.2", gi(1));
        VALUE_LENGTHS.put("A.B.3", gi(2));
        VALUE_LENGTHS.put("A.B.4", gi(1));

        VALUE_LENGTHS.put("A.C.A", gi(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3));
        VALUE_LENGTHS.put("A.C.B", gi(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3));
    }

    public VolumeConverterCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String getSymbol(CTRObjectID id) {
        String symbol = SYMBOLS.get(id.toString());
        return symbol == null ? DEFAULT_SYMBOL : symbol;
    }

    public BigDecimal getOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        return DEFAULT_OVERFLOW;
    }

    public int[] getValueLengths(CTRObjectID id) {
        int[] lengths = VALUE_LENGTHS.get(id.toString());
        return lengths == null ? DEFAULT_LENGTH : lengths;
    }

    public Unit getUnit(CTRObjectID id, int valueNumber) {
        Unit unit = UNITS.get(id.toString());
        return unit == null ? DEFAULT_UNIT : unit;
    }

}
