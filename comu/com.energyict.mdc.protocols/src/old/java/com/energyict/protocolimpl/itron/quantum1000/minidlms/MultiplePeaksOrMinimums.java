/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MultiplePeaksOrMinimums.java
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
public class MultiplePeaksOrMinimums extends AbstractDataDefinition {

    private MultiplePeaksType[] multiplePeaksTypes;

    /**
     * Creates a new instance of MultiplePeaksOrMinimums
     */
    public MultiplePeaksOrMinimums(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MultiplePeaksOrMinimums:\n");
        for (int i=0;i<getMultiplePeaksTypes().length;i++) {
            strBuff.append("       multiplePeaksTypes["+i+"]="+getMultiplePeaksTypes()[i]+"\n");
        }
        return strBuff.toString();
    }


    protected int getVariableName() {
        return 50; // DLMS_MULTIPLE_PEAKS_MINS
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setMultiplePeaksTypes(new MultiplePeaksType[6]);
        for (int i=0;i<getMultiplePeaksTypes().length;i++) {
            getMultiplePeaksTypes()[i] = new MultiplePeaksType(data, offset,getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone());
            offset+=MultiplePeaksType.size();
        }
    }

    public MultiplePeaksType[] getMultiplePeaksTypes() {
        return multiplePeaksTypes;
    }

    public void setMultiplePeaksTypes(MultiplePeaksType[] multiplePeaksTypes) {
        this.multiplePeaksTypes = multiplePeaksTypes;
    }
}
