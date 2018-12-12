package com.elster.us.protocolimpl.landisgyr.quad4;

class RegisterAddress {

    Firmware firmware;
    TableAddress tableAddress;
    TypeChannelSelectRcd typeChannelSelectRcd;

    RegisterAddress(Quad4 quad4, int offset, TypeChannelSelectRcd tcs) {
        this.firmware = quad4.getFirmware();
        this.tableAddress = new TableAddress(quad4, 15, offset);
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