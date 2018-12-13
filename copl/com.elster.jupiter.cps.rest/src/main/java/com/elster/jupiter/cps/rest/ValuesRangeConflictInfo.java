/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValuesRangeConflictInfo {
    public String conflictType;
    public String message;
    public boolean editable;
    public boolean conflictAtStart;
    public boolean conflictAtEnd;
    public CustomPropertySetInfo<?> customPropertySet;
}