/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap;

import com.elster.jupiter.metering.ReadingInfo;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import aQute.bnd.annotation.ProviderType;
import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.schema.message.HeaderType;

import java.util.List;

@ProviderType
public interface SendMeterReadingsProvider {

    String NAME = "CIM SendMeterReadings";

    void call(List<ReadingInfo> readingInfos, HeaderType.Verb requestVerb);
    boolean call(MeterReadings meterReadings, HeaderType header, EndPointConfiguration endPointConfiguration);
}
