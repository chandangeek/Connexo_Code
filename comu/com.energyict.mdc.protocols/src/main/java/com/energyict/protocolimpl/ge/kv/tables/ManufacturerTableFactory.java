/*
 * ManufacturerTableFactory.java
 *
 * Created on 18 oktober 2005, 11:59
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ge.kv.tables;

import com.energyict.protocolimpl.ansi.c12.C12ProtocolLink;
import com.energyict.protocolimpl.ansi.c12.tables.TableFactory;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class ManufacturerTableFactory extends TableFactory {

    private C12ProtocolLink c12ProtocolLink;

    // cached tables
    private GEDeviceTable geDeviceTable=null;
    private MeterProgramConstants1 meterProgramConstants1=null;
    private MeterProgramConstants2 meterProgramConstants2=null;
    private DisplayConfigurationTable displayConfigurationTable=null;
    private ScaleFactorTable scaleFactorTable=null;
    private ElectricalServiceConfiguration electricalServiceConfiguration=null;
    private ElectricalServiceStatus electricalServiceStatus=null;

    /** Creates a new instance of TableFactory */
    public ManufacturerTableFactory(C12ProtocolLink c12ProtocolLink) {
        this.c12ProtocolLink = c12ProtocolLink;
    }

    public C12ProtocolLink getC12ProtocolLink() {
        return c12ProtocolLink;
    }

    public int getTestValue() {
        return 99;
    }

    public GEDeviceTable getGEDeviceTable() throws IOException {
        if (geDeviceTable==null) {
            geDeviceTable = new GEDeviceTable(this);
            geDeviceTable.build();
        }
        return geDeviceTable;
    }

    public MeterProgramConstants1 getMeterProgramConstants1() throws IOException {
        if (meterProgramConstants1==null) {
            meterProgramConstants1 = new MeterProgramConstants1(this);
            meterProgramConstants1.build();
        }
        return meterProgramConstants1;
    }

    public MeterProgramConstants2 getMeterProgramConstants2() throws IOException {
        if (meterProgramConstants2==null) {
            meterProgramConstants2 = new MeterProgramConstants2(this);
            meterProgramConstants2.build();
        }
        return meterProgramConstants2;
    }

    public DisplayConfigurationTable getDisplayConfigurationTable() throws IOException {
        if (displayConfigurationTable==null) {
            displayConfigurationTable = new DisplayConfigurationTable(this);
            displayConfigurationTable.build();
        }
        return displayConfigurationTable;
    }

    public ScaleFactorTable getScaleFactorTable() throws IOException {
        if (scaleFactorTable==null) {
            scaleFactorTable = new ScaleFactorTable(this);
            scaleFactorTable.build();
        }
        return scaleFactorTable;
    }

    public ElectricalServiceConfiguration getElectricalServiceConfiguration() throws IOException {
        if (electricalServiceConfiguration==null) {
            electricalServiceConfiguration = new ElectricalServiceConfiguration(this);
            electricalServiceConfiguration.build();
        }
        return electricalServiceConfiguration;
    }

    public ElectricalServiceStatus getElectricalServiceStatus() throws IOException {
        if (electricalServiceStatus==null) {
            electricalServiceStatus = new ElectricalServiceStatus(this);
            electricalServiceStatus.build();
        }
        return electricalServiceStatus;
    }

}
