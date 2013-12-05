/*
 * ManufacturerTableFactory.java
 *
 * Created on 18 oktober 2005, 11:59
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.tables;

import com.energyict.protocolimpl.ansi.c12.C12ProtocolLink;
import com.energyict.protocolimpl.ansi.c12.tables.TableFactory;
import com.energyict.protocolimpl.itron.sentinel.Sentinel;
import com.energyict.protocolimpl.itron.sentinel.logicalid.DataReadDescriptor;
import com.energyict.protocolimpl.itron.sentinel.logicalid.DataReadFactory;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class ManufacturerTableFactory extends TableFactory {

    private C12ProtocolLink c12ProtocolLink;
    Sentinel sentinel;

    /** Creates a new instance of TableFactory */
    public ManufacturerTableFactory(Sentinel sentinel) {
        this.sentinel = sentinel;

    }

    // cached tables

    public WriteOnlyTable2049 getWriteOnlyTable2049(DataReadDescriptor dataReadDescriptor) throws IOException {
        WriteOnlyTable2049 w = new WriteOnlyTable2049(this);
        w.setDataReadDescriptor(dataReadDescriptor);
        w.transfer();
        return w;
    }

    public ReadOnlyTable2050 getReadOnlyTable2050() throws IOException {
        ReadOnlyTable2050 r = new ReadOnlyTable2050(this);
        r.build();
        return r;
    }

    public LoadProfileData getLoadProfileDataHeaderOnly(int blockId) throws IOException {
        return getLoadProfileData(blockId, 1, true);
    }
    public LoadProfileData getLoadProfileData(int blockId,int blocks) throws IOException {
        return getLoadProfileData(blockId, blocks, false);
    }
    private LoadProfileData getLoadProfileData(int blockId,int blocks, boolean headerOnly) throws IOException {
        LoadProfileData lpd = new LoadProfileData(this);
        lpd.setBlockId(blockId);
        lpd.setBlocks(blocks);
        lpd.setHeaderOnly(headerOnly);
        lpd.build();
        return lpd;
    }


    public C12ProtocolLink getC12ProtocolLink() {
        return (C12ProtocolLink)sentinel;
    }

    public DataReadFactory getDataReadFactory() {
        return sentinel.getDataReadFactory();
    }


}
