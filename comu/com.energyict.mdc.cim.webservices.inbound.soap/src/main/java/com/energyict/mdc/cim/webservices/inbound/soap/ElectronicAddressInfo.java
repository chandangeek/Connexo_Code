/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap;

import ch.iec.tc57._2011.meterconfig.ElectronicAddress;

public class ElectronicAddressInfo {
    private String email1;
    private String email2;
    private String lan;
    private String mac;
    private String password;
    private String radio;
    private String userID;
    private String web;

    public ElectronicAddressInfo(ElectronicAddress electronicAddress){
        if (electronicAddress!= null) {
            setEmail1(electronicAddress.getEmail1());
            setEmail2(electronicAddress.getEmail2());
            setLan(electronicAddress.getLan());
            setMac(electronicAddress.getMac());
            setPassword(electronicAddress.getPassword());
            setRadio(electronicAddress.getRadio());
            setUserID(electronicAddress.getUserID());
            setWeb(electronicAddress.getWeb());
        }
    }

    public String getEmail1() {
        return email1;
    }

    public void setEmail1(String email1) {
        this.email1 = email1;
    }

    public String getEmail2() {
        return email2;
    }

    public void setEmail2(String email2) {
        this.email2 = email2;
    }

    public String getLan() {
        return lan;
    }

    public void setLan(String lan) {
        this.lan = lan;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRadio() {
        return radio;
    }

    public void setRadio(String radio) {
        this.radio = radio;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getWeb() {
        return web;
    }

    public void setWeb(String web) {
        this.web = web;
    }
}
