package com.energyict.protocolimpl.iec1107.a140;

import com.energyict.cbo.Quantity;

import java.util.Date;

public class HistoricalRegister {

    private int triggerSrc;
    private Date time = null;

    private Quantity importRegister;
    private Quantity exportRegister;
    private Quantity [] tou = new Quantity[4];
    private TouSourceRegister tariffSources;

    HistoricalRegister(
            int triggerScr, Date time, Quantity importRegister,
            Quantity exportRegister, Quantity tou1, Quantity tou2,
            Quantity tou3, Quantity tou4, TouSourceRegister tariffSources ){

        this.triggerSrc = triggerScr;
        this.time = time;
        this.importRegister = importRegister;
        this.exportRegister = exportRegister;
        this.tou[0] = tou1;
        this.tou[1] = tou2;
        this.tou[2] = tou3;
        this.tou[3] = tou4;
        this.tariffSources = tariffSources;

    }

    public int getTriggerSource( ){
        return triggerSrc;
    }

    public Date getTime( ){
        return time;
    }

    public Quantity getExportRegister() {
        return exportRegister;
    }

    public Quantity getImportRegister() {
        return importRegister;
    }

    public Quantity getTou( int touIndex ) {
        return tou[touIndex];
    }

   public TouSourceRegister getTariffSources() {
        return tariffSources;
    }

    public String toString( ){
        String rslt =
            "HistoricalRegister " +
            "[trgSource=" + HistoricalRegisterSet.triggerSrc[triggerSrc] + "], " +
            "[time=" + time + "], " +
            "[import=" + importRegister + "], " +
            "[export=" + exportRegister + "], " +
            "[tou1=" + tou[0] + "], " +
            "[tou2=" + tou[1] + "], " +
            "[tou3=" + tou[2] + "], " +
            "[tou4=" + tou[3] + "], " +
            "[tariffSrc " + tariffSources + "]";
        return rslt;
    }

}
