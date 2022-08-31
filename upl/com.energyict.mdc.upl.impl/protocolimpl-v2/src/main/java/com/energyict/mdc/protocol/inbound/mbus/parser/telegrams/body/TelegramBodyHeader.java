package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body;


import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.TelegramField;

import java.util.Arrays;

public class TelegramBodyHeader {

    private TelegramField ciField;
    private TelegramField accNrField;
    private TelegramField statusField;
    private TelegramField sigField;

    public TelegramBodyHeader() {

    }

    public void createTelegramBodyHeader(String[] bodyHeader) {
        // TODO: check CI-Field for correct Header length
        this.setCiField(bodyHeader[0]);
        this.setAccNrField(bodyHeader[1]);
        this.setStatusField(bodyHeader[2]);
        this.setSigField(Arrays.copyOfRange(bodyHeader, 3, 5));
    }

    public void setCiField(String ciField) {
        this.ciField = new TelegramField();
        this.ciField.addFieldPart(ciField);
    }

    public void setAccNrField(String accNrField) {
        this.accNrField = new TelegramField();
        this.accNrField.addFieldPart(accNrField);
    }

    public void setStatusField(String statusField) {
        this.statusField = new TelegramField();
        this.statusField.addFieldPart(statusField);
    }

    public void setSigField(String[] sigField) {
        this.sigField = new TelegramField();
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

    public void debugOutput() {
        if(this.ciField != null) {
            System.out.println("Type of TelegramBodyHeader: " + getTelegramType());
        }

        if(this.accNrField != null) {
            System.out.println("AccessNumber: " + getAccessNumber());
        }

        if(this.statusField != null) {
            // TODO: parse Status-Field
            System.out.println("StatusField: " + this.getStatusField());
            int status = Integer.parseInt(this.getStatusField(), 16);
            switch ((status & 0x03)) {
                case 1:
                    System.out.println("\t - application busy [!]");
                    break;

                case 2:
                    System.out.println("\t - application error [!]");
                    break;

                case 3:
                    System.out.println("\t - abnormal situation [!]");
                    break;
            }

            switch ((status & 0x04) >> 2) {
                case 0:
                    System.out.println("\t - power ok");
                    break;
                case 1:
                    System.out.println("\t - power low [!]");
                    break;
            }

            switch ((status & 0x08) >> 3) {
                case 0:
                    System.out.println("\t - no permanent error");
                    break;
                case 1:
                    System.out.println("\t - permanent error [!]");
                    break;
            }

            switch ((status & 0x10) >> 4) {
                case 0:
                    System.out.println("\t - no temporary error");
                    break;
                case 1:
                    System.out.println("\t - temporary error [!]");
                    break;
            }

            switch ((status & 0x20) >> 4) {
                case 0:
                    //System.out.println("\t - no temporary error");
                    break;
                case 1:
                    System.out.println("\t - manufacturer specific 1 [!]");
                    break;
            }

            switch ((status & 0x40 >> 5) ) {
                case 0:
                    //System.out.println("\t - no temporary error");
                    break;
                case 1:
                    System.out.println("\t - manufacturer specific 2 [!]");
                    break;
            }

            switch ((status & 0x80 >> 6) ) {
                case 0:
                    //System.out.println("\t - no temporary error");
                    break;
                case 1:
                    System.out.println("\t - manufacturer specific 3 [!]");
                    break;
            }

        }

        if(this.sigField != null) {
            // TODO: parse Sig-Field
            System.out.println("Sig-Field1: " + this.sigField.getFieldParts().get(0));
            System.out.println("Sig-Field2: " + this.sigField.getFieldParts().get(1));
        }
    }
}