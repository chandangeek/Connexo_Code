/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ViewObjectConfig.java
 *
 * Created on 8 december 2006, 15:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ViewObjectConfig extends AbstractDataDefinition {

    private ViewConfigType[] viewConfigTypes;

    /**
     * Creates a new instance of ViewObjectConfig
     */
    public ViewObjectConfig(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ViewObjectConfig:\n");
        for (int i=0;i<getViewConfigTypes().length;i++) {
            strBuff.append("       viewConfigTypes["+i+"]="+getViewConfigTypes()[i]+"\n");
        }
        return strBuff.toString();
    }

    protected int getVariableName() {
        return 65; // DLMS_VIEW_OBJECT_CONFIG
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        int range = data.length/ViewConfigType.size();
        setViewConfigTypes(new ViewConfigType[range]);
        for(int i=0;i<getViewConfigTypes().length;i++) {
            getViewConfigTypes()[i] = new ViewConfigType(data,offset);
            offset+=ViewConfigType.size();
        }

    }

    public ViewConfigType[] getViewConfigTypes() {
        return viewConfigTypes;
    }

    public void setViewConfigTypes(ViewConfigType[] viewConfigTypes) {
        this.viewConfigTypes = viewConfigTypes;
    }


}
