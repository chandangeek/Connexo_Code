/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

class RegisterAddress {

    Firmware firmware;
    TableAddress tableAddress;
    TypeChannelSelectRcd typeChannelSelectRcd;

    RegisterAddress(MaxSys maxSys, int offset, TypeChannelSelectRcd tcs) {
        this.firmware = maxSys.getFirmware();
        this.tableAddress = new TableAddress(maxSys, 15, offset);
        this.typeChannelSelectRcd = tcs;
    }

    Firmware getFirmware() {
        return firmware;
    }

    TableAddress getTableAddress() {
        return tableAddress;
    }

    TypeChannelSelectRcd getTypeChannelSelectRcd() {
        return typeChannelSelectRcd;
    }

    public String toString() {
        return "[ " + tableAddress + " " + typeChannelSelectRcd + " ]";
    }

}