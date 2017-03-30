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
public class LastBillingPeriodEnergyDataRead extends AbstractDataRead {

    private RegisterData[] registerTotalDatas;
    private RegisterData[][] registerRateDatas;

    /** Creates a new instance of ConstantsDataRead */
    public LastBillingPeriodEnergyDataRead(DataReadFactory dataReadFactory) {
        super(dataReadFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LastBillingPeriodEnergyDataRead:\n");
        for (int i=0;i<getRegisterTotalDatas().length;i++) {
            strBuff.append("       registerTotalDatas["+i+"]="+getRegisterTotalDatas()[i]+"\n");
        }
        for (int rate=0;rate<getRegisterRateDatas().length;rate++) {
            for (int i=0;i<getRegisterRateDatas()[rate].length;i++) {
                strBuff.append("       registerRateDatas[rate"+rate+"]["+i+"]="+getRegisterRateDatas()[rate][i]+"\n");
            }
        }
        return strBuff.toString();
    }


    protected void parse(byte[] data) throws IOException {

        int offset=0;
        int dataOrder = getDataReadFactory().getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setRegisterTotalDatas(new RegisterData[getDataReadFactory().getCapabilitiesDataRead().getNumberOfEnergies()]);
        for (int i=0;i<getRegisterTotalDatas().length;i++) {
            RegisterData rd = new RegisterData(LogicalIDFactory.findLogicalId(getDataReadFactory().getQuantityIdentificationDataRead().getEnergyLids()[i]),
                                               new BigDecimal(Double.longBitsToDouble(C12ParseUtils.getLong(data, offset, 8, dataOrder))));
            getRegisterTotalDatas()[i]=rd;
            offset+=8;
        }

        setRegisterRateDatas(new RegisterData[getDataReadFactory().getCapabilitiesDataRead().getNumberOfTOURates()][getDataReadFactory().getCapabilitiesDataRead().getNumberOfEnergies()]);
        for (int rate=0;rate<getDataReadFactory().getCapabilitiesDataRead().getNumberOfTOURates();rate++) {
            //registerRateDatas[rate] = new RegisterData[getDataReadFactory().getCapabilitiesDataRead().getNumberOfEnergies()];
            for (int i=0;i<getRegisterRateDatas()[rate].length;i++) {
                RegisterData rd = new RegisterData(LogicalIDFactory.findLogicalId(getDataReadFactory().getQuantityIdentificationDataRead().getEnergyLids()[i]),
                                                   new BigDecimal(Double.longBitsToDouble(C12ParseUtils.getLong(data, offset, 8, dataOrder))),
                                                   rate);
                getRegisterRateDatas()[rate][i]=rd;
                offset+=8;
            }
        }
    }

    protected void prepareBuild() throws IOException {

        long[] lids = new long[]{LogicalIDFactory.findLogicalId("LAST_BP_ALL_SEC_ENERGIES_TOTAL").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_BP_ALL_SEC_ENERGIES_RATE_A").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_BP_ALL_SEC_ENERGIES_RATE_B").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_BP_ALL_SEC_ENERGIES_RATE_C").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_BP_ALL_SEC_ENERGIES_RATE_D").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_BP_ALL_SEC_ENERGIES_RATE_E").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_BP_ALL_SEC_ENERGIES_RATE_F").getId(),
                                 LogicalIDFactory.findLogicalId("LAST_BP_ALL_SEC_ENERGIES_RATE_G").getId()};

        setDataReadDescriptor(new DataReadDescriptor(0x00, getDataReadFactory().getCapabilitiesDataRead().getNumberOfTOURates()+1, lids));

    } // protected void prepareBuild() throws IOException

    public RegisterData[] getRegisterTotalDatas() {
        return registerTotalDatas;
    }

    public void setRegisterTotalDatas(RegisterData[] registerTotalDatas) {
        this.registerTotalDatas = registerTotalDatas;
    }

    public RegisterData[][] getRegisterRateDatas() {
        return registerRateDatas;
    }

    public void setRegisterRateDatas(RegisterData[][] registerRateDatas) {
        this.registerRateDatas = registerRateDatas;
    }

} // public class ConstantsDataRead extends AbstractDataRead
