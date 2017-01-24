package com.energyict.protocolimplv2.ace4000.requests.tracking;

/**
 * Indicates the type of a request.
 * This is used for tracking purposes
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/01/13
 * Time: 11:09
 * Author: khe
 */
public enum RequestType {
    Config,
    MBusCurrentRegister,
    MBusBillingRegister,
    LoadProfile,
    Events,
    InstantRegisters,
    CurrentRegisters,
    Firmware,
    Contactor,
    BillingRegisters,
    FirmwareVersion
}