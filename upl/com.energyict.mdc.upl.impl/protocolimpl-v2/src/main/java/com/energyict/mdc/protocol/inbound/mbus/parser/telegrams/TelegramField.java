package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams;


import com.energyict.mdc.protocol.inbound.mbus.MerlinLogger;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.Converter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TelegramField {

    protected final MerlinLogger logger;
    protected List<String> fieldParts;
    protected String parsedValue;
    protected Instant timeValue;
    protected Map<Integer, Long> parsedIntervals;

    public TelegramField(MerlinLogger logger) {
        this.fieldParts = new ArrayList<String>();
        this.logger = logger;
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

    public Optional<Instant> getTimeValue() {
        return Optional.ofNullable(timeValue);
    }

    public Map<Integer, Long> getParsedIntervals() {
        return parsedIntervals;
    }
}