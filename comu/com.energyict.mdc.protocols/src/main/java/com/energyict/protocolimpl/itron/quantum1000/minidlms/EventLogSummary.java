/*
 * EventLogSummary.java
 *
 * Created on 8 december 2006, 15:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Koen
 */
public class EventLogSummary extends AbstractDataDefinition {

    private final int NUM_EVENT_SUMMARIES=16;

    private List eventLogSummariesTypes = new ArrayList();

    /**
     * Creates a new instance of EventLogSummary
     */
    public EventLogSummary(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EventLogSummary:\n");
        for (int i=0;i<getEventLogSummariesTypes().size();i++) {
            strBuff.append("       eventLogSummariesTypes("+i+")="+(EventLogSummariesType)getEventLogSummariesTypes().get(i)+"\n");
        }
        return strBuff.toString();
    }

    protected int getVariableName() {
        return 75; // DLMS_EVENT_LOG_SUMMARIES
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        for (int i=0;i<NUM_EVENT_SUMMARIES;i++) {
            EventLogSummariesType obj = new EventLogSummariesType(data, offset,  getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone());
            if (obj.getNumOccurences()>0)
               getEventLogSummariesTypes().add(obj);
            offset+=EventLogSummariesType.size();
        }
    }

    public List getEventLogSummariesTypes() {
        return eventLogSummariesTypes;
    }


}
