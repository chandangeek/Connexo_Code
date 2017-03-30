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
public class LastBillingPeriodCumulativeDemandDataRead extends AbstractDataRead {

    private RegisterData[] registerCumulativeTotalDatas;
    private RegisterData[][] registerCumulativeRateDatas;

    /** Creates a new instance of CurrentCumulativeDemandDataRead */
    public LastBillingPeriodCumulativeDemandDataRead(DataReadFactory dataReadFactory) {
        super(dataReadFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LastBillingPeriodCumulativeDemandDataRead:\n");
        for (int i=0;i<getRegisterCumulativeTotalDatas().length;i++) {
            strBuff.append("       registerTotalDatas["+i+"]="+getRegisterCumulativeTotalDatas()[i]+"\n");
        }
        for (int rate=0;rate<getRegisterCumulativeRateDatas().length;rate++) {
            for (int i=0;i<getRegisterCumulativeRateDatas()[rate].length;i++) {
                strBuff.append("       registerRateDatas[rate"+rate+"]["+i+"]="+getRegisterCumulativeRateDatas()[rate][i]+"\n");
            }
        }
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {

        int offset=0;
        int dataOrder = getDataReadFactory().getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setRegisterCumulativeTotalDatas(new RegisterData[getDataReadFactory().getCapabilitiesDataRead().getNumberOfCumulativeDemands()]);
        for (int i=0;i<getRegisterCumulativeTotalDatas().length;i++) {
            RegisterData rd = new RegisterData(LogicalIDFactory.findLogicalId(getDataReadFactory().getQuantityIdentificationDataRead().getCumDemandLids()[i]),
                                               new BigDecimal(Double.longBitsToDouble(C12ParseUtils.getLong(data, offset, 8, dataOrder))));
            getRegisterCumulativeTotalDatas()[i]=rd;
            offset+=8;
        }

        setRegisterCumulativeRateDatas(new RegisterData[getDataReadFactory().getCapabilitiesDataRead().getNumberOfTOURates()][getDataReadFactory().getCapabilitiesDataRead().getNumberOfCumulativeDemands()]);
        for (int rate=0;rate<getDataReadFactory().getCapabilitiesDataRead().getNumberOfTOURates();rate++) {
            //registerRateDatas[rate] = new RegisterData[getDataReadFactory().getCapabilitiesDataRead().getNumberOfEnergies()];
            for (int i=0;i<getRegisterCumulativeRateDatas()[rate].length;i++) {
                RegisterData rd = new RegisterData(LogicalIDFactory.findLogicalId(getDataReadFactory().getQuantityIdentificationDataRead().getCumDemandLids()[i]),
                                                   new BigDecimal(Double.longBitsToDouble(C12ParseUtils.getLong(data, offset, 8, dataOrder))),
                                                   rate);
                getRegisterCumulativeRateDatas()[rate][i]=rd;
                offset+=8;
            }
        }
    }

    protected void prepareBuild() throws IOException {

        long[] lids = new long[]{LogicalIDFactory.findLogicalId("LAST_BP_ALL_SEC_CUMS_TOTAL").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_BP_ALL_SEC_CUMS_RATE_A").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_BP_ALL_SEC_CUMS_RATE_B").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_BP_ALL_SEC_CUMS_RATE_C").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_BP_ALL_SEC_CUMS_RATE_D").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_BP_ALL_SEC_CUMS_RATE_E").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_BP_ALL_SEC_CUMS_RATE_F").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_BP_ALL_SEC_CUMS_RATE_G").getId()};

        setDataReadDescriptor(new DataReadDescriptor(0x00, getDataReadFactory().getCapabilitiesDataRead().getNumberOfTOURates()+1, lids));

    } // protected void prepareBuild() throws IOException

    public RegisterData[] getRegisterCumulativeTotalDatas() {
        return registerCumulativeTotalDatas;
    }

    public void setRegisterCumulativeTotalDatas(RegisterData[] registerCumulativeTotalDatas) {
        this.registerCumulativeTotalDatas = registerCumulativeTotalDatas;
    }

    public RegisterData[][] getRegisterCumulativeRateDatas() {
        return registerCumulativeRateDatas;
    }

    public void setRegisterCumulativeRateDatas(RegisterData[][] registerCumulativeRateDatas) {
        this.registerCumulativeRateDatas = registerCumulativeRateDatas;
    }

} // public class ConstantsDataRead extends AbstractDataRead
