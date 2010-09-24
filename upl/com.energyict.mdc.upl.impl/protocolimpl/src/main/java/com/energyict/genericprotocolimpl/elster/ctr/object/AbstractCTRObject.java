package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveParser;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 10:51:36
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractCTRObject {
    private CTRObjectID id;
    private int access;
    private int qlf;
    private String symbol;
    private BigDecimal overflowValue;

    protected int sum(int[] valueLength) {
        int sum = 0;
        for(int i:valueLength) {sum +=i;}
        return sum;
    }

    protected abstract int[] parseValueLengths();

    public abstract void parse(byte[] rawData, int offset);

    public abstract BigDecimal parseOverflowValue();

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

}
