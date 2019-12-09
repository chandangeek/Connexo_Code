package com.energyict.protocolimplv2.nta.esmr50.common.registers.enums;

public abstract class LTEMonitoringWrapper {

    protected static final String SEP = ";\n";
    protected boolean decoded = false;
    protected String errorMessage;
    protected long t3402;
    protected long t3412;
    protected int rsrq;
    protected int rsrp;
    protected int qRxlevMin;

    public boolean isDecoded() {
        return decoded;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public abstract String toString();

    public long getT3402() {
        return t3402;
    }

    public long getT3412() {
        return t3412;
    }

    public int getRsrq() {
        return rsrq;
    }

    public int getRsrp() {
        return rsrp;
    }

    public int getqRxlevMin() {
        return qRxlevMin;
    }
}
