package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 10:51:36
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractCTRObject<T extends AbstractCTRObject> extends AbstractField<T> {

    private CTRObjectID id;
    private int access;
    private int qlf;
    private String symbol;
    private double[] def;

    public abstract Unit parseUnit(CTRObjectID id, int valueNumber);

    protected abstract String parseSymbol(CTRObjectID id);

    protected abstract int[] parseValueLengths(CTRObjectID id);

    public abstract BigDecimal parseOverflowValue(CTRObjectID id, int valueNumber, Unit unit);

    protected int sum(int[] valueLength) {
        int sum = 0;
        for (int i : valueLength) {
            sum += i;
        }
        return sum;
    }

    protected int getCommonOverflow(Unit unit) {
        int overflow = 0;
        if (unit == Unit.get(BaseUnit.HOUR)) {
            overflow = 24;
        }
        if (unit == Unit.get(BaseUnit.MINUTE)) {
            overflow = 60;
        }
        if (unit == Unit.get(BaseUnit.SECOND)) {
            overflow = 60;
        }
        if (unit == Unit.get(BaseUnit.DAY)) {
            overflow = 32;
        }
        if (unit == Unit.get(BaseUnit.MONTH)) {
            overflow = 12;
        }
        if (unit == Unit.get(BaseUnit.YEAR)) {
            overflow = 99;
        }
        return overflow;
    }

    public CTRObjectID getId() {
        return id;
    }

    protected void setId(CTRObjectID id) {
        this.id = id;
    }

    public int getQlf() {
        return qlf;
    }

    protected void setQlf(int qlf) {
        this.qlf = qlf;
    }

    public int getAccess() {
        return access;
    }

    protected void setAccess(int access) {
        this.access = access;
    }

    public String getSymbol() {
        return symbol;
    }

    protected void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double[] getDefault() {
        return def;
    }

    protected void setDefault(double[] def) {
        this.def = def;
    }

}