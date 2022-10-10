package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body;


import com.energyict.mdc.protocol.inbound.mbus.MerlinLogger;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.TelegramField;

import java.util.Arrays;
import java.util.StringJoiner;

public class TelegramBodyHeader {

    private final MerlinLogger logger;
    private TelegramField ciField;
    private TelegramField accNrField;
    private TelegramField statusField;
    private TelegramField sigField;

    public TelegramBodyHeader(MerlinLogger logger) {
        this.logger = logger;
    }

    public void createTelegramBodyHeader(String[] bodyHeader) {
        // TODO: check CI-Field for correct Header length
        this.setCiField(bodyHeader[0]);
        this.setAccNrField(bodyHeader[1]);
        this.setStatusField(bodyHeader[2]);
        this.setSigField(Arrays.copyOfRange(bodyHeader, 3, 5));
    }

    public void setCiField(String ciField) {
        this.ciField = new TelegramField(logger);
        this.ciField.addFieldPart(ciField);
    }

    public void setAccNrField(String accNrField) {
        this.accNrField = new TelegramField(logger);
        this.accNrField.addFieldPart(accNrField);
    }

    public void setStatusField(String statusField) {
        this.statusField = new TelegramField(logger);
        this.statusField.addFieldPart(statusField);
    }

    public void setSigField(String[] sigField) {
        this.sigField = new TelegramField(logger);
        this.sigField.addFieldParts(sigField);
    }

    public byte[] getAESCBCInitVectorPart() {
        byte[] aesCbcInitVectorPart = new byte[8];
        byte[] accNrFieldByteArr = this.accNrField.getFieldAsByteArray();
        System.arraycopy(accNrFieldByteArr, 0, aesCbcInitVectorPart, 0, accNrFieldByteArr.length);
        System.arraycopy(accNrFieldByteArr, 0, aesCbcInitVectorPart, accNrFieldByteArr.length, accNrFieldByteArr.length);
        System.arraycopy(accNrFieldByteArr, 0, aesCbcInitVectorPart, accNrFieldByteArr.length + 1, accNrFieldByteArr.length);
        System.arraycopy(accNrFieldByteArr, 0, aesCbcInitVectorPart, accNrFieldByteArr.length + 2, accNrFieldByteArr.length);
        System.arraycopy(accNrFieldByteArr, 0, aesCbcInitVectorPart, accNrFieldByteArr.length + 3, accNrFieldByteArr.length);
        System.arraycopy(accNrFieldByteArr, 0, aesCbcInitVectorPart, accNrFieldByteArr.length + 4, accNrFieldByteArr.length);
        System.arraycopy(accNrFieldByteArr, 0, aesCbcInitVectorPart, accNrFieldByteArr.length + 5, accNrFieldByteArr.length);
        System.arraycopy(accNrFieldByteArr, 0, aesCbcInitVectorPart, accNrFieldByteArr.length + 6, accNrFieldByteArr.length);
        return aesCbcInitVectorPart;
    }

    public String getTelegramType(){
        return this.ciField.getFieldParts().get(0);
    }

    public String getAccessNumber() {
        return this.accNrField.getFieldParts().get(0);
    }

    public String getStatusField() {
        return this.statusField.getFieldParts().get(0);
    }

    public void debugOutput(StringJoiner joiner) {
        if(this.ciField != null) {
            joiner.add("Type of TelegramBodyHeader: " + getTelegramType());
        }

        if(this.accNrField != null) {
            joiner.add("AccessNumber: " + getAccessNumber());
        }

        if(this.statusField != null) {
            // TODO: parse Status-Field
            joiner.add("StatusField: " + this.getStatusField());
            int status = Integer.parseInt(this.getStatusField(), 16);
            switch ((status & 0x03)) {
                case 1:
                    joiner.add("\t - application busy [!]");
                    break;

                case 2:
                    joiner.add("\t - application error [!]");
                    break;

                case 3:
                    joiner.add("\t - abnormal situation [!]");
                    break;
            }

            switch ((status & 0x04) >> 2) {
                case 0:
                    joiner.add("\t - power ok");
                    break;
                case 1:
                    joiner.add("\t - power low [!]");
                    break;
            }

            switch ((status & 0x08) >> 3) {
                case 0:
                    joiner.add("\t - no permanent error");
                    break;
                case 1:
                    joiner.add("\t - permanent error [!]");
                    break;
            }

            switch ((status & 0x10) >> 4) {
                case 0:
                    joiner.add("\t - no temporary error");
                    break;
                case 1:
                    joiner.add("\t - temporary error [!]");
                    break;
            }

            // manufacturer specific 1
            switch ((status & 0x20) >> 5) {
                case 0:
                    joiner.add("\t - leakage alarm cleared");
                    break;
                case 1:
                    joiner.add("\t - leakage alarm (MLF) [!]");
                    break;
            }

            // manufacturer specific 2
            switch ((status & 0x40 >> 6) ) {
                case 0:
                    joiner.add("\t - overconsumption alarm cleared");
                    break;
                case 1:
                    joiner.add("\t - actual Alarm burst (MBA) [!]");
                    break;
            }

            // manufacturer specific 3
            switch ((status & 0x80 >> 7) ) {
                case 0:
                    joiner.add("\t - device paired");
                    break;
                case 1:
                    joiner.add("\t - a removal (or cable cut) detected [!]");
                    break;
            }

        }

        if(this.sigField != null) {
            // TODO: parse Sig-Field
            joiner.add("Sig-Field1: " + this.sigField.getFieldParts().get(0));
            joiner.add("Sig-Field2: " + this.sigField.getFieldParts().get(1));
        }
    }
}