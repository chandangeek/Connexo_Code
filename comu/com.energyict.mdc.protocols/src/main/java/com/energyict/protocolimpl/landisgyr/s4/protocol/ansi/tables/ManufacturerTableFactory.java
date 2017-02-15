/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ManufacturerTableFactory.java
 *
 * Created on 18 oktober 2005, 11:59
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.tables;

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
    FeatureParameters featureParameters=null;
    MeterStatus meterStatus=null;
    S4Configuration s4Configuration=null;
    MeterFactors meterFactors=null;
    InterfaceStatus interfaceStatus=null;
    ServiceTypeTable serviceTypeTable=null;

    public ServiceTypeTable getServiceTypeTable() throws IOException {
        if (serviceTypeTable==null) {
            serviceTypeTable = new ServiceTypeTable(this);
            serviceTypeTable.build();
        }
        return serviceTypeTable;
    }

    public InterfaceStatus getInterfaceStatus() throws IOException {
        if (interfaceStatus==null) {
            interfaceStatus = new InterfaceStatus(this);
            interfaceStatus.build();
        }
        return interfaceStatus;
    }

    public MeterFactors getMeterFactors() throws IOException {
        if (meterFactors==null) {
            meterFactors = new MeterFactors(this);
            meterFactors.build();
        }
        return meterFactors;
    }

    public S4Configuration getS4Configuration() throws IOException {
        if (s4Configuration==null) {
            s4Configuration = new S4Configuration(this);
            s4Configuration.build();
        }
        return s4Configuration;
    }

    public MeterStatus getMeterStatus() throws IOException {
        if (meterStatus==null) {
            meterStatus = new MeterStatus(this);
            meterStatus.build();
        }
        return meterStatus;
    }

    public FeatureParameters getFeatureParameters() throws IOException {
        if (featureParameters==null) {
            featureParameters = new FeatureParameters(this);
            featureParameters.build();
        }
        return featureParameters;
    }

    /** Creates a new instance of TableFactory */
    public ManufacturerTableFactory(C12ProtocolLink c12ProtocolLink) {
        this.c12ProtocolLink = c12ProtocolLink;
    }

    public C12ProtocolLink getC12ProtocolLink() {
        return c12ProtocolLink;
    }



}
