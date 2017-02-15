/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cps.ValuesRangeConflict;
import com.elster.jupiter.cps.ValuesRangeConflictType;
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

    public CustomPropertySetIntervalConflictInfo(CustomPropertySetInfo customPropertySet, ValuesRangeConflict conflict) {
        this.customPropertySet = customPropertySet;
        this.conflictType = conflict.getType().name();
        this.message = conflict.getMessage();
        this.editable = conflict.getType().equals(ValuesRangeConflictType.RANGE_INSERTED);
        this.conflictAtStart = conflict.getType().equals(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_START);
        this.conflictAtEnd = conflict.getType().equals(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_END);
        if(conflict.getType().equals(ValuesRangeConflictType.RANGE_OVERLAP_DELETE)){
            this.conflictAtStart = true;
            this.conflictAtEnd = true;
        }
    }
}