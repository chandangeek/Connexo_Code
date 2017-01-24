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
public class CurrentDemandDataRead extends AbstractDataRead {

    private RegisterData[] registerDemandTotalDatas;
    private RegisterData[][] registerDemandRateDatas;

    /** Creates a new instance of ConstantsDataRead */
    public CurrentDemandDataRead(DataReadFactory dataReadFactory) {
        super(dataReadFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CurrentDemandDataRead:\n");
        for (int rate=0;rate<getRegisterDemandRateDatas().length;rate++) {
            for (int i=0;i<getRegisterDemandRateDatas()[rate].length;i++) {
                strBuff.append("       registerDemandRateDatas["+rate+"]["+i+"]="+getRegisterDemandRateDatas()[rate][i]+"\n");
            }
        }

        for (int i=0;i<getRegisterDemandTotalDatas().length;i++) {
            strBuff.append("       registerDemandTotalDatas["+i+"]="+getRegisterDemandTotalDatas()[i]+"\n");
        }
        return strBuff.toString();
    }


    protected void parse(byte[] data) throws IOException {

        int offset=0;
        int dataOrder = getDataReadFactory().getManufacturerTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setRegisterDemandTotalDatas(new RegisterData[getDataReadFactory().getCapabilitiesDataRead().getNumberOfDemands()+getDataReadFactory().getCapabilitiesDataRead().getPFAvgBillingPeriodAvailable()]);
        for (int i=0;i<getRegisterDemandTotalDatas().length;i++) {
            RegisterData rd = new RegisterData(LogicalIDFactory.findLogicalId(getDataReadFactory().getQuantityIdentificationDataRead().getDemandLids()[i]),
                                               new BigDecimal((double)Float.intBitsToFloat(C12ParseUtils.getInt(data, offset, 4, dataOrder))));
            getRegisterDemandTotalDatas()[i]=rd;
            offset+=4;
        }

        setRegisterDemandRateDatas(new RegisterData[getDataReadFactory().getCapabilitiesDataRead().getNumberOfTOURates()][getDataReadFactory().getCapabilitiesDataRead().getNumberOfDemands()]);
        for (int rate=0;rate<getDataReadFactory().getCapabilitiesDataRead().getNumberOfTOURates();rate++) {
            for (int i=0;i<getRegisterDemandRateDatas()[rate].length;i++) {
                RegisterData rd = new RegisterData(LogicalIDFactory.findLogicalId(getDataReadFactory().getQuantityIdentificationDataRead().getDemandTOOLids()[i]),
                                                   new BigDecimal((double)Float.intBitsToFloat(C12ParseUtils.getInt(data, offset, 4, dataOrder))),
                                                   rate);
                getRegisterDemandRateDatas()[rate][i]=rd;
                offset+=4;
            }
        }



    }

    protected void prepareBuild() throws IOException {

        long[] lids = new long[]{LogicalIDFactory.findLogicalId("ALL_SEC_DEMANDS_TOTAL").getId(),
                                 LogicalIDFactory.findLogicalId("ALL_SEC_DEMANDS_RATE_A").getId(),
                                 LogicalIDFactory.findLogicalId("ALL_SEC_DEMANDS_RATE_B").getId(),
                                 LogicalIDFactory.findLogicalId("ALL_SEC_DEMANDS_RATE_C").getId(),
                                 LogicalIDFactory.findLogicalId("ALL_SEC_DEMANDS_RATE_D").getId(),
                                 LogicalIDFactory.findLogicalId("ALL_SEC_DEMANDS_RATE_E").getId(),
                                 LogicalIDFactory.findLogicalId("ALL_SEC_DEMANDS_RATE_F").getId(),
                                 LogicalIDFactory.findLogicalId("ALL_SEC_DEMANDS_RATE_G").getId()};

        setDataReadDescriptor(new DataReadDescriptor(0x00, getDataReadFactory().getCapabilitiesDataRead().getNumberOfTOURates()+1, lids));

    } // protected void prepareBuild() throws IOException

    public RegisterData[] getRegisterDemandTotalDatas() {
        return registerDemandTotalDatas;
    }

    public void setRegisterDemandTotalDatas(RegisterData[] registerDemandTotalDatas) {
        this.registerDemandTotalDatas = registerDemandTotalDatas;
    }

    public RegisterData[][] getRegisterDemandRateDatas() {
        return registerDemandRateDatas;
    }

    public void setRegisterDemandRateDatas(RegisterData[][] registerDemandRateDatas) {
        this.registerDemandRateDatas = registerDemandRateDatas;
    }

} // public class ConstantsDataRead extends AbstractDataRead
