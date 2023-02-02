package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body;


import com.energyict.mdc.protocol.inbound.mbus.MerlinLogger;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.TelegramField;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.Converter;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.TelegramEncoding;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.TelegramFunctionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class TelegramBodyPayload {

    private final MerlinLogger logger;
    private TelegramField bodyFieldEncrypted;
    private TelegramField bodyFieldDecrypted;
    private List<TelegramVariableDataRecord> records;
    private boolean hasBeenEncrypted = true;

    public TelegramBodyPayload(MerlinLogger logger) {
        this.logger = logger;
        this.bodyFieldEncrypted = new TelegramField(logger);
        this.bodyFieldDecrypted = new TelegramField(logger);
    }

    public void createTelegramBodyPayload(String[] bodyPayload) {
        this.bodyFieldEncrypted = new TelegramField(logger);
        this.bodyFieldEncrypted.addFieldParts(bodyPayload);
    }

    public void setDecryptedTelegramBodyPayload(String[] bodyPayload) {
        this.bodyFieldDecrypted = new TelegramField(logger);
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
                    logger.error("Invalid record size: " + this.records.size());
                    // TODO: throw exception
                }

                if(!this.records.get(0).getDif().isFillByte()) {
                    logger.error("Fill byte 1 is invalid: " + this.records.get(0).getDif().getFieldParts().get(0));
                    // TODO: throw exception because first two records need to be 2F in encryption mode
                }
                if(!this.records.get(1).getDif().isFillByte()) {
                    logger.error("Fill byte 1 is invalid: " + this.records.get(1).getDif().getFieldParts().get(0));
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
        TelegramVariableDataRecord rec = new TelegramVariableDataRecord(logger);
        DIFTelegramField dif = new DIFTelegramField(logger);
        dif.addFieldPart(this.bodyFieldDecrypted.getFieldParts().get(startPosition));
        dif.parse(this.bodyFieldDecrypted.getFieldParts().size() - startPosition);

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
        VIFTelegramField vif = new VIFTelegramField(logger);
        if ((dif.getFunctionType() != TelegramFunctionType.USER_DEFINED_CELL_ID)
                && (!TelegramEncoding.ENCODING_NULL.equals(dif.getDataFieldEncoding()))) {
            vif.addFieldPart(this.bodyFieldDecrypted.getFieldParts().get(startPosition + 1 + difeList.size()));
            vif.setParent(rec);
            vif.parse();
        } else {
            // User-Defined Cell Id doesn't seem to use a VIF
            startPosition--;
        }

        rec.setVif(vif);

        List<VIFETelegramField> vifeList = new ArrayList<VIFETelegramField>();
        if (vif.isProfile()) {
            logger.debug("\t* Profile detected, getting single VIFE");
            VIFETelegramField vife = this.processSingleVIFEField(this.bodyFieldDecrypted.getFieldParts().get(startPosition + 2 + difeList.size()));
            vife.setExtensionBit(false);
            vifeList.add(vife);
        } else {
            if (vif.isExtensionBit()) {
                // increase startPosition by 2 (DIF and VIF) and the number of DIFEs
                vifeList = this.parseVIFEFields(startPosition + 2 + difeList.size());
            }
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
                TelegramDataVariableField dataFieldVariable = new TelegramDataVariableField(logger);

                String lVar = this.bodyFieldDecrypted.getFieldParts().get(lowerBoundary);
                int variableLength;
                if (vif.isProfile()) {
                    logger.debug("\tLVAR = " + lVar + " direct conversion for profiles");
                    variableLength = Converter.hexToInt(lVar);
                } else {
                    variableLength = dataFieldVariable.getVariableLengthFieldLength(lVar);
                    logger.debug("\tLVAR = " + lVar + " conversion to " + variableLength);
                }
                if (variableLength > 0) {
                    // store VIF meta data
                    String[] fieldParts = Arrays.copyOfRange(this.bodyFieldDecrypted.getFieldPartsAsArray(), lowerBoundary, upperBoundary);

                    lowerBoundary = upperBoundary;
                    upperBoundary = lowerBoundary + variableLength - dif.getDataFieldLength();

                    List<String> payload = this.bodyFieldDecrypted.getFieldParts().subList(lowerBoundary, upperBoundary);

                    dataFieldVariable.addFieldParts(fieldParts);
                    dataFieldVariable.addVariableLengthPayload(payload);
                    dataFieldVariable.parse(vif.isProfile());
                    rec.setDataField(dataFieldVariable);
                } else {
                    logger.warn("Cannot parse variable-field-length " + variableLength);
                }
            } else {
                TelegramDataField dataField = new TelegramDataField(rec, logger);
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

        while(extensionBitSet && position < this.bodyFieldDecrypted.getFieldParts().size()) {
            dife = this.processSingleDIFEField(this.bodyFieldDecrypted.getFieldParts().get(position));
            difeList.add(dife);
            extensionBitSet = dife.isExtensionBit();
            position++;
        }

        return difeList;
    }

    private DIFETelegramField processSingleDIFEField(String fieldValue) {
        logger.debug("\t# DIFE: " + fieldValue);
        DIFETelegramField dife = new DIFETelegramField(logger);
        dife.addFieldPart(fieldValue);

        return dife;
    }

    private List<VIFETelegramField> parseVIFEFields(int position) {
        List<VIFETelegramField> vifeList = new ArrayList<VIFETelegramField>();
        boolean extensionBitSet = true;
        VIFETelegramField vife = null;

        while(extensionBitSet && position < this.bodyFieldDecrypted.getFieldParts().size()) {
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
        logger.debug("\t** VIFE: " + fieldValue);

        int iFieldValue = Converter.hexToInt(fieldValue);
        VIFETelegramField vife = new VIFETelegramField(logger);
        vife.addFieldPart(fieldValue);

        if((iFieldValue & VIFTelegramField.EXTENSION_BIT_MASK) == VIFTelegramField.EXTENSION_BIT_MASK) {
            if (iFieldValue != 0xc3) {
                vife.setExtensionBit(true);
                logger.debug("\t\t - extension: true ");
            } else {
                //vife.setExtensionBit(true);
                logger.debug("\t - back-flow VIFE, reducing length");
            }
        }

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

    public void debugOutput(StringJoiner joiner) {
        joiner.add("-------------------------------------------------------------");
        joiner.add("-------------------- BEGIN BODY PAYLOAD ---------------------");
        joiner.add("-------------------------------------------------------------");
        joiner.add("Encrypted: " + this.hasBeenEncrypted);
        if(this.records != null) {
            for(int i = 0; i < this.records.size(); i++) {
                joiner.add("RECORD: " + i);
                this.records.get(i).debugOutput(joiner);
            }
        }
        joiner.add("-------------------------------------------------------------");
        joiner.add("--------------------- END BODY PAYLOAD ----------------------");
        joiner.add("-------------------------------------------------------------");
    }

    public List<TelegramVariableDataRecord> getRecords() {
        // TODO Auto-generated method stub
        return this.records;
    }

    public String[] getEncryptedPayload() {
        return this.bodyFieldEncrypted.getFieldPartsAsArray();
    }
}