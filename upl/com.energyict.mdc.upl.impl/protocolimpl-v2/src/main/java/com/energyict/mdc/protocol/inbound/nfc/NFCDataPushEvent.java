/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.inbound.nfc;


import com.energyict.protocol.MeterEvent;

import java.util.Date;
import java.util.Optional;

/**
 * Meter events supported for the NFC DataPush
 */
public class NFCDataPushEvent {

    public static Optional<MeterEvent> parseEventCode(Date date, int meterEventCode){
        switch(meterEventCode) {
            case 0x0000810D: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Combined credit below low credit threshold (prepayment mode)"));
            case 0x00008119: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Emergency credit has become available (prepayment mode)"));
            case 0x00008157: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Supply interrupted"));
            case 0x00008168: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Supply disabled then armed - activate emergency credit triggered"));
            case 0x000081AA: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Emergency credit exhausted"));
            case 0x000081AB: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Emergency credit activated"));
            case 0x000081B9: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Meter cover closed"));
            case 0x000081BC: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Strong magnetic field removed"));
            case 0x000081BD: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Supply connect failure (valve or load switch)"));
            case 0x000081C0: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Supply disconnect failure (valve or load switch)"));
            case 0x000081C1: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Terminal cover closed"));
            case 0x00008F01: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Active power import above load limit threshold"));
            case 0x00008F0F: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Credit below disablement threshold (prepayment mode)"));
            case 0x00008F32: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Supply armed"));
            case 0x00008F33: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Supply disabled then armed - load limit triggered"));
            case 0x00008F34: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Supply enabled after load limit restoration period (load limit triggered)"));
            case 0x00008F3F: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Unauthorised physical access - tamper detect"));
            case 0x00008F74: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Unauthorised physical access - meter cover removed"));
            case 0x00008F75: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Unauthorised physical access - strong magnetic field"));
            case 0x00008F76: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Unauthorised physical access - terminal cover removed"));
            case 0x00008F78: return Optional.of(new MeterEvent(date, MeterEvent.OTHER, meterEventCode, "Unauthorised physical access - other"));
            default: return Optional.empty();
        }
    }
}
