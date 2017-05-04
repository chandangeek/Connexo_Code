/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Frame.java
 *
 * Created on 13 februari 2007, 17:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.protocol;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.CRCGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author Koen
 */
public class Frame {

    private boolean send;
    private boolean confirm;
    private DSDU dsdu;
    private byte[] data;
    private boolean checkFrame;

    /** Creates a new instance of Frame */
    public Frame() {
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuilder strBuff = new StringBuilder();
        strBuff.append("Frame:\n");
        strBuff.append("   checkFrame=").append(isCheckFrame()).append("\n");
        strBuff.append("   confirm=").append(isConfirm()).append("\n");
        strBuff.append(ProtocolUtils.outputHexString(getData())).append("\n");
        strBuff.append("   dsdu=").append(getDsdu()).append("\n");
        strBuff.append("   send=").append(isSend()).append("\n");
        try {
           strBuff.append("   text=").append(isText()).append("\n");
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return strBuff.toString();
    }

    public boolean isText() throws IOException {
        if (getData()[0] != 0) {
            return true;
        } else {
            return false;
        }

        //throw new IOException("Frame, isText, program flow error!");
    }


    public void init(byte[] data) throws IOException {

        setData(data);
        setCheckFrame(true);

        // length >= 4 ?
        if (getData().length < 4) {
			setCheckFrame(false);
		}
        // is crc valid?
        if (CRCGenerator.calcCCITTCRCReverse(getData()) != 0) {
			setCheckFrame(false);
		}
        // is length valid ?
        if (getData()[0] != (getData().length-4)) {
			setCheckFrame(false);
		}
        // check data+ field
        if (((int)getData()[1]&0xE0) != 0xE0) {
			setCheckFrame(false);
		}
        // check send & confirm field
        if (((getData()[1]&0x0f) != 0x0f) && ((getData()[1]&0x0f) != 0x0c) && ((getData()[1]&0x0f) != 0x03) && ((getData()[1]&0x0f) != 0x00)) {
			setCheckFrame(false);
		}

        if (isCheckFrame()) {
            int temp = (int)data[1]&0xff;
            setSend((temp & 0x0c) != 0);
            setConfirm((temp & 0x03) != 0);
            boolean priority = (temp & 0x10) != 0;
            setDsdu(new DSDU());

            if (getData().length>4) {
				getDsdu().init(ProtocolUtils.getSubArray(getData(),2), priority);
			} else {
				getDsdu().init(priority);
			}
        }

    } // public void init(byte[] data)




    public void init(boolean send, boolean confirm) throws IOException {
        DSDU dsdu = new DSDU();
        dsdu.init(null);
        init(send, confirm, dsdu);
    }
    public void init(boolean send, boolean confirm, DSDU dsdu) throws IOException {
        this.setSend(send);
        this.setConfirm(confirm);
        this.setDsdu(dsdu);
        int dataPlus = 0xE0;
        if (getDsdu().isPriority()) {
			dataPlus |= 0x10;
		}
        if (isSend()) {
			dataPlus |=0x0C;
		}
        if (isConfirm()) {
			dataPlus |=0x03;
		}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(getDsdu().getData()==null?0:getDsdu().getData().length);
        baos.write(dataPlus);
        if (getDsdu().getData()!=null) {
			baos.write(getDsdu().getData());
		}

        int crc = CRCGenerator.calcCCITTCRCReverse(baos.toByteArray());
        baos.write((crc >> 8)&0xff);
        baos.write(crc&0xff);
        setData(baos.toByteArray());
    }

    public boolean isSend() {
        return send;
    }

    private void setSend(boolean send) {
        this.send = send;
    }

    public boolean isConfirm() {
        return confirm;
    }

    private void setConfirm(boolean confirm) {
        this.confirm = confirm;
    }

    public DSDU getDsdu() {
        return dsdu;
    }

    private void setDsdu(DSDU dsdu) {
        this.dsdu = dsdu;
    }

    public byte[] getData() {
        return data;
    }

    private void setData(byte[] data) {
        this.data = data;
    }

    public boolean isCheckFrame() {
        return checkFrame;
    }

    private void setCheckFrame(boolean checkFrame) {
        this.checkFrame = checkFrame;
    }

}
