/*
 * TableTemplate.java
 *
 * Created July 2006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.tables;

import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.ActualSourcesLimitingTable;
import com.energyict.protocolimpl.ansi.c12.tables.ConfigurationTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class ServiceTypeTable extends AbstractTable {

    private int nrOfUserTypes;
    private int formNumber;
    private TypeDescription typeDescription;
    private int index;
    private ServiceTypeEntry[] serviceTypeEntries;
    private ExpectedServiceType expectedServiceTypeIndex;
    private CalConstants calConstants;

    /** Creates a new instance of TableTemplate */
    public ServiceTypeTable(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(2,true));
    }


    public BigDecimal getCurrentMultiplier() throws IOException {
        return CurrentClass.findCurrentClass(getTypeDescription().getClassType()).getMultiplier().multiply(BigDecimal.valueOf(3600/getTableFactory().getC12ProtocolLink().getProfileInterval()));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ServiceTypeTable:\n");
        strBuff.append("   calConstants="+getCalConstants()+"\n");
        strBuff.append("   expectedServiceTypeIndex="+getExpectedServiceTypeIndex()+"\n");
        strBuff.append("   formNumber="+getFormNumber()+"\n");
        strBuff.append("   index="+getIndex()+"\n");
        strBuff.append("   nrOfUserTypes="+getNrOfUserTypes()+"\n");
        for (int i=0;i<getServiceTypeEntries().length;i++) {
            strBuff.append("       serviceTypeEntries["+i+"]="+getServiceTypeEntries()[i]+"\n");
        }
        strBuff.append("   typeDescription="+getTypeDescription()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {
        ConfigurationTable cfgt = getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualSourcesLimitingTable aslt = getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualSourcesLimitingTable();
        int offset=0;
        setNrOfUserTypes((int)data[offset++]&0xFF);
        setFormNumber((int)data[offset++]&0xFF);
        setTypeDescription(new TypeDescription(data, offset, getManufacturerTableFactory()));
        offset+=TypeDescription.getSize(getManufacturerTableFactory());
        setIndex((int)data[offset++]&0xFF);
        if (getManufacturerTableFactory().getS4Configuration().getS4ConfigMask().isSinglePhaseServiceExpected()) {
            setServiceTypeEntries(new ServiceTypeEntry[27]);
            for (int i=0;i<getServiceTypeEntries().length;i++) {
                getServiceTypeEntries()[i] = new ServiceTypeEntry(data, offset, getManufacturerTableFactory());
                offset+=ServiceTypeEntry.getSize(getManufacturerTableFactory());
            }
            setExpectedServiceTypeIndex(new ExpectedServiceType(data, offset, getManufacturerTableFactory()));
            offset+=ExpectedServiceType.getSize(getManufacturerTableFactory());
            setCalConstants(new CalConstants(data, offset, getManufacturerTableFactory()));
            offset+=CalConstants.getSize(getManufacturerTableFactory());
        }
        else {
            setServiceTypeEntries(new ServiceTypeEntry[30]);
            for (int i=0;i<getServiceTypeEntries().length;i++) {
                getServiceTypeEntries()[i] = new ServiceTypeEntry(data, offset, getManufacturerTableFactory());
                offset+=ServiceTypeEntry.getSize(getManufacturerTableFactory());
            }
        }

    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

//    protected void prepareBuild() throws IOException {
//        // override to provide extra functionality...
//        PartialReadInfo partialReadInfo = new PartialReadInfo(0,84);
//        setPartialReadInfo(partialReadInfo);
//    }

    public int getNrOfUserTypes() {
        return nrOfUserTypes;
    }

    public void setNrOfUserTypes(int nrOfUserTypes) {
        this.nrOfUserTypes = nrOfUserTypes;
    }

    public int getFormNumber() {
        return formNumber;
    }

    public void setFormNumber(int formNumber) {
        this.formNumber = formNumber;
    }

    public TypeDescription getTypeDescription() {
        return typeDescription;
    }

    public void setTypeDescription(TypeDescription typeDescription) {
        this.typeDescription = typeDescription;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public ServiceTypeEntry[] getServiceTypeEntries() {
        return serviceTypeEntries;
    }

    public void setServiceTypeEntries(ServiceTypeEntry[] serviceTypeEntries) {
        this.serviceTypeEntries = serviceTypeEntries;
    }

    public ExpectedServiceType getExpectedServiceTypeIndex() {
        return expectedServiceTypeIndex;
    }

    public void setExpectedServiceTypeIndex(ExpectedServiceType expectedServiceTypeIndex) {
        this.expectedServiceTypeIndex = expectedServiceTypeIndex;
    }

    public CalConstants getCalConstants() {
        return calConstants;
    }

    public void setCalConstants(CalConstants calConstants) {
        this.calConstants = calConstants;
    }


}
