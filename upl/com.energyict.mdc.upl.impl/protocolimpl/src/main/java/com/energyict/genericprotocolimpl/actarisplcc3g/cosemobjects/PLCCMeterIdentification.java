package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ObjectIdentification;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.protocol.ProtocolUtils;

public class PLCCMeterIdentification extends AbstractPLCCObject {

    private String identification;

    static public final int MANUF_ACTARIS=03;
    static public final int MANUF_LNG=04;
    static public final int MANUF_ISKRA=27;
    private int constructorCode;

    private int year;

    static public final int TYPE_MONOPHASE=90;
    static public final int TYPE_POLYPHASE=91;
    private int type;

    private int serialnr;

    public PLCCMeterIdentification(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }

    protected ObjectIdentification getId() {
        return new ObjectIdentification("0.0.96.2.0.255", DLMSClassId.DATA.getClassId() );
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PLCCMeterIdentification:\n");
        strBuff.append("   constructorCode="+getConstructorCode()+"\n");
        strBuff.append("   identification="+getIdentification()+"\n");
        strBuff.append("   serialnr="+getSerialnr()+"\n");
        strBuff.append("   type="+getType()+"\n");
        strBuff.append("   year="+getYear()+"\n");
        return strBuff.toString();
    }

    protected void doInvoke() throws IOException {
        AbstractDataType o = AXDRDecoder.decode(getCosemObjectFactory().getData(getId().getObisCode()).getData());
        if (o.isVisibleString()) {
            setIdentification(o.getVisibleString().getStr());
            constructorCode = Integer.parseInt(getIdentification().substring(0,2));
            year = Integer.parseInt(getIdentification().substring(2,4)) + 2000;
            type = Integer.parseInt(getIdentification().substring(4,6));
            serialnr = Integer.parseInt(getIdentification().substring(6));
        }
        else if (o.isOctetString()) {
            byte[] data = o.getOctetString().getOctetStr();
            setIdentification(ParseUtils.buildStringDecimal(ParseUtils.bcd2Long(data, 0,6),12));
            constructorCode = ProtocolUtils.BCD2hex(data[0]);
            year = ProtocolUtils.BCD2hex(data[1]) + 2000;
            type = ProtocolUtils.BCD2hex(data[2]);
            serialnr = (int)ParseUtils.bcd2Long(data, 3,3);
        }
    }

    public int getConstructorCode() {
        return constructorCode;
    }

    private void setConstructorCode(int constructorCode) {
        this.constructorCode = constructorCode;
    }

    public int getType() {
        return type;
    }

    private void setType(int type) {
        this.type = type;
    }

    public int getSerialnr() {
        return serialnr;
    }

    private void setSerialnr(int serialnr) {
        this.serialnr = serialnr;
    }

    public int getYear() {
        return year;
    }

    private void setYear(int year) {
        this.year = year;
    }

    public String getIdentification() {
        return identification;
    }

    private void setIdentification(String identification) {
        this.identification = identification;
    }

    public com.energyict.edf.messages.objects.MeterIdentification toMeterIdentification() throws IOException {
        com.energyict.edf.messages.objects.MeterIdentification meterIdentification = new com.energyict.edf.messages.objects.MeterIdentification();
        byte[] data = new byte[6];
        data[0] = ProtocolUtils.hex2BCD(getConstructorCode());
        data[1] = ProtocolUtils.hex2BCD(getYear()-2000);
        data[2] = ProtocolUtils.hex2BCD(getType());
        data[3] = ProtocolUtils.hex2BCD(getSerialnr()/10000);
        data[4] = ProtocolUtils.hex2BCD((getSerialnr()%10000)/100);
        data[5] = ProtocolUtils.hex2BCD(getSerialnr()%100);
        meterIdentification.setId(data);
        return meterIdentification;
    }


}