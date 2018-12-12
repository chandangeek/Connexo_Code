/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edmi.mk6.events;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocolimplv2.edmi.mk6.profiles.LoadSurveyData;

import java.util.List;

/**
 * @author sva
 * @since 7/03/2017 - 12:22
 */
public interface EventLogParser {

    List<MeterProtocolEvent> parseMeterProtocolEvents(LoadSurveyData logSurveyData) throws ProtocolException;
}
