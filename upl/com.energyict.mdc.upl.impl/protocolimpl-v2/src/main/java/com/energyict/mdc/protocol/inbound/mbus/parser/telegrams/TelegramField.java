package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams;


import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.Converter;

import java.util.ArrayList;
import java.util.List;

public class TelegramField {

    protected List<String> fieldParts;
    protected String parsedValue;

    public TelegramField() {
        this.fieldParts = new ArrayList<String>();
    }

    public void addFieldPart(String value) {
        this.fieldParts.add(value);
    }

    public void addFieldParts(String[] values) {
        for(int i = 0; i < values.length; i++) {
            this.addFieldPart(values[i]);
        }
    }

    public void clearTelegramPart() {
        this.fieldParts.clear();
    }

    public List<String> getFieldParts() {
        return fieldParts;
    }

    public String[] getFieldPartsAsArray() {
        return this.fieldParts.toArray(new String[this.fieldParts.size()]);
    }

    public String getFieldPartsAsString() {
        String retString = new String();
        for(int i = 0; i < fieldParts.size(); i++) {
            if(i+1 == fieldParts.size()) {
                retString += fieldParts.get(i);
            }
            else {
                retString += fieldParts.get(i) + " ";
            }
        }
        return retString;
    }

    public byte[] getFieldAsByteArray() {
        return Converter.convertStringListToByteArray(this.fieldParts);
    }

    public String getParsedValue() {
        return this.parsedValue;
    }

}