package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomPropertySetIntervalConflictInfo {
    public String conflictType;
    public String message;
    public boolean editable;
    public boolean conflictAtStart;
    public boolean conflictAtEnd;
    public CustomPropertySetInfo customPropertySet;

    public CustomPropertySetIntervalConflictInfo() {
    }

    public CustomPropertySetIntervalConflictInfo(MessageSeed message, CustomPropertySetInfo customPropertySet, boolean editable) {
        this.message = message.getDefaultFormat();
        this.conflictType = message.getKey();
        this.customPropertySet = customPropertySet;
        this.editable = editable;
        conflictAtStart = message.getKey().contains("edit.historical.values.overlap.can.update.start");
        conflictAtEnd = message.getKey().contains("edit.historical.values.overlap.can.update.end");
        if(message.getKey().contains("edit.historical.values.overlap.can.delete")){
            conflictAtStart = message.getKey().contains("edit.historical.values.overlap.can.delete");
            conflictAtEnd = message.getKey().contains("edit.historical.values.overlap.can.delete");
        }
    }
}