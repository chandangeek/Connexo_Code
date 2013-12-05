/*
 * AbstractBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class AbstractBasePage {

    private AbstractBasePageFactory basePagesFactory;

    abstract protected BasePageDescriptor preparebuild() throws IOException;
    abstract protected void parse(byte[] data) throws IOException;

    /** Creates a new instance of AbstractBasePage */
    public AbstractBasePage(AbstractBasePageFactory basePagesFactory) {
        this.setBasePagesFactory(basePagesFactory);
    }

    public void invoke() throws IOException {
        BasePageDescriptor basePageDescriptor = preparebuild();
        if (basePageDescriptor.getData()==null) { // upload from meter

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int count=0;
            int length=basePageDescriptor.getLength();
            while(count < basePageDescriptor.getLength()) {
                int startAddress=getBasePagesFactory().getMemStartAddress()+basePageDescriptor.getBaseAddress()+count;
                int size=length<getBasePagesFactory().getProtocolLink().getBlockSize()?length:getBasePagesFactory().getProtocolLink().getBlockSize();
                int endAddress=startAddress+size-1;
                byte[] data = getBasePagesFactory().getProtocolLink().getCommandFactory().getUploadCommand(startAddress,endAddress).getData();
                baos.write(data);
                count+=size;
                length-=size;
            } // while(count < basePageDescriptor.getLength())

            byte[] data = baos.toByteArray();
            if (data != null) {
                parse(data);
            }
        }
        else { // download to the meter
            int startAddress=getBasePagesFactory().getMemStartAddress()+basePageDescriptor.getBaseAddress();
            int endAddress=startAddress+basePageDescriptor.getLength()-1;
            getBasePagesFactory().getProtocolLink().getCommandFactory().downloadCommand(startAddress,endAddress, basePageDescriptor.getData());
        }
    }

    public AbstractBasePageFactory getBasePagesFactory() {
        return basePagesFactory;
    }

    public void setBasePagesFactory(AbstractBasePageFactory basePagesFactory) {
        this.basePagesFactory = basePagesFactory;
    }
}
