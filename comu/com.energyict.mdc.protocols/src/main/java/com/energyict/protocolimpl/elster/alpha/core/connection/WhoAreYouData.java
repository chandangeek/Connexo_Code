/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * WhoAreYouData.java
 *
 * Created on 11 juli 2005, 11:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.core.connection;

import com.energyict.protocols.util.ProtocolUtils;

/**
 *
 * @author Koen
 */
public class WhoAreYouData {

    private int pcode;
    private int pseries;
    private int xuom;
    private int syserr;
    private int events;
    private int modstat;
    private byte[] passwordEncryptionKey;


    /** Creates a new instance of WhoAreYouData */
    public WhoAreYouData(byte[] data) {
        parse(data);
    }

    private void parse(byte[] data) {
        setPcode((int)data[0]&0xff);
        setPseries((int)data[1]&0xff);
        setXuom(ProtocolUtils.getShort(data, 2));
        setSyserr(ProtocolUtils.getShort(data, 4));
        setEvents((int)data[6]&0xff);
        setModstat((int)data[7]&0xff);
        setPasswordEncryptionKey(ProtocolUtils.getSubArray(data,8));
    }

    public int getPcode() {
        return pcode;
    }

    public void setPcode(int pcode) {
        this.pcode = pcode;
    }

    public int getPseries() {
        return pseries;
    }

    public void setPseries(int pseries) {
        this.pseries = pseries;
    }

    public int getXuom() {
        return xuom;
    }

    public void setXuom(int xuom) {
        this.xuom = xuom;
    }

    public int getSyserr() {
        return syserr;
    }

    public void setSyserr(int syserr) {
        this.syserr = syserr;
    }

    public int getEvents() {
        return events;
    }

    public void setEvents(int events) {
        this.events = events;
    }

    public int getModstat() {
        return modstat;
    }

    public void setModstat(int modstat) {
        this.modstat = modstat;
    }

    public byte[] getPasswordEncryptionKey() {
        return passwordEncryptionKey;
    }

    public void setPasswordEncryptionKey(byte[] passwordEncryptionKey) {
        this.passwordEncryptionKey = passwordEncryptionKey;
    }

}
