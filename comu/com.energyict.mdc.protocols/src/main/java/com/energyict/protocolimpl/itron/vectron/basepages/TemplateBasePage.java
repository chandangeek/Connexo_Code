/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RealTimeBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.vectron.basepages;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class TemplateBasePage extends AbstractBasePage {


    /** Creates a new instance of RealTimeBasePage */
    public TemplateBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x0,0x0);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        //getBasePagesFactory().getFulcrum().getTimeZone()
    }


}