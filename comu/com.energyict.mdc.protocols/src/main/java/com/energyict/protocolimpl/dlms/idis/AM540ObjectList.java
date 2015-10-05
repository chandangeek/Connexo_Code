package com.energyict.protocolimpl.dlms.idis;

import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.mdc.common.ObisCode;

/**
 * Copyrights EnergyICT
 * Date: 21/05/13
 * Time: 12:04
 * Author: khe
 */
public class AM540ObjectList {

    /**
     * List representing the instantiated objects in the AM540 module.
     * This is used to avoid reading out the full list from the meter.
     * <p>
     * This list was copied from an AM540 module with firmware version V030100.16
     */
    public UniversalObject[] getObjectList() {

        return new UniversalObject[]{
                new UniversalObject(ObisCode.fromString("0.0.1.0.0.255"), DLMSClassId.findById(8)),
                new UniversalObject(ObisCode.fromString("0.0.10.0.0.255"), DLMSClassId.findById(9)),
                new UniversalObject(ObisCode.fromString("0.0.10.0.1.255"), DLMSClassId.findById(9)),
                new UniversalObject(ObisCode.fromString("0.0.10.0.100.255"), DLMSClassId.findById(9)),
                new UniversalObject(ObisCode.fromString("0.0.10.0.107.255"), DLMSClassId.findById(9)),
                new UniversalObject(ObisCode.fromString("0.0.11.0.0.255"), DLMSClassId.findById(11)),
                new UniversalObject(ObisCode.fromString("0.0.13.0.0.255"), DLMSClassId.findById(20)),
                new UniversalObject(ObisCode.fromString("0.0.14.0.0.255"), DLMSClassId.findById(6)),
                new UniversalObject(ObisCode.fromString("0.0.15.0.0.255"), DLMSClassId.findById(22)),
                new UniversalObject(ObisCode.fromString("0.0.15.0.13.255"), DLMSClassId.findById(22)),
                new UniversalObject(ObisCode.fromString("0.0.15.0.2.255"), DLMSClassId.findById(22)),
                new UniversalObject(ObisCode.fromString("0.0.15.0.3.255"), DLMSClassId.findById(22)),
                new UniversalObject(ObisCode.fromString("0.0.16.1.0.255"), DLMSClassId.findById(21)),
                new UniversalObject(ObisCode.fromString("0.0.20.0.0.255"), DLMSClassId.findById(19)),
                new UniversalObject(ObisCode.fromString("0.0.21.0.0.255"), DLMSClassId.findById(7)),
                new UniversalObject(ObisCode.fromString("0.0.22.0.0.255"), DLMSClassId.findById(23)),
                new UniversalObject(ObisCode.fromString("0.0.25.0.0.255"), DLMSClassId.findById(41)),
                new UniversalObject(ObisCode.fromString("0.0.25.2.0.255"), DLMSClassId.findById(43)),
                new UniversalObject(ObisCode.fromString("0.0.25.7.0.255"), DLMSClassId.findById(48)),
                new UniversalObject(ObisCode.fromString("0.0.29.0.0.255"), DLMSClassId.findById(90)),
                new UniversalObject(ObisCode.fromString("0.0.29.1.0.255"), DLMSClassId.findById(91)),
                new UniversalObject(ObisCode.fromString("0.0.29.2.0.255"), DLMSClassId.findById(92)),
                new UniversalObject(ObisCode.fromString("0.0.40.0.0.255"), DLMSClassId.findById(15)),
                new UniversalObject(ObisCode.fromString("0.0.40.0.1.255"), DLMSClassId.findById(15)),
                new UniversalObject(ObisCode.fromString("0.0.40.0.2.255"), DLMSClassId.findById(15)),
                new UniversalObject(ObisCode.fromString("0.0.40.0.3.255"), DLMSClassId.findById(15)),
                new UniversalObject(ObisCode.fromString("0.0.41.0.0.255"), DLMSClassId.findById(17)),
                new UniversalObject(ObisCode.fromString("0.0.42.0.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.43.0.0.255"), DLMSClassId.findById(64)),
                new UniversalObject(ObisCode.fromString("0.0.43.0.1.255"), DLMSClassId.findById(64)),
                new UniversalObject(ObisCode.fromString("0.0.43.0.2.255"), DLMSClassId.findById(64)),
                new UniversalObject(ObisCode.fromString("0.0.43.0.3.255"), DLMSClassId.findById(64)),
                new UniversalObject(ObisCode.fromString("0.0.43.1.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.43.1.1.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.44.0.0.255"), DLMSClassId.findById(18)),
                new UniversalObject(ObisCode.fromString("0.0.94.33.10.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.94.33.11.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.94.33.12.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.94.33.13.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.94.33.14.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.94.33.9.255"), DLMSClassId.findById(7)),
                new UniversalObject(ObisCode.fromString("0.0.96.1.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.96.1.1.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.96.1.3.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.96.1.4.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.96.1.5.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.96.10.1.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.96.10.2.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.96.11.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.96.11.1.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.96.13.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.96.14.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.96.62.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.96.65.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.96.7.19.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("0.0.96.7.20.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("0.0.96.7.21.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.96.7.9.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.97.97.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.97.98.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.97.98.10.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.98.1.0.255"), DLMSClassId.findById(7)),
                new UniversalObject(ObisCode.fromString("0.0.99.98.0.255"), DLMSClassId.findById(7)),
                new UniversalObject(ObisCode.fromString("0.0.99.98.1.255"), DLMSClassId.findById(7)),
                new UniversalObject(ObisCode.fromString("0.0.99.98.4.255"), DLMSClassId.findById(7)),
                new UniversalObject(ObisCode.fromString("0.1.94.31.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.1.94.31.10.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.1.94.31.3.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.1.94.31.7.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.1.94.31.8.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.1.94.31.9.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.128.0.0.0.255"), DLMSClassId.findById(8192)),
                new UniversalObject(ObisCode.fromString("0.128.0.0.1.255"), DLMSClassId.findById(8194)),
                new UniversalObject(ObisCode.fromString("0.128.0.0.13.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.128.0.0.14.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.128.0.0.15.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.128.0.0.17.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.128.1.0.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.128.5.0.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.128.5.0.1.255"), DLMSClassId.findById(8195)),
                new UniversalObject(ObisCode.fromString("0.128.6.0.0.255"), DLMSClassId.findById(9128)),
                new UniversalObject(ObisCode.fromString("0.128.8.0.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.128.8.0.1.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("1.0.0.2.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("1.0.0.2.8.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("1.0.0.9.11.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.1.24.0.255"), DLMSClassId.findById(5)),
                new UniversalObject(ObisCode.fromString("1.0.1.4.0.255"), DLMSClassId.findById(5)),
                new UniversalObject(ObisCode.fromString("1.0.1.6.0.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("1.0.1.6.1.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("1.0.1.6.2.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("1.0.1.6.3.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("1.0.1.6.4.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("1.0.1.7.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.1.8.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.1.8.1.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.1.8.2.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.1.8.3.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.1.8.4.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.12.31.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.12.35.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.12.39.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.12.43.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.12.44.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.12.45.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.15.24.0.255"), DLMSClassId.findById(5)),
                new UniversalObject(ObisCode.fromString("1.0.15.7.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.15.8.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.16.8.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.2.4.0.255"), DLMSClassId.findById(5)),
                new UniversalObject(ObisCode.fromString("1.0.2.6.0.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("1.0.2.6.1.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("1.0.2.6.2.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("1.0.2.6.3.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("1.0.2.6.4.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("1.0.2.7.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.2.8.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.2.8.1.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.2.8.2.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.2.8.3.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.2.8.4.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.21.24.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.21.7.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.22.24.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.22.7.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.3.4.0.255"), DLMSClassId.findById(5)),
                new UniversalObject(ObisCode.fromString("1.0.3.6.0.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("1.0.3.6.1.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("1.0.3.6.2.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("1.0.3.6.3.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("1.0.3.6.4.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("1.0.3.7.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.3.8.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.3.8.1.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.3.8.2.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.3.8.3.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.3.8.4.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.31.24.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.31.4.0.255"), DLMSClassId.findById(5)),
                new UniversalObject(ObisCode.fromString("1.0.31.7.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.32.24.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.32.32.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("1.0.32.33.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.32.34.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.32.36.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("1.0.32.37.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.32.38.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.32.7.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.4.4.0.255"), DLMSClassId.findById(5)),
                new UniversalObject(ObisCode.fromString("1.0.4.6.0.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("1.0.4.6.1.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("1.0.4.6.2.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("1.0.4.6.3.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("1.0.4.6.4.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("1.0.4.7.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.4.8.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.4.8.1.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.4.8.2.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.4.8.3.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.4.8.4.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.41.24.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.41.7.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.42.24.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.42.7.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.5.8.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.5.8.1.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.5.8.2.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.5.8.3.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.5.8.4.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.51.24.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.51.4.0.255"), DLMSClassId.findById(5)),
                new UniversalObject(ObisCode.fromString("1.0.51.7.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.52.24.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.52.32.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("1.0.52.33.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.52.34.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.52.36.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("1.0.52.37.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.52.38.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.52.7.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.6.8.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.6.8.1.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.6.8.2.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.6.8.3.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.6.8.4.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.61.24.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.61.7.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.62.24.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.62.7.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.7.8.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.7.8.1.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.7.8.2.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.7.8.3.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.7.8.4.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.71.24.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.71.4.0.255"), DLMSClassId.findById(5)),
                new UniversalObject(ObisCode.fromString("1.0.71.7.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.72.24.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.72.32.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("1.0.72.33.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.72.34.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.72.36.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("1.0.72.37.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.72.38.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.72.7.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.8.8.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.8.8.1.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.8.8.2.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.8.8.3.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.8.8.4.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.81.7.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("1.0.90.7.0.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("1.0.99.1.0.255"), DLMSClassId.findById(7)),
                new UniversalObject(ObisCode.fromString("1.0.99.2.0.255"), DLMSClassId.findById(7)),
                new UniversalObject(ObisCode.fromString("1.0.99.97.0.255"), DLMSClassId.findById(7)),
                new UniversalObject(ObisCode.fromString("1.1.0.2.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("1.1.0.2.8.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("1.2.0.2.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("1.2.0.2.8.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("1.3.0.2.8.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("1.0.16.7.0.255"), DLMSClassId.findById(3)),

                // Objects concerning disconnector.
                new UniversalObject(ObisCode.fromString("0.0.15.0.1.255"), DLMSClassId.findById(22)),
                new UniversalObject(ObisCode.fromString("0.0.10.0.106.255"), DLMSClassId.findById(9)),
                new UniversalObject(ObisCode.fromString("0.0.96.3.10.255"), DLMSClassId.findById(70)),
                new UniversalObject(ObisCode.fromString("0.0.96.11.2.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.99.98.2.255"), DLMSClassId.findById(7)),
                new UniversalObject(ObisCode.fromString("0.0.17.0.0.255"), DLMSClassId.findById(71)),
                new UniversalObject(ObisCode.fromString("1.0.31.4.0.255"), DLMSClassId.findById(5)),
                new UniversalObject(ObisCode.fromString("1.0.51.4.0.255"), DLMSClassId.findById(5)),
                new UniversalObject(ObisCode.fromString("1.0.71.4.0.255"), DLMSClassId.findById(5)),
                new UniversalObject(ObisCode.fromString("1.0.16.24.0.255"), DLMSClassId.findById(5)),

                // Additional objects (most of them are related to MBus support)
                new UniversalObject(ObisCode.fromString("0.0.10.0.103.255"), DLMSClassId.findById(9)),
                new UniversalObject(ObisCode.fromString("0.0.24.6.0.255"), DLMSClassId.findById(74)),
                new UniversalObject(ObisCode.fromString("0.0.96.11.3.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.0.99.98.3.255"), DLMSClassId.findById(7)),
                new UniversalObject(ObisCode.fromString("0.1.10.0.106.255"), DLMSClassId.findById(9)),
                new UniversalObject(ObisCode.fromString("0.1.15.0.1.255"), DLMSClassId.findById(22)),
                new UniversalObject(ObisCode.fromString("0.1.24.1.0.255"), DLMSClassId.findById(72)),
                new UniversalObject(ObisCode.fromString("0.1.24.2.1.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("0.1.24.2.2.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("0.1.24.2.3.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("0.1.24.2.4.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("0.1.24.3.0.255"), DLMSClassId.findById(7)),
                new UniversalObject(ObisCode.fromString("0.1.24.4.0.255"), DLMSClassId.findById(70)),
                new UniversalObject(ObisCode.fromString("0.1.24.5.0.255"), DLMSClassId.findById(7)),
                new UniversalObject(ObisCode.fromString("0.1.24.6.0.255"), DLMSClassId.findById(74)),
                new UniversalObject(ObisCode.fromString("0.1.96.1.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.1.96.10.3.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.1.96.11.4.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.1.96.3.10.255"), DLMSClassId.findById(70)),
                new UniversalObject(ObisCode.fromString("0.128.94.31.11.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("0.128.94.31.12.255"), DLMSClassId.findById(3)),
                new UniversalObject(ObisCode.fromString("0.2.24.1.0.255"), DLMSClassId.findById(72)),
                new UniversalObject(ObisCode.fromString("0.2.24.2.1.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("0.2.24.2.2.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("0.2.24.2.3.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("0.2.24.2.4.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("0.2.24.3.0.255"), DLMSClassId.findById(7)),
                new UniversalObject(ObisCode.fromString("0.2.24.4.0.255"), DLMSClassId.findById(70)),
                new UniversalObject(ObisCode.fromString("0.2.24.5.0.255"), DLMSClassId.findById(7)),
                new UniversalObject(ObisCode.fromString("0.2.96.1.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.2.96.10.3.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.2.96.11.4.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.2.96.3.10.255"), DLMSClassId.findById(70)),
                new UniversalObject(ObisCode.fromString("0.3.24.1.0.255"), DLMSClassId.findById(72)),
                new UniversalObject(ObisCode.fromString("0.3.24.2.1.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("0.3.24.2.2.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("0.3.24.2.3.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("0.3.24.2.4.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("0.3.24.3.0.255"), DLMSClassId.findById(7)),
                new UniversalObject(ObisCode.fromString("0.3.24.4.0.255"), DLMSClassId.findById(70)),
                new UniversalObject(ObisCode.fromString("0.3.24.5.0.255"), DLMSClassId.findById(7)),
                new UniversalObject(ObisCode.fromString("0.3.96.1.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.3.96.10.3.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.3.96.11.4.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.4.24.1.0.255"), DLMSClassId.findById(72)),
                new UniversalObject(ObisCode.fromString("0.4.24.2.1.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("0.4.24.2.2.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("0.4.24.2.3.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("0.4.24.2.4.255"), DLMSClassId.findById(4)),
                new UniversalObject(ObisCode.fromString("0.4.24.3.0.255"), DLMSClassId.findById(7)),
                new UniversalObject(ObisCode.fromString("0.4.24.4.0.255"), DLMSClassId.findById(70)),
                new UniversalObject(ObisCode.fromString("0.4.24.5.0.255"), DLMSClassId.findById(7)),
                new UniversalObject(ObisCode.fromString("0.4.96.1.0.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.4.96.10.3.255"), DLMSClassId.findById(1)),
                new UniversalObject(ObisCode.fromString("0.4.96.11.4.255"), DLMSClassId.findById(1))
        };
    }
}