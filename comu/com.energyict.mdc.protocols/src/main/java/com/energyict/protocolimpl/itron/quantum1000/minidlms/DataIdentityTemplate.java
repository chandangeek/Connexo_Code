/*
 * DataIdentityTemplate_1.java
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
public class DataIdentityTemplate extends AbstractDataDefinition {

    /** Creates a new instance of DataIdentityTemplate_1 */
    public DataIdentityTemplate(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    protected int getVariableName() {
        return -1;
    }

    protected void parse(byte[] data) throws IOException {

    }
}
