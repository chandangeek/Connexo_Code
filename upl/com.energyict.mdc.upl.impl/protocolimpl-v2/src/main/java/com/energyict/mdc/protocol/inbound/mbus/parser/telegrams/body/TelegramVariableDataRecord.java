package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body;


import com.energyict.mdc.protocol.inbound.mbus.MerlinLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class TelegramVariableDataRecord {

    private final MerlinLogger logger;
    private DIFTelegramField dif;
    private List<DIFETelegramField> difes;
    private VIFTelegramField vif;
    private List<VIFETelegramField> vifes;

    private TelegramDataField dataField;

    public TelegramVariableDataRecord(MerlinLogger logger) {
        this.logger = logger;
    }

    public void parse() {
        this.dif = new DIFTelegramField(logger);
        this.dif.parse();
        this.vif = new VIFTelegramField(logger);
        this.vif.setParent(this);
        this.vif.parse();

        this.dataField = new TelegramDataField(logger);
        this.dataField.setParent(this);
        this.dataField.parse();
    }

    public DIFTelegramField getDif() {
        return dif;
    }

    public void setDif(DIFTelegramField dif) {
        this.dif = dif;
    }

    public void addDifes(List<DIFETelegramField> difes) {
        if(this.difes == null) {
            this.difes = new ArrayList<DIFETelegramField>();
            this.difes.addAll(difes);
        }
    }

    public VIFTelegramField getVif() {
        return vif;
    }

    public void setVif(VIFTelegramField vif) {
        this.vif = vif;
    }

    public void addVifes(List<VIFETelegramField> vifes) {
        if(this.vifes == null) {
            this.vifes = new ArrayList<VIFETelegramField>();
            this.vifes.addAll(vifes);
        }
    }

    public TelegramDataField getDataField() {
        return this.dataField;
    }

    public void setDataField(TelegramDataField dataField) {
        this.dataField = dataField;
    }

    public void getDataField(TelegramDataField dataField) {
        this.dataField = dataField;
    }

    public void debugOutput(StringJoiner joiner) {
        joiner.add("VARIABLE DATA RECORD: ");
        if(this.dif != null) {
            this.dif.debugOutput(joiner);
        }

        if(this.difes != null) {
            for(int i = 0; i < this.difes.size(); i++) {
                this.difes.get(i).debugOutput(joiner);
            }
        }

        if(this.vif != null) {
            this.vif.debugOutput();
        }

        if(this.vifes != null) {
            for(int i = 0; i < this.vifes.size(); i++) {
                this.vifes.get(i).debugOutput(joiner);
            }
        }

        if(this.dataField != null) {
            this.dataField.debugOutput(joiner);
        }
        joiner.add("==================================================");
    }

}

