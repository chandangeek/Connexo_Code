package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body;


import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.TelegramField;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.TelegramEncoding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TelegramBodyPayload {

    private TelegramField bodyFieldEncrypted;
    private TelegramField bodyFieldDecrypted;
    private List<TelegramVariableDataRecord> records;
    private boolean hasBeenEncrypted = true;

    public TelegramBodyPayload() {
        this.bodyFieldEncrypted = new TelegramField();
        this.bodyFieldDecrypted = new TelegramField();
    }

    public void createTelegramBodyPayload(String[] bodyPayload) {
        this.bodyFieldEncrypted = new TelegramField();
        this.bodyFieldEncrypted.addFieldParts(bodyPayload);
    }

    public void setDecryptedTelegramBodyPayload(String[] bodyPayload) {
        this.bodyFieldDecrypted = new TelegramField();
        this.bodyFieldDecrypted.addFieldParts(bodyPayload);
    }

    public void parse() {
        if(this.records == null) {
            this.records = new ArrayList<TelegramVariableDataRecord>();
        }
        this.records.clear();

        if (this.hasBeenEncrypted) {
            int startPosition = 0;
            // if encryption has been used we have to adjust the start position
            // of the first variable data record by some check fields
            if(this.hasBeenEncrypted) {
                this.parseVariableDataRecord(startPosition);
                this.parseVariableDataRecord(startPosition + 1);

                if(this.records.size() != 2) {
                    System.err.println("Invalid record size: " + this.records.size());
                    // TODO: throw exception
                }

                if(!this.records.get(0).getDif().isFillByte()) {
                    System.err.println("Fill byte 1 is invalid: " + this.records.get(0).getDif().getFieldParts().get(0));
                    // TODO: throw exception because first two records need to be 2F in encryption mode
                }
                if(!this.records.get(1).getDif().isFillByte()) {
                    System.err.println("Fill byte 1 is invalid: " + this.records.get(1).getDif().getFieldParts().get(0));
                    // TODO: throw exception because first two records need to be 2F in encryption mode
                }
                startPosition = 2;
            }

            while(startPosition < this.bodyFieldDecrypted.getFieldParts().size()) {
                startPosition = this.parseVariableDataRecord(startPosition);
            }
        }
    }

    private int parseVariableDataRecord(int startPosition) {
        TelegramVariableDataRecord rec = new TelegramVariableDataRecord();
        DIFTelegramField dif = new DIFTelegramField();
        dif.addFieldPart(this.bodyFieldDecrypted.getFieldParts().get(startPosition));
        dif.parse();

        rec.setDif(dif);

        if (dif.isFillByte()) {
            // fill byte for AES, do not interpret
            this.records.add(rec);
            return startPosition + 1;
        }

        List<DIFETelegramField> difeList = new ArrayList<DIFETelegramField>();
        if(dif.isExtensionBit()) {
            // increase start position by one (because we have already read DIF)
            difeList = this.parseDIFEFields(startPosition + 1);
        }

        rec.addDifes(difeList);

        // increase startPosition by 1 (DIF) and the number of DIFEs
        VIFTelegramField vif = new VIFTelegramField();
        vif.addFieldPart(this.bodyFieldDecrypted.getFieldParts().get(startPosition + 1 + difeList.size()));
        vif.setParent(rec);
        vif.parse();

        rec.setVif(vif);

        List<VIFETelegramField> vifeList = new ArrayList<VIFETelegramField>();
        if(vif.isExtensionBit()) {
            // increase startPosition by 2 (DIF and VIF) and the number of DIFEs
            vifeList = this.parseVIFEFields(startPosition + 2 + difeList.size());
        }

        rec.addVifes(vifeList);

        // increase startPosition by 2 (DIF and VIF) and the number of DIFEs and
        // the number of VIFEs
        int lowerBoundary = startPosition + 2 + difeList.size() + vifeList.size();
        int upperBoundary = lowerBoundary + dif.getDataFieldLength();

        if(dif.getDataFieldLength() == 0) {
            // no data values, nothing todo, continue with the next one
            return upperBoundary;
        }

        if(this.bodyFieldDecrypted.getFieldParts().size() >= upperBoundary) {
            if (dif.getDataFieldEncoding().equals(TelegramEncoding.ENCODING_VARIABLE_LENGTH)){
                TelegramDataVariableField dataFieldVariable = new TelegramDataVariableField();
                int variableLength = dataFieldVariable.getVariableLengthFieldLength(this.bodyFieldDecrypted.getFieldParts().get(lowerBoundary));
                if (variableLength > 0) {
                    String[] fieldParts = Arrays.copyOfRange(this.bodyFieldDecrypted.getFieldPartsAsArray(), lowerBoundary, upperBoundary);
                    List<String> payload = this.bodyFieldDecrypted.getFieldParts().subList(upperBoundary, upperBoundary + variableLength - 2);
                    dataFieldVariable.addFieldParts(fieldParts);
                    dataFieldVariable.addVariableLengthPayload(payload);
                    dataFieldVariable.parse();
                } else {
                    System.err.println("Cannot parse variable-field-length " + variableLength);
                }
            } else {
                TelegramDataField dataField = new TelegramDataField(rec);
                String[] fieldParts = Arrays.copyOfRange(this.bodyFieldDecrypted.getFieldPartsAsArray(), lowerBoundary, upperBoundary);
                dataField.addFieldParts(fieldParts);

                dataField.parse();
                rec.setDataField(dataField);
            }
        }

        this.records.add(rec);

        return upperBoundary;
    }

    private List<DIFETelegramField> parseDIFEFields(int position) {
        List<DIFETelegramField> difeList = new ArrayList<DIFETelegramField>();
        boolean extensionBitSet = true;
        DIFETelegramField dife = null;

        while(extensionBitSet) {
            if(this.bodyFieldDecrypted.getFieldParts().size() < position) {
                // TODO: throw exception
            }
            dife = this.processSingleDIFEField(this.bodyFieldDecrypted.getFieldParts().get(position));
            difeList.add(dife);
            extensionBitSet = dife.isExtensionBit();
            position++;
        }

        return difeList;
    }

    private DIFETelegramField processSingleDIFEField(String fieldValue) {
        DIFETelegramField dife = new DIFETelegramField();
        dife.addFieldPart(fieldValue);

        return dife;
    }

    private List<VIFETelegramField> parseVIFEFields(int position) {
        List<VIFETelegramField> vifeList = new ArrayList<VIFETelegramField>();
        boolean extensionBitSet = true;
        VIFETelegramField vife = null;

        while(extensionBitSet) {
            if(this.bodyFieldDecrypted.getFieldParts().size() < position) {
                // TODO: throw exception
            }
            vife = this.processSingleVIFEField(this.bodyFieldDecrypted.getFieldParts().get(position));
            vifeList.add(vife);
            extensionBitSet = vife.isExtensionBit();
            position++;
        }

        return vifeList;
    }

    private VIFETelegramField processSingleVIFEField(String fieldValue) {
        VIFETelegramField vife = new VIFETelegramField();
        vife.addFieldPart(fieldValue);

        return vife;
    }

    public String getPayloadAsString() {
        return this.bodyFieldEncrypted.getFieldPartsAsString();
    }

    public String getDecryptedPayloadAsString() {
        return this.bodyFieldDecrypted.getFieldPartsAsString();
    }

    public List<String> getPayloadAsList() {
        return this.bodyFieldEncrypted.getFieldParts();
    }

    public List<String> getDecryptedPayloadAsList() {
        return this.bodyFieldDecrypted.getFieldParts();
    }

    public boolean isHasBeenEncrypted() {
        return hasBeenEncrypted;
    }

    public void setHasBeenEncrypted(boolean hasBeenEncrypted) {
        this.hasBeenEncrypted = hasBeenEncrypted;
    }

    public void debugOutput() {
        System.out.println("-------------------------------------------------------------");
        System.out.println("-------------------- BEGIN BODY PAYLOAD ---------------------");
        System.out.println("-------------------------------------------------------------");
        System.out.println("Encrypted: " + this.hasBeenEncrypted);
        if(this.records != null) {
            for(int i = 0; i < this.records.size(); i++) {
                System.out.println("RECORD: " + i);
                this.records.get(i).debugOutput();
            }
        }
        System.out.println("-------------------------------------------------------------");
        System.out.println("--------------------- END BODY PAYLOAD ----------------------");
        System.out.println("-------------------------------------------------------------");
    }

    public List<TelegramVariableDataRecord> getRecords() {
        // TODO Auto-generated method stub
        return this.records;
    }

}