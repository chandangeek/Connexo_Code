/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ConstantsDataRead.java
 *
 * Created on 2 november 2006, 16:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.logicalid;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;
import java.math.BigDecimal;


/**
 *
 * @author Koen
 */
public class LastSelfReadDemandTimeOfOccurenceDataRead extends AbstractDataRead {

    private RegisterData[] registerDemandTOOTotalDatas;
    private RegisterData[][] registerDemandTOORateDatas;

    /** Creates a new instance of ConstantsDataRead */
    public LastSelfReadDemandTimeOfOccurenceDataRead(DataReadFactory dataReadFactory) {
        super(dataReadFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LastSelfReadDemandTimeOfOccurenceDataRead:\n");
        for (int rate=0;rate<getRegisterDemandTOORateDatas().length;rate++) {
            for (int i=0;i<getRegisterDemandTOORateDatas()[rate].length;i++) {
                strBuff.append("       registerDemandTOORateDatas["+rate+"]["+i+"]="+getRegisterDemandTOORateDatas()[rate][i]+"\n");
            }
        }

        for (int i=0;i<getRegisterDemandTOOTotalDatas().length;i++) {
            strBuff.append("       registerDemandTOOTotalDatas["+i+"]="+getRegisterDemandTOOTotalDatas()[i]+"\n");
        }
        return strBuff.toString();
    }


    protected void parse(byte[] data) throws IOException {

        int offset=0;
        int dataOrder = getDataReadFactory().getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setRegisterDemandTOOTotalDatas(new RegisterData[getDataReadFactory().getCapabilitiesDataRead().getNumberOfDemands()]);
        for (int i=0;i<getRegisterDemandTOOTotalDatas().length;i++) {
            RegisterData rd = new RegisterData(LogicalIDFactory.findLogicalId(getDataReadFactory().getQuantityIdentificationDataRead().getDemandLids()[i]),
                                               new BigDecimal((double)Float.intBitsToFloat(C12ParseUtils.getInt(data, offset, 4, dataOrder))));
            getRegisterDemandTOOTotalDatas()[i]=rd;
            offset+=4;
        }

        setRegisterDemandTOORateDatas(new RegisterData[getDataReadFactory().getCapabilitiesDataRead().getNumberOfTOURates()][getDataReadFactory().getCapabilitiesDataRead().getNumberOfDemands()]);
        for (int rate=0;rate<getDataReadFactory().getCapabilitiesDataRead().getNumberOfTOURates();rate++) {
            for (int i=0;i<getRegisterDemandTOORateDatas()[rate].length;i++) {
                RegisterData rd = new RegisterData(LogicalIDFactory.findLogicalId(getDataReadFactory().getQuantityIdentificationDataRead().getDemandTOOLids()[i]),
                                                   new BigDecimal((double)Float.intBitsToFloat(C12ParseUtils.getInt(data, offset, 4, dataOrder))),
                                                   rate);
                getRegisterDemandTOORateDatas()[rate][i]=rd;
                offset+=4;
            }
        }



    }

    protected void prepareBuild() throws IOException {

        long[] lids = new long[]{LogicalIDFactory.findLogicalId("LAST_SR_ALL_DEMAND_TOO_TOTAL").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_SR_ALL_DEMAND_TOO_RATE_A").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_SR_ALL_DEMAND_TOO_RATE_B").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_SR_ALL_DEMAND_TOO_RATE_C").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_SR_ALL_DEMAND_TOO_RATE_D").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_SR_ALL_DEMAND_TOO_RATE_E").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_SR_ALL_DEMAND_TOO_RATE_F").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_SR_ALL_DEMAND_TOO_RATE_G").getId()};

        setDataReadDescriptor(new DataReadDescriptor(0x00, getDataReadFactory().getCapabilitiesDataRead().getNumberOfTOURates()+1, lids));

    } // protected void prepareBuild() throws IOException

    public RegisterData[] getRegisterDemandTOOTotalDatas() {
        return registerDemandTOOTotalDatas;
    }

    public void setRegisterDemandTOOTotalDatas(RegisterData[] registerDemandTOOTotalDatas) {
        this.registerDemandTOOTotalDatas = registerDemandTOOTotalDatas;
    }

    public RegisterData[][] getRegisterDemandTOORateDatas() {
        return registerDemandTOORateDatas;
    }

    public void setRegisterDemandTOORateDatas(RegisterData[][] registerDemandTOORateDatas) {
        this.registerDemandTOORateDatas = registerDemandTOORateDatas;
    }

} // public class ConstantsDataRead extends AbstractDataRead
