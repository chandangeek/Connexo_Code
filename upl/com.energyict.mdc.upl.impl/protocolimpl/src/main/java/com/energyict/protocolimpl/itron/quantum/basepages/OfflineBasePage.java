package com.energyict.protocolimpl.itron.quantum.basepages;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;

/**
 * Base page describing the Offline flag
 *
 * @author sva
 * @since 19/11/2013 10:44
 */
public class OfflineBasePage extends AbstractBasePage {

    public OfflineBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("OfflineBasePage");
        return strBuff.toString();
    }  
    
    protected BasePageDescriptor preparebuild() throws IOException {
        BasePageDescriptor basePageDescriptor = new BasePageDescriptor(0x1D96, 1);  // Apparently the Offline Flag is at register 0x1D96 and not on 0x2112
        basePageDescriptor.setData(new byte[]{(byte) 0xFF});    // Set 0xFF as data to download to the flag
        return basePageDescriptor;
    }
    
    protected void parse(byte[] data) throws IOException {
    }
}
