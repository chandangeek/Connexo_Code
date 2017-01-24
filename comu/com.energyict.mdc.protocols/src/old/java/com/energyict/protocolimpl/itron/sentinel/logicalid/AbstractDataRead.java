/*
 * AbstractDataRead.java
 *
 * Created on 2 november 2006, 16:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.logicalid;

import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class AbstractDataRead {


    abstract protected void parse(byte[] data) throws IOException;

    private DataReadFactory dataReadFactory;

    private DataReadDescriptor dataReadDescriptor;

    /** Creates a new instance of AbstractDataRead */
    public AbstractDataRead(DataReadFactory dataReadFactory) {
        this.dataReadFactory=dataReadFactory;
    }

    protected void prepareBuild() throws IOException {
    }

    public void invoke() throws IOException {
        prepareBuild();
        dataReadFactory.getManufacturerTableFactory().getWriteOnlyTable2049(dataReadDescriptor);
        parse (dataReadFactory.getManufacturerTableFactory().getReadOnlyTable2050().getData());
    }

    public DataReadFactory getDataReadFactory() {
        return dataReadFactory;
    }

    public void setDataReadFactory(DataReadFactory dataReadFactory) {
        this.dataReadFactory = dataReadFactory;
    }

    public DataReadDescriptor getDataReadDescriptor() {
        return dataReadDescriptor;
    }

    public void setDataReadDescriptor(DataReadDescriptor dataReadDescriptor) {
        this.dataReadDescriptor = dataReadDescriptor;
    }

}
